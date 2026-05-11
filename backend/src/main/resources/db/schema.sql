CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog;

-- User table (admin accounts)
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50),
    `email` VARCHAR(100),
    `avatar` VARCHAR(255),
    `role` VARCHAR(20) DEFAULT 'ADMIN',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Category table
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `slug` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255),
    `sort_order` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tag table
CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `slug` VARCHAR(50) NOT NULL UNIQUE,
    `category_id` BIGINT,
    `color` VARCHAR(7) DEFAULT '#409EFF',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article table
CREATE TABLE IF NOT EXISTS `article` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `slug` VARCHAR(200) UNIQUE,
    `summary` TEXT,
    `content` LONGTEXT NOT NULL,
    `content_md` LONGTEXT,
    `cover_image` VARCHAR(255),
    `category_id` BIGINT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `article_type` ENUM('TECH', 'LIFE') NOT NULL,
    `status` ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') DEFAULT 'DRAFT',
    `view_count` INT DEFAULT 0,
    `comment_count` INT DEFAULT 0,
    `is_top` BOOLEAN DEFAULT FALSE,
    `published_at` DATETIME,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`),
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Article-Tag junction table
CREATE TABLE IF NOT EXISTS `article_tag` (
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    PRIMARY KEY (`article_id`, `tag_id`),
    FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comment table
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100),
    `content` TEXT NOT NULL,
    `ip_address` VARCHAR(45),
    `user_agent` VARCHAR(512),
    `is_admin` BOOLEAN DEFAULT FALSE,
    `status` ENUM('PENDING', 'APPROVED', 'SPAM') DEFAULT 'APPROVED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`article_id`) REFERENCES `article`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`parent_id`) REFERENCES `comment`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Visitor table
CREATE TABLE IF NOT EXISTS `visitor` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `ip_address` VARCHAR(45) NOT NULL,
    `user_agent` VARCHAR(512),
    `page_url` VARCHAR(512),
    `referer` VARCHAR(512),
    `visit_date` DATE NOT NULL,
    `visit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_ip_date` (`ip_address`, `visit_date`),
    INDEX `idx_visit_date` (`visit_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Daily visit stats table
CREATE TABLE IF NOT EXISTS `daily_visit` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `visit_date` DATE NOT NULL UNIQUE,
    `pv_count` INT DEFAULT 0,
    `uv_count` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Friend link table
CREATE TABLE IF NOT EXISTS `friend_link` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `url` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255),
    `icon` VARCHAR(255),
    `sort_order` INT DEFAULT 0,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Site config table (contact info, etc.)
CREATE TABLE IF NOT EXISTS `site_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_key` VARCHAR(64) NOT NULL UNIQUE,
    `config_value` VARCHAR(512),
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert seed data

-- Admin user (password: admin123, BCrypt encoded)
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `role`) VALUES
('admin', '$2b$10$Fq4L5QZyEO.E6E/KmPc0he3xTwvIMKpI/Dgt135B/Lt5QD7zrNSSi', '博主', 'admin@blog.com', 'ADMIN');

-- Categories
INSERT INTO `category` (`name`, `slug`, `description`, `sort_order`) VALUES
('技术文章', 'tech', '编程技术、开发经验、工具分享', 1),
('生活分享', 'life', '日常随笔、旅行记录、读书心得', 2);

-- Tags
INSERT INTO `tag` (`name`, `slug`, `category_id`, `color`) VALUES
('Spring Boot', 'spring-boot', 1, '#6DB33F'),
('Java', 'java', 1, '#007396'),
('Vue.js', 'vuejs', 1, '#4FC08D'),
('MySQL', 'mysql', 1, '#4479A1'),
('前端开发', 'frontend', 1, '#FF6B6B'),
('后端开发', 'backend', 1, '#4ECDC4'),
('微服务', 'microservices', 1, '#95E1D3'),
('读书笔记', 'reading', 2, '#F38181'),
('旅行', 'travel', 2, '#AA96DA'),
('美食', 'food', 2, '#FCBAD3'),
('摄影', 'photography', 2, '#FFFFD2'),
('随笔', 'essay', 2, '#A8D8EA');

