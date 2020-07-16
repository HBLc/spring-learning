package com.springboot.mysqldao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Description: MySqlDaoMapper
 *
 * @author hbl
 * @date 2020/7/16 0016 15:11
 */
@Mapper
public interface MySqlDaoMapper
{
    List<Map<String, Object>> getAllStudents();
}
