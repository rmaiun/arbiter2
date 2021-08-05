use arbiter;

create table if not exists user_realm_role
(
    realm bigint not null,
    user  bigint not null,
    role  bigint not null,
    PRIMARY KEY (realm, user, role)
);

ALTER TABLE user_realm_role
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

create index user_realm_role_id_idx on user_realm_role (realm, user, role);