package com.example.backend.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.*;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.proc.*;
import com.nimbusds.jwt.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProperties jwtProperties;
    // CognitoのJWKセット(JWT署名検証用の公開鍵)URL

    @Autowired
    public JwtAuthFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
     throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            try {
                //JWKセットを取得してJWTを検証
                JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(
                    URI.create(jwtProperties.getJwkSetUrl()).toURL(),
                    new DefaultResourceRetriever()
                );
                ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
                JWSKeySelector<SecurityContext> keySelector = 
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
                jwtProcessor.setJWSKeySelector(keySelector);
                
                //JWTの署名を検証しクレームを取得
                JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);
                
                //認証済みユーザーを表すオブジェクトを作成
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claimsSet.getSubject(),
                    null,
                    Collections.emptyList()
                );
                //セキュリティコンテキストに認証情報を設定
                //これによりアプリケーション全体でこのリクエストは認証済みとして扱われる
                SecurityContextHolder.getContext().setAuthentication(authentication);
                }catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        filterChain.doFilter(request, response);
    }
}