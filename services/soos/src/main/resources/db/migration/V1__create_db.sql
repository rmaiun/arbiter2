# create database arbiter;
ALTER DATABASE arbiter CHARACTER SET utf8 COLLATE utf8_unicode_ci;
SET GLOBAL sql_mode = (SELECT REPLACE(@@sql_mode, 'ONLY_FULL_GROUP_BY', ''));
