create table payments (
    payment_id varchar(100) primary key,
    order_id bigint not null,
    amount decimal(12, 2) not null,
    status varchar(40) not null,
    created_at timestamp not null default current_timestamp
);
