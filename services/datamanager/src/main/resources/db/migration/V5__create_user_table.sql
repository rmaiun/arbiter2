use arbiter;

create table if not exists user
(
    id         bigint primary key auto_increment,
    name       varchar(20) not null,
    nickname   varchar(20) default null,
    tid        bigint      default null,
    active     boolean     default true,
    created_at timestamp   not null
);

ALTER TABLE user
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index user_id_idx on user (id);
create index user_name_idx on user (name);
create index user_nickname_idx on user (nickname);
