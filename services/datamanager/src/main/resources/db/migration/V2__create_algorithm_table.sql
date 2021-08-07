use arbiter;

create table if not exists algorithm
(
    id    bigint primary key auto_increment,
    value varchar(20) not null unique
);

ALTER TABLE algorithm
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index algorithm_id_idx on algorithm (id);