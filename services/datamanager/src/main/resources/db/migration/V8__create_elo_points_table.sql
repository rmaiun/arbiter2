use arbiter;

create table if not exists elo_points
(
    id     bigint primary key auto_increment,
    user   bigint not null,
    points int    not null
);

create index elo_points_id_idx on elo_points (id);
create index elo_points_realm_season_user_idx on elo_points (user);