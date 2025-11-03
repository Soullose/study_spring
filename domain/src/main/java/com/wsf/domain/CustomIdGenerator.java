package com.wsf.domain;

import java.io.Serializable;
import java.util.random.RandomGenerator;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.github.f4b6a3.uuid.factory.standard.TimeOrderedEpochFactory;

///主键生成策略
public class CustomIdGenerator implements IdentifierGenerator {

	@Override
	public Serializable generate(SharedSessionContractImplementor arg0, Object arg1) throws HibernateException {
		RandomGenerator random = RandomGenerator.getDefault();
		TimeOrderedEpochFactory factory = new TimeOrderedEpochFactory(random::nextLong);
		return factory.create().toString();
	}

}