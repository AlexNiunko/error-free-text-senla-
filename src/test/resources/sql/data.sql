-- tasks
TRUNCATE TABLE public.task CASCADE;

insert into public.task (id, created_at, updated_at, language, status)
values (1, now(), now(), 'RU', 'CREATED'),
       (2, now(), now(), 'RU', 'IN_PROGRESS'),
       (3, now(), now(), 'EN', 'COMPLETED'),
       (4, now(), now(), 'RU', 'FAILED'),
       (5, now(), now(), 'EN', 'CREATED');

SELECT SETVAL('task_id_seq', (SELECT MAX(id) from public.task));

-- task_content
insert into public.task_content (id, position, created_at, updated_at, content, is_correct, task_id)
values (1, 1, now(), now(), 'Первая задача: текст для проверки орфографии.', false, 1),
       (2, 2, now(), now(), 'Вторая часть текста первой задачи.', false, 1),

       (3, 1, now(), now(), 'Контент для задачи в обработке.', false, 2),

       (4, 1, now(), now(), 'Completed task content part one.', false, 3),
       (5, 2, now(), now(), 'Completed task content part two.', false, 3),

       (6, 1, now(), now(), 'Текст задачи, при обработке которой произошла ошибка.', false, 4),

       (7, 1, now(), now(), 'Some english text to be checked.', false, 5);

SELECT SETVAL('task_content_id_seq', (SELECT MAX(id) from public.task_content));

-- task_error
insert into public.task_error (id, created_at, updated_at, message, task_id)
values (1, now(), now(), 'Yandex Speller timeout', 4),
       (2, now(), now(), 'Invalid response format from external API', 4);

SELECT SETVAL('task_error_id_seq', (SELECT MAX(id) from public.task_error));
