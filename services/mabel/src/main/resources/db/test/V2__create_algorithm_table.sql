use arbiter;

create table if not exists algorithm
(
    id    bigint primary key auto_increment,
    value varchar(20) not null unique
);

create index algorithm_id_idx on algorithm (id);