use arbiter;

create table if not exists realm
(
    id      bigint primary key auto_increment,
    surname varchar(20) not null,
    expected_players int not null
);

ALTER TABLE realm
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index realm_id_idx on realm (id);
