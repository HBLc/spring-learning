package com.springboot.dao;

import com.springboot.pojo.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description: UserPermissionMapper
 *
 * @author hbl
 * @date 2021/03/11 0011 14:11
 */
@Mapper
public interface UserPermissionMapper
{
    List<Permission> findByUserName(String userName);
}
