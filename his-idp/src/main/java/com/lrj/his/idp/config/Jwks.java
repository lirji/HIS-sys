package com.lrj.his.idp.config;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * 生成签发用 RSA 密钥对(dev:启动即生成,进程重启即轮换 —— 网关会经 JWKS 端点自动重新拉取)。
 * 生产应改为从密钥库/Nacos 加载固定密钥,避免重启使已签发 token 失效。
 */
final class Jwks {

    private Jwks() {
    }

    static RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("生成 RSA 密钥失败", ex);
        }
    }
}
