package com.wsf.domain;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.github.f4b6a3.uuid.alt.GUID;

///主键生成策略
public class CustomIdGenerator implements IdentifierGenerator {

	@Override
	public Serializable generate(SharedSessionContractImplementor arg0, Object arg1) throws HibernateException {
//		RandomGenerator random = RandomGenerator.getDefault();
//		TimeOrderedEpochFactory factory = new TimeOrderedEpochFactory(random::nextLong);
		SecureRandom secureRandom = new SecureRandom();
		GUID guid = GUID.v7(Instant.now(), secureRandom);
//		UUID uuid = UuidCreator.getTimeOrderedEpochPlus1();
		return guid.toString();
//		return factory.create().toString();
	}

}