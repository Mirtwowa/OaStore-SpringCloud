package org.example.oastoreaop.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
@Slf4j
public class JwtUtil {
    private final static Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    // 密钥
    private static final byte[] SECRET = "52d907a4b404af790cf2cf488acc4836".getBytes();

    // 生成JWT Token
    public static String genToken(Map<String, Object> claims) {
        try {
            // 设置JWT的有效时间，比如1个月
            Date expirationTime = new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 *30);

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
    //接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) throws ParseException {
        try {
            // 解析JWT token
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 创建HMAC验证器
            JWSVerifier verifier = new MACVerifier(SECRET);

            // 验证JWT token
            if (!signedJWT.verify(verifier)) {
                throw new JWTVerificationException("Token验证失败");
            }

            // 获取JWT的payload部分
            Object account = signedJWT.getJWTClaimsSet().getClaim("account");
            logger.info("account = {}", account);
            // 检查token是否过期
            if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                throw new JWTVerificationException("Token已过期");
            }

            // 返回业务数据
            return signedJWT.getJWTClaimsSet().getClaims();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException("Token解析失败", e);
        }
    }
}
