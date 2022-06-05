package com.wsf.domain;

/**
 * open
 * 2022/6/4
 */
public interface BaseMapper<D,E> {
    /**
     *
     * @param entity
     * @return
     */
    D toDto(E entity);

    /**
     *
     * @param dto
     * @return
     */
    E toEntity(D dto);
}
