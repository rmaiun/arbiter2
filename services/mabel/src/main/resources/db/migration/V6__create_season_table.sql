use arbiter;

create table if not exists season
(
    id               bigint primary key auto_increment,
    name             varchar(20) not null unique,
    algorithm        bigint      not null,
    realm bigint not null,
    end_notification timestamp,
    foreign key season_fk_realm (realm) references realm (id)
);

create index season_id_idx on season (id);
create index season_name_idx on season (name);
create index season_name_realm_idx on season (name, realm);
