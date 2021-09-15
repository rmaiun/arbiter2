use arbiter;

create table if not exists user_realm_role
(
    user      bigint not null,
    realm     bigint not null,
    role      bigint not null,
    bot_usage boolean default false,
    PRIMARY KEY (user, realm, role)
);

create index user_realm_role_id_idx on user_realm_role (realm, user, role);