-- Migration: widen user_agent / page_url / referer columns to accommodate
-- long WeChat in-app browser user agents and shared URLs (with WeChat's
-- appended `?from=...&isappinstalled=...` parameters), which exceeded
-- VARCHAR(255) and caused INSERT to fail with `Data too long for column`.
--
-- Apply against existing prod databases that were initialized before
-- the schema.sql change.

USE blog;

ALTER TABLE `visitor`
    MODIFY COLUMN `user_agent` VARCHAR(512),
    MODIFY COLUMN `page_url`   VARCHAR(512),
    MODIFY COLUMN `referer`    VARCHAR(512);

ALTER TABLE `comment`
    MODIFY COLUMN `user_agent` VARCHAR(512);
