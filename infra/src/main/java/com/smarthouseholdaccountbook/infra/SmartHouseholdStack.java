package com.smarthouseholdaccountbook.infra;

import java.util.List;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.BlockDevice;
import software.amazon.awscdk.services.ec2.BlockDeviceVolume;
import software.amazon.awscdk.services.ec2.CfnEIP;
import software.amazon.awscdk.services.ec2.CfnEIPAssociation;
import software.amazon.awscdk.services.ec2.EbsDeviceProps;
import software.amazon.awscdk.services.ec2.EbsDeviceVolumeType;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.route53.ARecord;
import software.amazon.awscdk.services.route53.HostedZone;
import software.amazon.awscdk.services.route53.HostedZoneAttributes;
import software.amazon.awscdk.services.route53.IHostedZone;
import software.amazon.awscdk.services.route53.RecordTarget;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.amazon.awscdk.services.s3.assets.Asset;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

/**
 * 案 A: EC2 1 台 + Docker Compose + 既存 Cognito + Route 53 + HTTPS (Let's Encrypt)。
 */
public class SmartHouseholdStack extends Stack {

    public SmartHouseholdStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final String projectName = InfraApp.contextString(this, "projectName", "smart-household");
        final String gitRepositoryUrl = InfraApp.contextString(this, "gitRepositoryUrl", "");
        final String gitRepositoryBranch = InfraApp.contextString(this, "gitRepositoryBranch", "master");
        final boolean enableSshAccess = InfraApp.contextBoolean(this, "enableSshAccess", false);
        final String allowedSshCidr = InfraApp.contextString(this, "allowedSshCidr", "0.0.0.0/0");
        final int rootVolumeGiB = InfraApp.contextInt(this, "rootVolumeGiB", 30);

        // --- ドメイン / 既存 Cognito（cdk.local.json で指定） ---
        final String domainName = InfraApp.contextString(this, "domainName", "");
        final String hostedZoneName = InfraApp.contextString(this, "hostedZoneName", "");
        final String hostedZoneId = InfraApp.contextString(this, "hostedZoneId", "");
        final String certbotEmail = InfraApp.contextString(this, "certbotEmail", "");
        final String cognitoUserPoolId = InfraApp.contextString(this, "cognitoUserPoolId", "");
        final String cognitoClientId = InfraApp.contextString(this, "cognitoClientId", "");

        validateRequiredContext(domainName, hostedZoneName, hostedZoneId, certbotEmail,
                cognitoUserPoolId, cognitoClientId);

        final String region = Stack.of(this).getRegion();
        final String issuerUrl = "https://cognito-idp." + region + ".amazonaws.com/" + cognitoUserPoolId;
        final String jwkSetUrl = issuerUrl + "/.well-known/jwks.json";
        final String appBaseUrl = "https://" + domainName;
        final String corsAllowedOrigins = appBaseUrl;

        // コスト削減: NAT Gateway なしの最小 VPC
        IVpc vpc = Vpc.Builder.create(this, "AppVpc")
                .maxAzs(1)
                .natGateways(0)
                .subnetConfiguration(List.of(
                        software.amazon.awscdk.services.ec2.SubnetConfiguration.builder()
                                .name("Public")
                                .subnetType(SubnetType.PUBLIC)
                                .cidrMask(24)
                                .build()))
                .build();

        // --- 既存 Cognito 設定を SSM に保存（EC2 bootstrap が参照） ---
        StringParameter.Builder.create(this, "CognitoUserPoolIdParam")
                .parameterName("/" + projectName + "/cognito/user-pool-id")
                .stringValue(cognitoUserPoolId)
                .build();

        StringParameter.Builder.create(this, "CognitoClientIdParam")
                .parameterName("/" + projectName + "/cognito/client-id")
                .stringValue(cognitoClientId)
                .build();

        StringParameter.Builder.create(this, "CognitoIssuerParam")
                .parameterName("/" + projectName + "/cognito/issuer-url")
                .stringValue(issuerUrl)
                .build();

        StringParameter.Builder.create(this, "DomainNameParam")
                .parameterName("/" + projectName + "/domain/name")
                .stringValue(domainName)
                .build();

        StringParameter.Builder.create(this, "DomainAppUrlParam")
                .parameterName("/" + projectName + "/domain/app-url")
                .stringValue(appBaseUrl)
                .build();

        StringParameter.Builder.create(this, "CorsAllowedOriginsParam")
                .parameterName("/" + projectName + "/domain/cors-allowed-origins")
                .stringValue(corsAllowedOrigins)
                .description("Spring Boot CORS allowed origins (comma-separated)")
                .build();

        StringParameter.Builder.create(this, "CertbotEmailParam")
                .parameterName("/" + projectName + "/domain/certbot-email")
                .stringValue(certbotEmail)
                .build();

        String gitUrlForSsm = gitRepositoryUrl.isBlank() ? "none" : gitRepositoryUrl;

