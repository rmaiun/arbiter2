use arbiter;

create table if not exists user
(
    id         bigint primary key auto_increment,
    surname    varchar(20) not null unique,
    nickname   varchar(20) default null unique,
    tid        bigint      default null unique,
    active     boolean     default true,
    created_at timestamp   not null
) CHARACTER SET utf8
  COLLATE utf8_general_ci;

ALTER TABLE user
    CONVERT TO CHARACTER SET utf8 COLLATE utf8_unicode_ci;

create index user_id_idx on user (id);
create index user_name_idx on user (surname);
create index user_nickname_idx on user (nickname);
create index user_tid_idx on user (tid);
create index user_name_active_idx on user (surname, active);
create index user_nickname_active_idx on user (nickname, active);
