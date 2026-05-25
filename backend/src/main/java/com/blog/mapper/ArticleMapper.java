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

    /**
     * 列表专用分页查询：排除 content/content_md 大字段，减少网络传输和内存占用。
     * tag 过滤使用 INNER JOIN 替代 EXISTS 子查询，大数据量时性能更好。
     */
    Page<com.blog.vo.ArticleSummary> selectPageSummary(Page<com.blog.vo.ArticleSummary> page,
                                                        @Param("categoryId") Long categoryId,
                                                        @Param("tagId") Long tagId,
                                                        @Param("articleType") String articleType,
                                                        @Param("status") String status);

    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Select("SELECT a.* FROM article a INNER JOIN article_tag at ON a.id = at.article_id WHERE at.tag_id = #{tagId} AND a.status = 'PUBLISHED' ORDER BY a.published_at DESC")
    List<Article> selectByTagId(@Param("tagId") Long tagId);
}
