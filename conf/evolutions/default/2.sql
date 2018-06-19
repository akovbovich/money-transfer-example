# --- !Ups

insert into "account_balance" values(1,10000);
insert into "account_balance" values(2,10000);
insert into "account_balance" values(3,    0);

# --- !Downs

delete from "account_balance" where account_id in (1,2,3);
