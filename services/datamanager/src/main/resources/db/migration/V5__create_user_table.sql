use arbiter;

create table if not exists user
(
    id         bigint primary key auto_increment,
    surname    varchar(20) not null unique,
    nickname   varchar(20) default null unique,
    tid        bigint      default null unique,
    active     boolean     default true,
    created_at timestamp   not null
);

ALTER TABLE user
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index user_id_idx on user (id);
create index user_name_idx on user (surname);
create index user_nickname_idx on user (nickname);
