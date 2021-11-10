# create database arbiter;
alter database arbiter character set utf8 collate utf8_unicode_ci;
SET GLOBAL sql_mode = (SELECT REPLACE(@@sql_mode, 'ONLY_FULL_GROUP_BY', ''));
