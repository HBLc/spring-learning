package com.springboot.oracledao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Description: OracleDaoMapper
 *
 * @author hbl
 * @date 2020/7/16 0016 15:11
 */
@Mapper
public interface OracleDaoMapper
{
    List<Map<String, Object>> getAllStudents();
}
