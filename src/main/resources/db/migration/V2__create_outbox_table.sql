create table outbox
(
    retry_count  integer not null,
    created_at   timestamp(6),
    published_at timestamp(6),
    id           uuid    not null,
    event_name   varchar(255),
    last_error   varchar(255),
    payload      TEXT,
    status       varchar(255) check (status in ('PENDING', 'PUBLISHED', 'FAILED')),
    primary key (id)
);
