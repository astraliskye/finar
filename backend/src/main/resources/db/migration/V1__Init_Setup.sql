create sequence users_seq start with 1 increment by 50;
create table game_results (id bigint not null, moves varchar(255) not null, player1 varchar(255) not null, player2 varchar(255) not null, result varchar(255) not null check (result in ('FINAR','ABORT','ABANDON','TIMEOUT','DRAW')), winner varchar(255) not null, primary key (id));
create table users (id bigint not null, email varchar(255) not null unique, password varchar(255) not null, profile_picture_url varchar(255), username varchar(255) not null unique, primary key (id));
