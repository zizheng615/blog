package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("SELECT t.*, COUNT(a.id) as articleCount FROM tag t LEFT JOIN article_tag at ON t.id = at.tag_id LEFT JOIN article a ON at.article_id = a.id AND a.status = 'PUBLISHED' GROUP BY t.id")
    List<Tag> selectAllWithCount();

    @Select("SELECT t.* FROM tag t INNER JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id = #{articleId}")
    List<Tag> selectByArticleId(@Param("articleId") Long articleId);

    @Select({
        "<script>",
        "SELECT t.*, at.article_id as articleId FROM tag t INNER JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id IN ",
        "<foreach collection='articleIds' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "</script>"
    })
    List<Tag> selectByArticleIds(@Param("articleIds") List<Long> articleIds);
}
