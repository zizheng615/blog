package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT a.*, c.name as category_name, c.slug as category_slug FROM article a LEFT JOIN category c ON a.category_id = c.id WHERE a.id = #{id}")
    Article selectByIdWithCategory(@Param("id") Long id);

    Page<Article> selectPageWithCategory(Page<Article> page,
                                         @Param("categoryId") Long categoryId,
                                         @Param("tagId") Long tagId,
                                         @Param("articleType") String articleType,
                                         @Param("status") String status);

    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Select("SELECT a.* FROM article a INNER JOIN article_tag at ON a.id = at.article_id WHERE at.tag_id = #{tagId} AND a.status = 'PUBLISHED' ORDER BY a.is_top DESC, COALESCE(a.published_at, a.created_at) DESC")
    List<Article> selectByTagId(@Param("tagId") Long tagId);
}
