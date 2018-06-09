# --- !Ups

create table "account_balance" (
  "account_id" bigint not null primary key,
  "balance" bigint not null check ("balance" >= 0)
);

# --- !Downs

drop table "account_balance" if exists;
