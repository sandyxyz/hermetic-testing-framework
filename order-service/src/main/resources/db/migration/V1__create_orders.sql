create table orders (
    id bigint primary key auto_increment,
    user_id varchar(100) not null,
    amount decimal(12, 2) not null,
    status varchar(40) not null,
    payment_id varchar(100),
    created_at timestamp not null default current_timestamp
);
