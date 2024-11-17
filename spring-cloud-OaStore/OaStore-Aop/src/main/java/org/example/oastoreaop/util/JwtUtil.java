package org.example.oastoreaop.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    // 密钥
    private static final byte[] SECRET = "52d907a4b404af790cf2cf488acc4836".getBytes();

    // 生成JWT Token
    public static String genToken(Map<String, Object> claims) {
        try {
            // 设置JWT的有效时间，比如1小时
            Date expirationTime = new Date(System.currentTimeMillis() + 1000 * 60 * 60);

            // 创建 JWT Claims
            JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder();
            claimsSetBuilder.expirationTime(expirationTime);
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsSetBuilder.claim(entry.getKey(), entry.getValue());
            }

            // 创建JWT Claims
            JWTClaimsSet claimsSet = claimsSetBuilder.build();

            // 使用 HMAC 签名
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256), // 使用 HMAC 签名算法
                    claimsSet
            );

            // 创建 HMAC 签名器
            JWSSigner signer = new MACSigner(SECRET);

            // 对 JWT 进行签名
            signedJWT.sign(signer);

            // 返回 JWT token
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Token生成失败", e);
        }
    }
}
