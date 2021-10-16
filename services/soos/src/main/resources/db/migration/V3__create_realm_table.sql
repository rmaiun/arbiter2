use arbiter;

create table if not exists realm
(
    id                 bigint primary key auto_increment,
    name               varchar(20) not null unique,
    team_size          int         not null,
    selected_algorithm bigint      not null,
    foreign key fk_algorithm (selected_algorithm) references algorithm (id)
);

create index realm_id_idx on realm (id);
