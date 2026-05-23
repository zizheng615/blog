-- 为现有数据库添加性能优化索引
-- 执行方式: mysql -u root -proot blog < add_indexes.sql

-- 文章列表查询索引: 按状态+发布时间排序（最常用）
CREATE INDEX IF NOT EXISTS `idx_article_status_published` ON `article`(`status`, `published_at` DESC);

-- 分类筛选索引
CREATE INDEX IF NOT EXISTS `idx_article_category` ON `article`(`category_id`);

-- 类型筛选索引
CREATE INDEX IF NOT EXISTS `idx_article_type` ON `article`(`article_type`);

-- 标签关联表索引: 按标签查文章
CREATE INDEX IF NOT EXISTS `idx_article_tag_tag_id` ON `article_tag`(`tag_id`);
