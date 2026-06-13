# 06. 運用と課金：止める・直す・直す前に確認する

> この章で学ぶこと: **インスタンスの停止と再開**、**スタックの完全削除**、**ログの見方**、**よくあるトラブル**、**課金の考え方**。

## 目次

1. [運用コマンド一覧](#運用コマンド一覧)
2. [一時停止（pause）](#一時停止pause)
3. [完全削除（destroy）](#完全削除destroy)
4. [ログと状態確認](#ログと状態確認)
5. [よくあるトラブル](#よくあるトラブル)
6. [課金の目安](#課金の目安)
7. [今後の拡張候補](#今後の拡張候補)
8. [まず覚えるポイント](#まず覚えるポイント)

---

## 運用コマンド一覧

| コマンド | 目的 |
|----------|------|
| `./infra/scripts/pause.sh` | EC2 を停止（コンピュート課金を抑える） |
| `./infra/scripts/resume.sh` | EC2 を再開（存在する場合） |
| `./infra/scripts/destroy.sh` | CDK スタックを完全削除 |
| `./infra/scripts/deploy-app.sh` | アプリだけ再デプロイ |
| `aws ssm start-session --target {InstanceId}` | SSM 経由でシェル接続（SSH 不要） |

InstanceId は `cdk deploy` の Output か、CloudFormation コンソールから確認します。

---

## 一時停止（pause）

```bash
./infra/scripts/pause.sh
```

### 何が起きるか

- EC2 インスタンスが **stop** される
- **コンピュート課金**（インスタンス時間）は止まる
- **EBS（ディスク）**、Elastic IP、Route 53 などは課金が続く場合がある

### 再開

```bash
aws ec2 start-instances --instance-ids {InstanceId} --region ap-northeast-1
```

停止中はアプリにアクセスできません。再開後、Docker と systemd サービスが自動起動するかは設定次第です。問題があれば `deploy-app.sh` で修復できます。

---

## 完全削除（destroy）

```bash
./infra/scripts/destroy.sh
```

確認プロンプトのあと、`cdk destroy SmartHouseholdStack --force` を実行します。

### 削除される主なリソース

| リソース | 備考 |
|----------|------|
| EC2 | インスタンスごと消える |
| EBS | `deleteOnTermination: true` のため一緒に削除 |
| Elastic IP | 解放される |
| ECR リポジトリ | `emptyOnDelete: true` でイメージも削除 |
| Secrets Manager | `RemovalPolicy.DESTROY` |
| Route 53 A レコード | CDK が作ったレコードは削除 |
| VPC、SG、IAM ロール | スタックに含まれるものは削除 |

### 削除されないもの

| リソース | 理由 |
|----------|------|
| **Cognito User Pool** | 既存リソースを参照しているだけ（CDK で未管理） |
| Route 53 ホストゾーン自体 | 既存ゾーンを import しているだけ |
| 手動で作った DNS レコード（www など） | スタック外 |

学習終了後に課金を止めたいときは、**destroy を忘れない**ことが重要です。

---

## ログと状態確認

### EC2 上の bootstrap ログ

User Data と bootstrap の出力は次に保存されます。

```text
/var/log/smart-household-bootstrap.log
```

SSM セッションで EC2 に入り、`tail -f` で追えます。

### SSM コマンドの結果

`deploy-app.sh` 実行後、失敗した場合は表示される `CommandId` で詳細を確認します。

```bash
aws ssm get-command-invocation \
  --command-id {CommandId} \
  --instance-id {InstanceId} \
  --region ap-northeast-1
```

### Docker Compose

EC2 上（SSM セッション内）:

```bash
cd /opt/smart-household/app
docker compose --env-file /opt/smart-household/.env \
  -f docker/compose/docker-compose.single-host.yaml \
  -f docker/compose/docker-compose.single-host.prod.yaml \
  -f docker/compose/docker-compose.single-host.aws.yaml \
  ps
```

### Frontend（systemd）

```bash
systemctl status smart-household-frontend.service
journalctl -u smart-household-frontend.service -n 100
```

### Nginx / certbot

```bash
nginx -t
systemctl status nginx
certbot certificates
```

---

## よくあるトラブル

### bootstrap が DB パスワード待ちで止まる

**原因**: `init-secrets.sh` を実行していない、または Secrets が空。

**対処**:

```bash
./infra/scripts/init-secrets.sh
./infra/scripts/deploy-app.sh
```

### certbot / HTTPS が失敗する

**原因の例**:

- Route 53 の A レコードがまだ Elastic IP を向いていない
- ポート 80 が SG で塞がれている
- 別サーバーが同じドメインを向いている

**対処**: DNS を `dig +short your-domain.com` で確認。伝播後に SSM で `certbot --nginx` を手動再実行。

### ECR にイメージが無い

**症状**: bootstrap ログに `Backend image not found in ECR yet`

**対処**: `./infra/scripts/deploy-app.sh` を実行。

### Frontend ビルドが OOM

**症状**: `npm run build` 中に killed、メモリ不足

**対処**:

- `cdk.json` の `instanceType` を `t4g.small` 以上にする
- `deploy.sh` で EC2 を作り直したあと `deploy-app.sh`

### ログイン後に Cognito エラー

**原因**: コールバック URL が本番ドメインと不一致。

**対処**: Cognito App Client の許可 URL を `https://{domainName}/` に更新。

### アプリは動くが API が CORS エラー

**原因**: SSM の `cors-allowed-origins` にフロントの Origin が無い、または EC2 の `.env` が古い。

**対処**: `domainName` を確認して `deploy.sh` を実行し、`deploy-app.sh` で `.env` を再生成。

### SSM コマンドが TimedOut

**原因**: 初回 Frontend ビルドに 90 分以上かかった、ネットワーク問題。

**対処**: `get-command-invocation` で途中ログを確認。EC2 上で bootstrap がまだ動いていれば完了を待つか、手動で `remote-app-deploy.sh` を実行。

---

## 課金の目安

正確な金額はリージョンと利用量で変わります。学習時に意識すべき **課金の柱** は次の通りです。

| 項目 | 停止中（pause） | 削除後（destroy） |
|------|-----------------|-------------------|
| EC2 インスタンス時間 | 止まる | 無し |
| EBS ストレージ | **継続** | 無し（削除時） |
| Elastic IP（割当のみ） | **継続**（要注意） | 無し |
| Route 53 ホストゾーン | 継続 | ゾーン自体は残る |
| ECR ストレージ | 継続 | 削除 |
| Secrets Manager | 継続 | 削除 |
| データ転送 | 利用時のみ | — |

**コスト削減のコツ**

- 使わないときは `pause.sh` で EC2 を止める
- 学習終了後は `destroy.sh` でスタックごと消す
- Elastic IP を割り当てたまま EC2 だけ止めると、IP 保持料がかかる場合がある

---

## 今後の拡張候補

現在の構成は学習向けの **案 A** です。本番要件が厳しくなったときの検討例です。

| 課題 | 拡張案 |
|------|--------|
| 可用性 | マルチ AZ、ALB + 複数 EC2 |
| DB 運用 | Amazon RDS for MySQL |
| シークレットローテーション | Secrets Manager の自動ローテーション |
| デプロイ | GitHub Actions から `deploy-app.sh` を自動実行 |
| 監視 | CloudWatch Agent、Actuator + アラーム |
| HTTPS 運用 | ALB + ACM に移行し certbot を廃止 |

これらは現スタックの範囲外です。ドキュメントや CDK を段階的に増やすときの参考にしてください。

---

## まず覚えるポイント

- 一時的に課金を抑えるなら **`pause.sh`**（EC2 停止）。完全に止めるなら **`destroy.sh`**。
- bootstrap のログは **`/var/log/smart-household-bootstrap.log`** にある。
- デプロイ失敗は **SSM の CommandId** で原因を追える。
- Secrets 未投入と DNS 未反映は、初回トラブルの定番原因。
- Cognito のコールバック URL と CORS は、ドメイン変更時に必ず見直す。
