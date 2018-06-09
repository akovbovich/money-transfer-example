# --- !Ups

insert into "account_balance" values(1,100);
insert into "account_balance" values(2,100);
insert into "account_balance" values(3,  0);

# --- !Downs

delete from table "account_balance";
