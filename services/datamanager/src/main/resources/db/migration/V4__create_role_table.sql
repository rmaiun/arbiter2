use arbiter;

create table if not exists role
(
    id         bigint primary key auto_increment,
    value      varchar(20) not null unique,
    permission int         not null
);

ALTER TABLE role
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index role_id_idx on role (id);