-- Tech Articles
INSERT INTO `article` (`title`, `slug`, `summary`, `content`, `category_id`, `author_id`, `article_type`, `status`, `view_count`, `comment_count`, `is_top`, `published_at`) VALUES
('Spring Boot 3 新特性详解', 'spring-boot-3-features',
'Spring Boot 3 基于 Spring Framework 6，需要 Java 17+，带来了许多激动人心的新特性，包括原生镜像支持、 Jakarta EE 9 迁移等。',
'<h2>Spring Boot 3 概述</h2><p>Spring Boot 3 是基于 Spring Framework 6 的全新版本，于 2022 年 11 月正式发布。这个版本带来了许多重要的变化和新的特性。</p><h3>主要变化</h3><ul><li>最低 Java 版本要求提升至 Java 17</li><li>从 Java EE 迁移到 Jakarta EE 9</li><li>原生镜像支持（GraalVM）</li><li>改进的可观察性</li></ul><h3>代码示例</h3><pre><code class="language-java">@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}</code></pre><p>以上就是 Spring Boot 3 的基础入门，更多高级特性敬请期待后续文章。</p>',
1, 1, 'TECH', 'PUBLISHED', 128, 3, TRUE, '2024-01-15 10:00:00'),

('Vue 3 Composition API 实践指南', 'vue3-composition-api',
'Composition API 是 Vue 3 最重要的新特性之一，它提供了一种更灵活、更强大的组织组件逻辑的方式。',
'<h2>什么是 Composition API</h2><p>Composition API 是 Vue 3 引入的一组 API，允许我们使用导入的函数而不是声明选项来编写 Vue 组件。</p><h3>基本用法</h3><pre><code class="language-javascript">import { ref, computed, onMounted } from ''vue''

export default {
  setup() {
    const count = ref(0)
    const double = computed(() => count.value * 2)

    onMounted(() => {
      console.log(''组件已挂载'')
    })

    return { count, double }
  }
}</code></pre><p>通过组合式函数，我们可以更好地复用逻辑。</p>',
1, 1, 'TECH', 'PUBLISHED', 96, 2, FALSE, '2024-02-20 14:30:00'),

('MySQL 性能优化实战', 'mysql-performance-optimization',
'数据库性能优化是后端开发的核心技能之一，本文分享一些常用的 MySQL 优化技巧和实践经验。',
'<h2>索引优化</h2><p>索引是数据库查询优化的基础。合理的索引设计可以大幅提升查询性能。</p><h3>索引设计原则</h3><ul><li>为 WHERE、JOIN、ORDER BY 子句中的列建立索引</li><li>选择性高的列优先</li><li>避免冗余索引</li><li>使用覆盖索引减少回表</li></ul><h3>查询优化示例</h3><pre><code class="language-sql">-- 使用 EXPLAIN 分析查询
EXPLAIN SELECT * FROM article WHERE category_id = 1;

-- 优化后的查询
SELECT id, title, summary FROM article
WHERE category_id = 1 AND status = ''PUBLISHED''
ORDER BY published_at DESC LIMIT 10;</code></pre>',
1, 1, 'TECH', 'PUBLISHED', 75, 1, FALSE, '2024-03-10 09:00:00'),

('Spring Security + JWT 实现认证授权', 'spring-security-jwt',
'Spring Security 是 Spring 生态中强大的安全框架，结合 JWT 可以实现无状态的认证授权机制。',
'<h2>Spring Security 简介</h2><p>Spring Security 是一个功能强大且高度可定制的身份验证和访问控制框架，是保护基于 Spring 的应用的事实标准。</p><h3>JWT 认证流程</h3><ol><li>用户登录，验证用户名密码</li><li>服务器生成 JWT Token 返回给客户端</li><li>客户端后续请求携带 Token</li><li>服务器验证 Token 有效性</li></ol><pre><code class="language-java">@Component
public class JwtTokenProvider {

    public String createToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact();
    }
}</code></pre>',
1, 1, 'TECH', 'PUBLISHED', 156, 4, TRUE, '2024-04-05 16:00:00'),

