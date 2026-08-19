package com.wsf.infrastructure.jpa.id;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.github.f4b6a3.uuid.alt.GUID;

///主键生成策略
public class CustomIdGenerator implements IdentifierGenerator {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	@Override
	public Serializable generate(SharedSessionContractImplementor arg0, Object arg1) throws HibernateException {
		return generateId();
	}

	/**
	 * 静态方法生成ID，供非Hibernate场景使用
	 */
	public static String generateId() {
		GUID guid = GUID.v7(Instant.now(), SECURE_RANDOM);
		return guid.toString();
	}

}