package com.springboot.dao;

import com.springboot.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description: UserMapper
 *
 * @author hbl
 * @date 2020/10/10 0010 10:44
 */
@Mapper
public interface UserMapper
{
    User findByUserName(@Param("userName") String userName);
}
