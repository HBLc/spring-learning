package com.springboot.mapper;

import com.springboot.bean.Student;
import org.apache.ibatis.annotations.Param;

/**
 * Description: StudentMapper
 *
 * @author hbl
 * @date 2020/6/4 0004 16:46
 */
// @Mapper
// @Component
// public interface StudentMapper
// {
//     @Insert("insert into student(sno,sname,ssex) values(#{sno}, #{name}, #{sex})")
//     int add(Student student);
//
//     @Update("update student set sname = #{name}, ssex = #{sex} where sno = #{sno}")
//     int update(Student student);
//
//     @Delete("delete from student where sno = #{sno}")
//     int deleteBySno(String sno);
//
//     @Select("select * from student where sno = #{sno}")
//     @Results(id = "student", value = {
//             @Result(property = "sno", column = "sno", javaType = String.class),
//             @Result(property = "name", column = "name", javaType = String.class),
//             @Result(property = "sex", column = "sex", javaType = String.class)
//     })
//     Student queryStudentBySno(String sno);
// }
public interface StudentMapper
{
    Student queryStudentBySno(@Param("sno") String sno);
}