        StringParameter.Builder.create(this, "GitRepositoryUrlParam")
                .parameterName("/" + projectName + "/deploy/git-repository-url")
                .stringValue(gitUrlForSsm)
                .build();

        StringParameter.Builder.create(this, "GitRepositoryBranchParam")
                .parameterName("/" + projectName + "/deploy/git-repository-branch")
                .stringValue(gitRepositoryBranch)
                .build();

        // --- Secrets Manager ---
        Secret appSecret = Secret.Builder.create(this, "AppSecret")
                .secretName(projectName + "/app")
                .description("MySQL passwords, OpenAI API key, and runtime secrets")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // --- ECR ---
        Repository backendRepository = Repository.Builder.create(this, "BackendRepository")
                .repositoryName(projectName + "/backend")
                .removalPolicy(RemovalPolicy.DESTROY)
                .emptyOnDelete(true)
                .lifecycleRules(List.of(
                        software.amazon.awscdk.services.ecr.LifecycleRule.builder()
                                .maxImageCount(5)
                                .description("Keep only recent images to save storage cost")
                                .build()))
                .build();

        // --- EC2 IAM ロール ---
        Role instanceRole = Role.Builder.create(this, "Ec2InstanceRole")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")))
                .build();

        appSecret.grantRead(instanceRole);
        backendRepository.grantPull(instanceRole);

