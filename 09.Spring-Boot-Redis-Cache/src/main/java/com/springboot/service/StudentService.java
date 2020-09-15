package com.springboot.service;

import com.springboot.bean.Student;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

/**
 * Description: StudentService
 *
 * @author hbl
 * @date 2020/6/5 0005 15:30
 */
@CacheConfig(cacheNames = "student")
public interface StudentService
{
    @CachePut(key = "#p0.sno")
    Student update(Student student);

    @CacheEvict(key = "#p0", allEntries = true)
    int deleteBySno(String sno);

    @Cacheable(key = "#p0")
    Student queryStudentBySno(String sno);
}
