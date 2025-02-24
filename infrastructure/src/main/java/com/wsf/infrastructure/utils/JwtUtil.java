package com.wsf.infrastructure.utils;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * open SoulLose 2022-04-23 16:12
 */
public class JwtUtil {

	// 有效期
	public static final Long JWT_TTL = 60 * 60 * 1000L; // 60 * 60 * 1000l一个小时
	// 设置密码明文
	public static final String JWT_KEY = "OPEN";

	/**
	 * 生成jtw(uuid)
	 *
	 * @param subject
	 *            token中要存放的数据（json格式）
	 * @return
	 */
	public static String createJWT(String subject) {
		JwtBuilder builder = getJwtBuilder(subject, null, getUUID());// 设置过期时间
		return builder.compact();
	}

	/**
	 * 生成jtw(uuid)
	 *
	 * @param subject
	 *            token中要存放的数据（json格式）
	 * @param ttlMillis
	 *            token超时时间
	 * @return
	 */
	public static String createJWT(String subject, Long ttlMillis) {
		JwtBuilder builder = getJwtBuilder(subject, ttlMillis, getUUID());// 设置过期时间
		return builder.compact();
	}

	/**
	 * 创建token
	 *
	 * @param id
	 * @param subject
	 * @param ttlMillis
	 * @return
	 */
	public static String createJWT(String subject, Long ttlMillis, String id) {
		JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id);// 设置过期时间
		return builder.compact();
	}

	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 生成加密后的秘钥 secretKey
	 *
	 * @return
	 */
	public static SecretKey generalKey() {
		byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		return key;
	}

	/**
	 * 解析
	 *
	 * @param jwt
	 * @return
	 * @throws Exception
	 */
	public static Claims parseJWT(String jwt) throws Exception {
		SecretKey secretKey = generalKey();
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();
	}

	private static JwtBuilder getJwtBuilder(String subject, Long ttlMillis, String uuid) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		SecretKey secretKey = generalKey();
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		if (ttlMillis == null) {
			ttlMillis = JwtUtil.JWT_TTL;
		}
		long expMillis = nowMillis + ttlMillis;
		Date expDate = new Date(expMillis);
		return Jwts.builder().setId(uuid) // 唯一的ID
				.setSubject(subject) // 主题 可以是JSON数据
				.setIssuer("open") // 签发者
				.setIssuedAt(now) // 签发时间
				.signWith(signatureAlgorithm, secretKey) // 使用HS256对称加密算法签名, 第二个参数为秘钥
				.setExpiration(expDate);
	}
}
