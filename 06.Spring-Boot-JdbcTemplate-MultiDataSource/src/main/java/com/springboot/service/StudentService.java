package com.springboot.service;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentService
 *
 * @author hbl
 * @date 2020/8/12 0012 18:06
 */
public interface StudentService
{
    List<Map<String, Object>> getAllStudentsFromMySQL();
    List<Map<String, Object>> getAllStudentsFromOracle();
}
