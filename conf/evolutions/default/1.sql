# --- !Ups

create table if not exists "account_balance" (
  "account_id" bigint not null primary key,
  "balance" bigint not null check ("balance" >= 0)
);
