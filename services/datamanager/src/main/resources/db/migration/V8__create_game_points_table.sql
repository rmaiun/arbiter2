use arbiter;

create table if not exists game_points
(
    id     bigint primary key auto_increment,
    realm  bigint not null,
    season bigint not null,
    user   bigint not null,
    points int    not null
);

ALTER TABLE game_points
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index game_points_id_idx on game_points (id);
create index game_points_realm_season_idx on game_points (realm, season);
create index game_points_realm_season_user_idx on game_points (realm, season, user);