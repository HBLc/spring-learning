package com.springboot.dao;

import com.springboot.pojo.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description: UserRoleMapper
 *
 * @author hbl
 * @date 2021/03/11 0011 14:11
 */
@Mapper
public interface UserRoleMapper
{
    List<Role> findByUserName(String userName);
}
