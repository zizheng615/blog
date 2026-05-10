package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {

    @Select("SELECT COUNT(DISTINCT ip_address) FROM visitor WHERE visit_date = #{date}")
    Long selectUvCountByDate(@Param("date") LocalDate date);

    @Select("SELECT COUNT(*) FROM visitor WHERE visit_date = #{date}")
    Long selectPvCountByDate(@Param("date") LocalDate date);

    @Select("SELECT COUNT(DISTINCT ip_address) FROM visitor")
    Long selectTotalUv();

    @Select("SELECT COUNT(*) FROM visitor")
    Long selectTotalPv();
}
