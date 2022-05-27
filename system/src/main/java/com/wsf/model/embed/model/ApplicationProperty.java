package com.wsf.model.embed.model;

import com.wsf.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * open
 * SoulLose
 * 2022-05-26 20:53
 */
@Getter
@Setter
@Entity
@Table(name = "t_open_application_property_")
public class ApplicationProperty extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1182864316154864701L;
    
    @Column(name = "name_")
    private String name;
    
    @Column(name = "value_")
    private String value;
    
    @Column(name = "description_")
    private String description;
}
