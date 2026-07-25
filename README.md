# Error Free Text

Error Free Text — сервис для проверки текста на орфографические ошибки через Yandex.Speller.  
Проект реализован на Java 17 / Spring Boot 3 с использованием PostgreSQL и Docker.

---

## 1. Что делает сервис

<ul>
  <li>Принимает задачи на проверку текста через REST API.</li>
  <li>Поддерживает языки <b>RU</b> и <b>EN</b>.</li>
  <li>Разбивает текст на части (<code>TaskContent</code>) с учётом лимитов Yandex.Speller.</li>
  <li>Обрабатывает задачи пакетами по расписанию.</li>
  <li>Асинхронно осуществляет обработку задач(вызов по REST Yandex.Speller и дальнейшая обработка) и обновляет статусы:
    <code>CREATED → IN_PROGRESS → COMPLETED / FAILED</code>.</li>
  <li>Фиксирует ошибки обработки во вложенной сущности <code>TaskError</code>.</li>
</ul>

---

## 2. Стек технологий

<ul>
  <li>Java 17, Spring Boot 3 (Web, JOOQ, Scheduling, Async)</li>
  <li>PostgreSQL + Liquibase+ JOOQ</li>
  <li>Docker / Docker Compose</li>
  <li>JUnit 5, Mockito, Testcontainers</li>
  <li>Lombok, MapStruct</li>
</ul>

---

## 3. Кратко об архитектуре

<ul>
  <li><code>Task</code> — основная сущность задачи с полями статуса, языка и версией для optimistic locking.</li>
  <li><code>TaskContent</code> — отдельный фрагмент текста, отправляемый в Yandex.Speller.</li>
  <li><code>TaskError</code> — хранилище ошибок обработки (REST‑ошибки, конфликты версий и т.п.).</li>
</ul>

Обработка выглядит так:

<ol>
  <li><code>TaskBatchProcessor</code> выбирает порцию задач со статусом <code>CREATED</code>, переводит их в <code>IN_PROGRESS</code> и запускает асинхронную обработку.</li>
  <li><code>TaskProcessor</code> (через <code>@Async</code> и <code>CompletableFuture</code>) вызывает Yandex.Speller для каждого фрагмента текста, применяет правки и выставляет итоговый статус.</li>
  <li><code>TaskSaver</code> сохраняет изменённые задачи и, при необходимости, ошибки. При <code>Exception</code> задача переводится в <code>FAILED</code> и фиксируется отдельная ошибка.</li>
</ol>

---

# 4. Асинхронность и блокировки

<ul>
  <li>Асинхронная обработка реализована на уровне задач: для каждой <code>Task</code> создаётся <code>CompletableFuture&lt;Task&gt;</code>, который выполняется в пуле <code>taskProcessingExecutor</code>.</li>
  <li>Выборка задач для пакетной обработки выполняется под пессимистической блокировкой на уровне PostgreSQL (через SELECT ... FOR UPDATE). Это гарантирует, что один и тот же Task не попадёт параллельно в разные батчи и не будет обрабатываться конкурентно.</li>
</ul>

---

## 5. Конфигурация приложения

Ниже — краткое описание ключевых настроек из <code>application.yml</code>.

### 5.1. Spring и база данных

<ul>
  <li><code>spring.application.name = error-free-txt</code> — имя приложения.</li>
  <li><code>spring.datasource.*</code> — настройки подключения к PostgreSQL:
    <ul>
      <li><code>url = jdbc:postgresql://localhost:5432/postgres</code></li>
      <li><code>username = postgres</code>, <code>password = postgres</code></li>
      <li><code>driver-class-name = org.postgresql.Driver</code></li>
    </ul>
  </li>
  <li><code>spring.liquibase.enabled = true</code> — включены миграции схемы БД.</li>
  <li><code>spring.liquibase.change-log = classpath:db/changelog/db.changelog-master.xml</code> — главный changelog.</li>
</ul>

### 5.2. Логирование и порт

<ul>
  <li><code>server.port = 8080</code> — HTTP‑порт приложения.</li>
  <li><code>logging.level.bel.senla.errorfreetext = TRACE</code> — детальное логирование внутри приложения.</li>
</ul>

### 5.3. Бизнес‑параметры

<ul>
  <li><code>content-size = 10000</code> — максимальный размер текстового фрагмента, который отправляется в Yandex.Speller.</li>
  <li><code>yandex-url = https://speller.yandex.net/services/spellservice.json/checkTexts</code> — эндпоинт Yandex.Speller.</li>
</ul>

### 5.4. Планировщик и асинхронный исполнитель

<ul>
  <li><code>scheduler.batch-size = 10</code> — сколько задач со статусом <code>CREATED</code> берётся за один цикл.</li>
  <li><code>scheduler.interval-ms = 10000</code> — период запуска пакетной обработки (10 секунд).</li>
  <li><code>scheduler.pool-size = 1</code> — логический параметр/настройка пула для шедуллера (может использоваться в конфигурации).</li>
  <li><code>async-processor.pool-size = 5</code> — количество потоков для <code>taskProcessingExecutor</code>, на котором выполняются методы <code>@Async</code> обработки задач.</li>
  <li><code>async-processor.queue-size = 10</code> — максимальная длина очереди задач в асинхронном пуле.</li>
</ul>

---

## 6. Тесты

<ul>
  <li>Интеграционные тесты поднимают полный Spring Boot контекст.</li>
  <li>Проверяются сценарии успешной пакетной обработки и обработки с REST‑ошибками.</li>
  <li>Клиент Yandex.Speller в тестах подменяется через <code>@SpyBean</code> и <code>RestClientException</code>.</li>
</ul>

---

## 7. Автор

<ul>
  <li>Alexandr Niunko</li>
  <li>Репозиторий: <a href="https://github.com/AlexNiunko/error-free-text-senla-">github.com/AlexNiunko/error-free-text</a></li>
</ul>
