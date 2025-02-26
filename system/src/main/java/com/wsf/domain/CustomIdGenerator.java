package com.wsf.domain;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import cn.hutool.core.util.IdUtil;

///NanoId主键生成策略
public class CustomIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor arg0, Object arg1) throws HibernateException {
//        return NanoIdUtils.randomNanoId();
//        return IdUtil.fastSimpleUUID();
        return IdUtil.getSnowflakeNextIdStr();
    }

}