('Docker 容器化部署实践', 'docker-deployment',
'Docker 让应用部署变得简单高效，本文介绍如何将 Spring Boot 应用容器化并部署到生产环境。',
'<h2>Docker 基础</h2><p>Docker 是一个开源的应用容器引擎，让开发者可以打包应用及其依赖到一个可移植的容器中。</p><h3>Dockerfile 示例</h3><pre><code class="language-dockerfile">FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]</code></pre><h3>常用命令</h3><pre><code class="language-bash"># 构建镜像
docker build -t blog-backend .

# 运行容器
docker run -p 8080:8080 blog-backend</code></pre>',
1, 1, 'TECH', 'PUBLISHED', 89, 2, FALSE, '2024-05-12 11:00:00');

-- Life Articles
INSERT INTO `article` (`title`, `slug`, `summary`, `content`, `category_id`, `author_id`, `article_type`, `status`, `view_count`, `comment_count`, `is_top`, `published_at`) VALUES
('春日漫步：寻找城市的绿意', 'spring-walk',
'春天来了，走出家门，在城市的角落里寻找那一抹绿色，感受生命的律动。',
'<div style="text-align: center; margin: 30px 0;"><p>🌿 春日的阳光温柔地洒在肩头</p></div><p>清晨，我独自走在公园的小径上。空气中弥漫着青草和泥土的芬芳，让人不由自主地深吸一口气。</p><p>路边的樱花已经盛开，粉色的花瓣随风飘落，像是在下一场温柔的花雨。我坐在长椅上，看着晨练的老人、嬉戏的孩子，感受着这座城市最朴素的温暖。</p><div style="text-align: center; margin: 30px 0;"><p>🌸 花开时节，与美好不期而遇</p></div><p>生活不必总是匆忙，偶尔停下脚步，才能发现那些被忽略的美好。</p>',
2, 1, 'LIFE', 'PUBLISHED', 67, 2, FALSE, '2024-03-25 08:00:00'),

('深夜食堂：一碗拉面的温暖', 'midnight-ramen',
'加班到深夜，街角的小面馆依然亮着灯。一碗热气腾腾的拉面，温暖了疲惫的身心。',
'<p>城市的夜晚总是灯火通明，但最温暖的，莫过于街角那盏为夜归人亮着的灯。</p><div style="text-align: center; margin: 30px 0;"><p>🍜 深夜的拉面，是最简单的幸福</p></div><p>推开门，暖黄色的灯光映入眼帘。老板微笑着问："还是老样子？"我点点头，找了个靠窗的位置坐下。</p><p>不一会儿，一碗热气腾腾的拉面端了上来。浓郁的汤底、劲道的面条、溏心蛋、叉烧肉、葱花... 简单的食材，却组成了最治愈的味道。</p><p>在这个快节奏的城市里，这样的小店就像一个小小的港湾，让每一个疲惫的灵魂都能找到归属感。</p>',
2, 1, 'LIFE', 'PUBLISHED', 45, 1, FALSE, '2024-04-18 22:00:00'),

('读《人类简史》：思考的力量', 'sapiens-reading',
'尤瓦尔·赫拉利的《人类简史》让我重新审视人类文明的发展，思维的边界远比想象中更广阔。',
'<p>用了两周的时间读完了《人类简史》，这本书带给我太多震撼和思考。</p><h3>认知革命</h3><p>赫拉利提出，人类之所以能从众多物种中脱颖而出，关键在于我们拥有"想象"的能力。我们能够相信一些不存在的事物——国家、宗教、货币、公司... 这些"虚构的故事"让人类能够大规模协作。</p><div style="text-align: center; margin: 30px 0;"><p>📚 阅读，是与伟大思想的对话</p></div><h3>农业革命</h3><p>农业革命被作者称为"史上最大的骗局"。人类从狩猎采集到农耕定居，看似获得了稳定的食物来源，实则付出了更多的劳动和更不均衡的营养。</p><p>这本书让我学会了用更宏观、更批判的视角看待历史和我们所处的世界。</p>',
2, 1, 'LIFE', 'PUBLISHED', 38, 1, FALSE, '2024-05-01 15:00:00'),

