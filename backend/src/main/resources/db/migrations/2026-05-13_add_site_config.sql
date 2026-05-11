-- Migration: add site_config table for editable contact info & similar settings.
-- Seeds the three contact entries shown on the public "联系我" page.

USE blog;

CREATE TABLE IF NOT EXISTS `site_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_key` VARCHAR(64) NOT NULL UNIQUE,
    `config_value` VARCHAR(512),
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `site_config` (`config_key`, `config_value`) VALUES
    ('contact_email',    '2788906816@qq.com'),
    ('contact_github',   'https://github.com/zizheng615'),
    ('contact_bilibili', 'https://space.bilibili.com/291245814')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