        instanceRole.addToPolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of("ssm:GetParameter", "ssm:GetParameters"))
                .resources(List.of(
                        "arn:aws:ssm:" + region + ":" + Stack.of(this).getAccount() + ":parameter/" + projectName + "/*"))
                .build());

        Asset bootstrapAsset = Asset.Builder.create(this, "BootstrapAsset")
                .path("assets/ec2-bootstrap")
                .build();
        bootstrapAsset.grantRead(instanceRole);

        SecurityGroup instanceSecurityGroup = SecurityGroup.Builder.create(this, "InstanceSecurityGroup")
                .vpc(vpc)
                .description("Allow HTTP/HTTPS to Nginx only")
                .allowAllOutbound(true)
                .build();

        instanceSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "HTTP (certbot + redirect)");
        instanceSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(443), "HTTPS");
        if (enableSshAccess) {
            instanceSecurityGroup.addIngressRule(Peer.ipv4(allowedSshCidr), Port.tcp(22), "SSH (optional)");
        }

        UserData userData = UserData.forLinux();
        userData.addCommands(
                "#!/bin/bash",
                "set -euxo pipefail",
                "exec > >(tee /var/log/smart-household-bootstrap.log) 2>&1",
                "",
                "PROJECT_NAME=" + shellQuote(projectName),
                "AWS_REGION=" + shellQuote(region),
                "ECR_REPO_URI=" + shellQuote(backendRepository.getRepositoryUri()),
                "APP_SECRET_ARN=" + shellQuote(appSecret.getSecretArn()),
                "BOOTSTRAP_ASSET_URL=" + shellQuote(bootstrapAsset.getS3ObjectUrl()),
                "",
                "dnf install -y aws-cli unzip",
                "mkdir -p /opt/smart-household/bootstrap",
                "aws s3 cp \"$BOOTSTRAP_ASSET_URL\" /tmp/bootstrap.zip --region \"$AWS_REGION\"",
                "unzip -o /tmp/bootstrap.zip -d /opt/smart-household/bootstrap",
                "chmod +x /opt/smart-household/bootstrap/bootstrap.sh",
                "chmod +x /opt/smart-household/bootstrap/remote-app-deploy.sh",
                "",
                "export PROJECT_NAME AWS_REGION ECR_REPO_URI APP_SECRET_ARN",
                "/opt/smart-household/bootstrap/bootstrap.sh");

        InstanceType instanceType = parseInstanceType(
                InfraApp.contextString(this, "instanceType", "t4g.small"));

        Instance instance = Instance.Builder.create(this, "AppInstance")
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .instanceType(instanceType)
                .machineImage(MachineImage.fromSsmParameter(
                        "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64"))
                .securityGroup(instanceSecurityGroup)
                .role(instanceRole)
                .userData(userData)
                .requireImdsv2(true)
                .blockDevices(List.of(
                        BlockDevice.builder()
                                .deviceName("/dev/xvda")
                                .volume(BlockDeviceVolume.ebs(rootVolumeGiB, EbsDeviceProps.builder()
                                        .encrypted(true)
                                        .deleteOnTermination(true)
                                        .volumeType(EbsDeviceVolumeType.GP3)
                                        .build()))
                                .build()))
                .build();

        // --- Elastic IP（IP 固定 + Route 53 A レコード） ---
        CfnEIP elasticIp = CfnEIP.Builder.create(this, "AppElasticIp")
                .domain("vpc")
                .build();

        CfnEIPAssociation.Builder.create(this, "AppElasticIpAssociation")
                .allocationId(elasticIp.getAttrAllocationId())
                .instanceId(instance.getInstanceId())
                .build();

        IHostedZone hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone",
                HostedZoneAttributes.builder()
                        .hostedZoneId(hostedZoneId)
                        .zoneName(hostedZoneName)
                        .build());

        String recordName = toRecordName(domainName, hostedZoneName);

        ARecord.Builder aRecordBuilder = ARecord.Builder.create(this, "AppARecord")
                .zone(hostedZone)
                .target(RecordTarget.fromIpAddresses(elasticIp.getAttrPublicIp()))
                .ttl(Duration.minutes(5));
        // ゾーン apex（ルートドメイン）のとき recordName は省略
        if (!recordName.isEmpty()) {
            aRecordBuilder.recordName(recordName);
        }
        aRecordBuilder.build();

        // --- Outputs ---
        CfnOutput.Builder.create(this, "InstanceId")
                .value(instance.getInstanceId())
                .build();

        CfnOutput.Builder.create(this, "ElasticIp")
                .value(elasticIp.getAttrPublicIp())
                .description("Elastic IP attached to EC2")
                .build();

        CfnOutput.Builder.create(this, "AppUrl")
                .value(appBaseUrl)
                .description("Application URL (HTTPS after certbot on first boot)")
                .build();

        CfnOutput.Builder.create(this, "BackendRepositoryUri")
                .value(backendRepository.getRepositoryUri())
                .build();

        CfnOutput.Builder.create(this, "AppSecretName")
                .value(appSecret.getSecretName())
                .build();

        CfnOutput.Builder.create(this, "AppSecretArn")
                .value(appSecret.getSecretArn())
                .description("Secrets Manager ARN for bootstrap / deploy-app")
                .build();

        CfnOutput.Builder.create(this, "BootstrapAssetS3Url")
                .value(bootstrapAsset.getS3ObjectUrl())
                .description("S3 URI of EC2 bootstrap asset (deploy-app uses for repair)")
                .build();

        CfnOutput.Builder.create(this, "CognitoUserPoolId")
                .value(cognitoUserPoolId)
                .build();

        CfnOutput.Builder.create(this, "CognitoClientId")
                .value(cognitoClientId)
                .build();

        CfnOutput.Builder.create(this, "CognitoIssuerUrl")
                .value(issuerUrl)
                .build();

        CfnOutput.Builder.create(this, "CognitoJwkSetUrl")
                .value(jwkSetUrl)
                .build();

        CfnOutput.Builder.create(this, "CognitoCallbackUrlHint")
                .value(appBaseUrl + "/")
                .description("Add to Cognito App Client allowed callback/sign-out URLs")
                .build();

        CfnOutput.Builder.create(this, "DestroyCommandHint")
                .value("cd infra && npm run cdk -- destroy SmartHouseholdStack")
                .build();
    }

    private static void validateRequiredContext(
            final String domainName,
            final String hostedZoneName,
            final String hostedZoneId,
            final String certbotEmail,
            final String cognitoUserPoolId,
            final String cognitoClientId) {
        if (domainName.isBlank()) {
            throw new IllegalArgumentException(
                    "cdk.local.json 'domainName' is required (例: app.example.com)");
        }
        if (hostedZoneName.isBlank()) {
            throw new IllegalArgumentException(
                    "cdk.local.json 'hostedZoneName' is required (例: example.com)");
        }
        if (hostedZoneId.isBlank()) {
            throw new IllegalArgumentException(
                    "cdk.local.json 'hostedZoneId' is required (Route 53 コンソールで確認)");
        }
        if (certbotEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "cdk.local.json 'certbotEmail' is required (Let's Encrypt 通知用)");
        }
        if (cognitoUserPoolId.isBlank()) {
            throw new IllegalArgumentException(
                    "cdk.local.json 'cognitoUserPoolId' is required (既存 User Pool ID)");
        }
        if (cognitoClientId.isBlank()) {
            throw new IllegalArgumentException(
                    "cdk.local.json 'cognitoClientId' is required (既存 App Client ID)");
        }
        if (!domainName.endsWith(hostedZoneName) && !domainName.endsWith("." + hostedZoneName)) {
            throw new IllegalArgumentException(
                    "domainName '" + domainName + "' must be under hostedZoneName '" + hostedZoneName + "'");
        }
    }

    /** smart-household-account-book.com + 同ゾーン名 → apex（空文字 = recordName 省略） */
    static String toRecordName(final String domainName, final String hostedZoneName) {
        if (domainName.equals(hostedZoneName)) {
            return "";
        }
        String suffix = "." + hostedZoneName;
        if (domainName.endsWith(suffix)) {
            return domainName.substring(0, domainName.length() - suffix.length());
        }
        return domainName;
    }

    private static String shellQuote(final String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static InstanceType parseInstanceType(final String value) {
        String[] parts = value.split("\\.");
        if (parts.length != 2) {
            return InstanceType.of(InstanceClass.T4G, InstanceSize.MICRO);
        }
        InstanceClass instanceClass = InstanceClass.valueOf(parts[0].toUpperCase());
        InstanceSize instanceSize = InstanceSize.valueOf(parts[1].toUpperCase());
        return InstanceType.of(instanceClass, instanceSize);
    }
}
