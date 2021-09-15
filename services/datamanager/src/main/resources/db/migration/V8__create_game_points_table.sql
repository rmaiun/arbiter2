use arbiter;

create table if not exists game_points
(
    id     bigint primary key auto_increment,
    realm  bigint not null,
    season bigint not null,
    user   bigint not null,
    points int    not null
);

create index game_points_id_idx on game_points (id);
create index game_points_realm_season_idx on game_points (realm, season);
create index game_points_realm_season_user_idx on game_points (realm, season, user);