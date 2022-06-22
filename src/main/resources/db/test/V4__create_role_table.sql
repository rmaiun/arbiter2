use arbiter;

create table if not exists role
(
    id         bigint primary key auto_increment,
    value      varchar(20) not null unique,
    permission int         not null
);

create index role_id_idx on role (id);
