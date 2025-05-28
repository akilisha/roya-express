-- drop table if exists tbl_board;
drop table if exists tbl_comment;
drop table if exists tbl_message;
drop table if exists tbl_user;
drop table if exists tbl_board;
drop table if exists tbl_profile;
drop table if exists todos;

create table if not exists todos
(
    id        uuid    default random_uuid() primary key,
    title     varchar(64) not null,
    completed boolean default false,
    constraint uniq_title unique (title)
);

create table if not exists tbl_profile
(
    oauth_id      varchar(64)  not null primary key,
    first_name    varchar(64),
    last_name     varchar(64),
    email_address varchar(128) not null unique,
    phone_number  varchar(15),
    last_updated  timestamp    not null default now()
);

create table if not exists tbl_board
(
    board_id     uuid                 default random_uuid() primary key,
    owner_id     uuid,
    title        varchar(64) not null,
    type         varchar(10) not null default 'DM',
    date_created timestamp   not null default now(),
    last_updated timestamp   not null default now(),
    constraint unique_wall unique(owner_id, title)
);

create table if not exists tbl_user
(
    user_id       uuid        default random_uuid() primary key,
    profile_id varchar(64) not null references tbl_profile (oauth_id),
    mood          varchar(10) default 'NONE',
    status        varchar(10) default 'NONE',
    title         varchar(32),
    bio           varchar(256),
    wall_id       uuid        not null not null references tbl_board (board_id),
    last_updated  timestamp   not null default now()
);

create table if not exists tbl_message
(
    message_id uuid               default random_uuid() primary key,
    author_id  uuid      not null references tbl_user (user_id),
    content    varchar(1024),
    date_sent  timestamp not null default now()
);

create table if not exists tbl_comment
(
    comment_id     uuid                   default random_uuid() primary key,
    author_id      uuid          not null references tbl_user (user_id),
    parent_message uuid          not null references tbl_message (message_id),
    parent_comment uuid references tbl_comment (comment_id),
    content        varchar(1024) not null,
    date_sent      timestamp     not null default now()
);
