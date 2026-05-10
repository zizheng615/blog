package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM comment WHERE article_id = #{articleId} AND status = 'APPROVED' AND parent_id IS NULL ORDER BY created_at DESC")
    List<Comment> selectRootCommentsByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND status = 'APPROVED' ORDER BY created_at ASC")
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId);

    @Select("SELECT c.*, a.title as article_title FROM comment c LEFT JOIN article a ON c.article_id = a.id ORDER BY c.created_at DESC")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "article_id", property = "articleId"),
        @Result(column = "parent_id", property = "parentId"),
        @Result(column = "nickname", property = "nickname"),
        @Result(column = "email", property = "email"),
        @Result(column = "content", property = "content"),
        @Result(column = "ip_address", property = "ipAddress"),
        @Result(column = "user_agent", property = "userAgent"),
        @Result(column = "is_admin", property = "isAdmin"),
        @Result(column = "status", property = "status"),
        @Result(column = "created_at", property = "createdAt"),
        @Result(column = "article_title", property = "articleTitle")
    })
    List<Comment> selectAllWithArticleInfo();
}