('雨天的周末：一本好书一杯茶', 'rainy-weekend',
'窗外细雨绵绵，屋内茶香袅袅。这样的周末，最适合窝在沙发里，读本好书。',
'<p>下雨天，世界仿佛被按下了静音键。</p><div style="text-align: center; margin: 30px 0;"><p>🌧️ 听雨，是最高级的独处</p></div><p>泡一壶普洱，翻开新买的书。窗外的雨声成了最自然的白噪音，让思绪更加沉静。</p><p>偶尔抬头看看窗外的梧桐，雨滴顺着叶脉滑落，像是大自然最温柔的眼泪。</p><p>这样的时刻，不需要太多言语，只需要和自己好好相处。</p>',
2, 1, 'LIFE', 'PUBLISHED', 52, 0, FALSE, '2024-05-08 10:00:00');

-- Article-Tag relationships
INSERT INTO `article_tag` (`article_id`, `tag_id`) VALUES
(1, 1), (1, 2), (1, 6),
(2, 3), (2, 5),
(3, 4), (3, 6),
(4, 1), (4, 2), (4, 7),
(5, 2), (5, 6), (5, 7),
(6, 9), (6, 12),
(7, 10), (7, 12),
(8, 8), (8, 12),
(9, 9), (9, 11), (9, 12);

-- Comments
INSERT INTO `comment` (`article_id`, `nickname`, `email`, `content`, `status`, `is_admin`) VALUES
(1, '小明', 'xiaoming@example.com', '写得太好了！Spring Boot 3 的原生镜像支持确实很值得期待 👍', 'APPROVED', FALSE),
(1, '开发者小李', 'devli@example.com', '请问博主有计划写 Spring Cloud 的教程吗？期待~', 'APPROVED', FALSE),
(1, '博主', 'admin@blog.com', '谢谢支持！Spring Cloud 系列正在计划中 😊', 'APPROVED', TRUE),
(2, '前端小王', 'fe@example.com', 'Composition API 确实比 Options API 更灵活，已经在项目里用上了', 'APPROVED', FALSE),
(4, 'Java程序员', 'java@example.com', 'JWT + Spring Security 的经典组合，安全性很高！', 'APPROVED', FALSE),
(4, '安全专家', 'sec@example.com', '建议补充一下 Token 刷新机制的实现细节', 'APPROVED', FALSE),
(6, '旅行者', 'travel@example.com', '春天的公园确实很美 🌸 喜欢这种慢节奏的生活方式', 'APPROVED', FALSE),
(6, '吃货一枚', 'food@example.com', '拉面看着就好吃！求地址 😋', 'APPROVED', FALSE);

-- Friend Links
INSERT INTO `friend_link` (`name`, `url`, `description`, `icon`, `sort_order`, `is_active`) VALUES
('GitHub', 'https://github.com/zizheng615', '代码托管平台', 'github', 1, TRUE),
('哔哩哔哩', 'https://space.bilibili.com/291245814', '年轻人的文化社区', 'bilibili', 2, TRUE),
('Spring 官方', 'https://spring.io', 'Spring Framework 官方网站', 'spring', 3, TRUE),
('Vue.js', 'https://vuejs.org', '渐进式 JavaScript 框架', 'vue', 4, TRUE),
('MDN Web 文档', 'https://developer.mozilla.org', 'Web 开发者的权威参考', 'mdn', 5, TRUE);

-- Site Config (default contact info)
INSERT INTO `site_config` (`config_key`, `config_value`) VALUES
('contact_email', '2788906816@qq.com'),
('contact_github', 'https://github.com/zizheng615'),
('contact_bilibili', 'https://space.bilibili.com/291245814');
