use arbiter;

create table if not exists game_history
(
    id         bigint primary key auto_increment,
    realm      bigint    not null,
    season     bigint    not null,
    w1         bigint    not null,
    w2         bigint    not null,
    l1         bigint    not null,
    l2         bigint    not null,
    shutout    boolean default null,
    created_at timestamp not null,

    foreign key fk_realm (realm) references realm (id),
    foreign key fk_season (season) references season (id),
    foreign key fk_w1 (w1) references user (id),
    foreign key fk_w2 (w2) references user (id),
    foreign key fk_l1 (l1) references user (id),
    foreign key fk_l2 (l2) references user (id)
);

ALTER TABLE game_history
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index game_history_id_idx on game_history (id);
create index game_history_realm_season_idx on game_history (realm, season);
create index game_history_realm_created_idx on game_history (realm, created_at);