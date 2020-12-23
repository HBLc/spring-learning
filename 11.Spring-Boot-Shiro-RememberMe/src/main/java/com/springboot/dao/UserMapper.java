package com.springboot.dao;

import com.springboot.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description: UserMapper
 *
 * @author hbl
 * @date 2020/12/23 0023 15:59
 */
@Mapper
public interface UserMapper
{
    User findByUserName(String userName);
}
