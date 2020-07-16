package com.springboot.service;

import java.util.List;
import java.util.Map;

/**
 * Description: StudentService
 *
 * @author hbl
 * @date 2020/7/16 0016 15:19
 */
public interface StudentService
{
    List<Map<String, Object>> getAllStudentsFromMysql();

    List<Map<String, Object>> getAllStudentsFromOracle();
}
