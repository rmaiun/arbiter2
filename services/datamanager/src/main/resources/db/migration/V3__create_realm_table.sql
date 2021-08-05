use arbiter;

create table if not exists realm
(
    id                 bigint primary key auto_increment,
    name            varchar(20) not null,
    team_size          int         not null,
    selected_algorithm bigint      not null,
    foreign key fk_algorithm (selected_algorithm) references algorithm (id)
);

ALTER TABLE realm
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index realm_id_idx on realm (id);
