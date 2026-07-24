create table if not exists public.task
(
    id         serial not null
        constraint pk_task
            primary key,
    created_at timestamp(6),
    updated_at timestamp(6),
    language   varchar(5),
    status     varchar(20)
);


create table if not exists public.task_content
(
    id         serial not null
        constraint pk_task_content
            primary key,
    position   integer,
    created_at timestamp(6),
    updated_at timestamp(6),
    content    varchar(10000),
    task_id    serial not null
        constraint fk_task_content_task
            references public.task
);


create table if not exists public.task_error
(
    id         serial not null
        constraint pk_task_error
            primary key,
    created_at timestamp(6),
    updated_at timestamp(6),
    message    varchar(255),
    task_id    serial not null
        constraint fk_task_error_task
            references public.task
);

