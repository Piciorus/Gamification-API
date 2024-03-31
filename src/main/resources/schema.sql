DROP EXTENSION IF EXISTS "uuid-ossp" CASCADE;
CREATE EXTENSION "uuid-ossp" SCHEMA public;

create table if not exists roles
(
    id                     uuid               not null primary key,
    name                   varchar(20) unique not null
);

create table if not exists users
(
    id                     uuid                not null primary key default public.uuid_generate_v4(),
    email                  varchar(100)        unique not null,
    password               varchar(100),
    threshold              int,
    tokens                 int,
    username               varchar(100),
    creation_date          timestamp,
    update_date            timestamp
);

create table if not exists user_roles (
    user_id     uuid,
    role_id     uuid,
    constraint  pk_user_role  primary key (user_id, role_id),
    constraint  fk_user_id    foreign key (user_id)  references users(id) on delete cascade,
    constraint  fk_role_id    foreign key (role_id)  references roles(id) on delete cascade
);

create table if not exists badges (
    id                     uuid                not null primary key default public.uuid_generate_v4(),
    name                   varchar(100),
    creation_date          timestamp           ,
    update_date            timestamp
);

create table if not exists user_badges (
    user_id     uuid,
    badge_id    uuid,
    constraint  pk_user_badges  primary key (user_id, badge_id),
    constraint  fk_user_id    foreign key (user_id)  references users(id) on delete cascade,
    constraint  fk_badge_id    foreign key (badge_id)  references badges(id) on delete cascade
);

create table if not exists quests (
    id                     uuid                not null primary key default public.uuid_generate_v4(),
    answer                 varchar(50),
    description            varchar(200),
    difficulty             varchar(50),
    quest_reward_tokens    int,
    rewarded               boolean,
    threshold              int,
    creation_date          timestamp,
    update_date            timestamp
);

create table if not exists quests_users (
    user_id     uuid,
    quest_id    uuid,
    constraint  pk_quests_users  primary key (user_id, quest_id),
    constraint  fk_user_id       foreign key (user_id)  references users(id) on delete cascade,
    constraint  fk_quest_id      foreign key (quest_id)  references quests(id) on delete cascade
);

create table if not exists categories (
    id             uuid not null primary key default public.uuid_generate_v4(),
    name           varchar(100) unique,
    creation_date  timestamp,
    update_date    timestamp
);

create table if not exists questions (
    id                     uuid not null primary key default public.uuid_generate_v4(),
    question_text          varchar(200),
    answer1                varchar(50),
    answer2                varchar(50),
    answer3                varchar(50),
    correct_answer         varchar(50),
    difficulty             varchar(50),
    quest_reward_tokens    int,
    rewarded               boolean,
    threshold              int,
    category_id            uuid,
    creation_date          timestamp,
    update_date            timestamp,
    checked_by_admin       boolean default false,
    foreign key (category_id) references categories(id) on delete cascade
);

