package com.springboot.dao;

import com.springboot.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description: UserMapper
 *
 * @author hbl
 * @date 2021/03/11 0011 14:10
 */
@Mapper
public interface UserMapper
{
    User findByUserName(String userName);
}
