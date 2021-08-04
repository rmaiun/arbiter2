use arbiter;

create table if not exists season
(
    id   bigint primary key auto_increment,
    name varchar(20) not null
);

ALTER TABLE season
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index season_id_idx on season (id);
create index season_name_idx on season (name);