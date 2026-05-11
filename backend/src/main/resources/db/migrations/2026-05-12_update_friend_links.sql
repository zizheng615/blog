-- Migration: point the GitHub and Bilibili friend links to the owner's
-- actual profiles instead of the generic homepages that shipped in the seed.

USE blog;

UPDATE `friend_link`
SET `url` = 'https://github.com/zizheng615'
WHERE `name` = 'GitHub';

UPDATE `friend_link`
SET `url` = 'https://space.bilibili.com/291245814'
WHERE `name` = '哔哩哔哩';
