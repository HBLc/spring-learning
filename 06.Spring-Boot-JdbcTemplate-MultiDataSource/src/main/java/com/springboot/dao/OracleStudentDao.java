package com.springboot.dao;

import java.util.List;
import java.util.Map;

/**
 * Description: OracleStudentDao
 *
 * @author hbl
 * @date 2020/8/12 0012 18:01
 */
public interface OracleStudentDao
{
    List<Map<String, Object>> getAllStudents();
}
