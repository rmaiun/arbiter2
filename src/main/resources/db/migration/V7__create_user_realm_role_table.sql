use arbiter;

create table if not exists user_realm_role
(
    user      bigint not null,
    realm     bigint not null,
    role      bigint not null,
    bot_usage boolean default false,

    primary key (user, realm, role),

    foreign key urr_fk_user (user) references user (id),
    foreign key urr_fk_realm (realm) references realm (id),
    foreign key urr_fk_role (role) references role (id)
);

create index user_realm_role_id_idx on user_realm_role (realm, user, role);