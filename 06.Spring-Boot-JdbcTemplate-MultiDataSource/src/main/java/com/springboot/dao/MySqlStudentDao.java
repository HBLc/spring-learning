package com.springboot.dao;

import java.util.List;
import java.util.Map;

/**
 * Description: MySqlStudentDao
 *
 * @author hbl
 * @date 2020/8/12 0012 17:55
 */
public interface MySqlStudentDao
{
    List<Map<String, Object>> getAllStudents();
}
