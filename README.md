# Электронная библиотека Library Manager

Сервис предназначен для учета книг в библиотеке, управления их выдачей читателям и отслеживания возвратов. Проект реализует REST API для взаимодействия с библиотечной системой, поддерживая операции с книгами, библиотечными экземплярами, читателями и займами.

---

## Сценарий использования

1. **Добавление книги**: Библиотекарь добавляет новую книгу в систему, указывая её название, автора, ISBN и год публикации.
2. **Создание библиотечного экземпляра**: Для каждой книги создается библиотечный экземпляр с указанием общего и доступного количества копий.
3. **Регистрация читателя**: Пользователь регистрируется в системе как читатель, указывая имя, email и дату регистрации.
4. **Выдача книги**: Читатель запрашивает книгу, создается займ, если есть доступные экземпляры. Количество доступных копий уменьшается.
5. **Возврат книги**: Читатель возвращает книгу, обновляется статус займа и увеличивается количество доступных копий.
6. **Получение списка займов**: Система предоставляет список займов для конкретного читателя с поддержкой пагинации.

---

## Схема связей таблиц в базе данных

![Database Schema](DB%20diagram.png)

- **book**: Хранит информацию о книгах (id, title, author, isbn, published_year).
- **library_item**: Хранит экземпляры книг (id, book_id, total_copies, available_copies), ссылается на `book` через `book_id`.
- **reader**: Хранит информацию о читателях (id, name, email, registered_at).
- **loan**: Хранит информацию о займах (id, reader_id, library_item_id, loan_date, return_date), ссылается на `reader` и `library_item` через `reader_id` и `library_item_id`.

Связи:
- `library_item.book_id` → `book.id` (один ко многим).
- `loan.reader_id` → `reader.id` (многие к одному).
- `loan.library_item_id` → `library_item.id` (многие к одному).

---

## Используемые технологии

- **Spring Boot**: Основной фреймворк для создания REST API.
- **Spring Data JPA / Hibernate**: Для работы с базой данных и маппинга сущностей.
- **PostgreSQL**: Реляционная база данных для хранения данных.
- **Mockito**: Для написания модульных тестов.
- **ByteBuddy**: Для поддержки inline mock maker в тестах.
- **Kotlin**: Основной язык программирования.
- **Gradle**: Система сборки проекта.
- **Docker**: Для контейнеризации приложения и базы данных.
- **Jackson**: Для сериализации/десериализации JSON.

---

## Запуск проекта

Запуск возможен с использованием переменных окружения и параметров конфигурации:

- `SPRING_DATASOURCE_URL` (`-Dspring.datasource.url`): **URL базы данных**. По умолчанию: `jdbc:postgresql://localhost:5432/library_manager`.
- `SPRING_DATASOURCE_USERNAME` (`-Dspring.datasource.username`): **Имя пользователя базы данных**. По умолчанию: `postgres`.
- `SPRING_DATASOURCE_PASSWORD` (`-Dspring.datasource.password`): **Пароль базы данных**. По умолчанию: пусто.
- `SERVER_PORT` (`-Dserver.port`): **Порт сервера**. По умолчанию: `8080`.
- `LOGGING_LEVEL` (`-Dlogging.level`): **Уровень логирования**. По умолчанию: `INFO`.

### Пример запуска с Docker

1. **Сборка Docker-образа**:
   ```bash
   ./gradlew build
   docker build -t library-manager .

2. **Запуск с Docker Compose**:
   Создай файл `docker-compose.yml`:
   ```yaml
   version: '3.8'
   services:
     postgres:
       image: postgres:latest
       environment:
         POSTGRES_DB: library_manager
         POSTGRES_USER: postgres
         POSTGRES_PASSWORD: password
       ports:
         - "5432:5432"
     app:
       image: library-manager
       depends_on:
         - postgres
       environment:
         SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/library_manager
         SPRING_DATASOURCE_USERNAME: postgres
         SPRING_DATASOURCE_PASSWORD: password
         SERVER_PORT: 8080
       ports:
         - "8080:8080"
   ```
   Запусти:
   ```bash
   docker-compose up
   ```

3. **Доступ к API**:
  - API доступно по адресу `http://localhost:8080`.
  - Документация API: [HTTP API](./API.md).

---

## Особенности проекта и решенные сложности

Проект является учебным, поэтому в процессе разработки были решены следующие задачи и преодолены сложности:

1. **Обработка исключений**:
  - Реализован `GlobalExceptionHandler` для единообразной обработки ошибок (`ResponseStatusException`, `ResourceNotFoundCustomException`, `DuplicateIsbnCustomException`, `DuplicateEmailCustomException`).

2. **Тестирование с Mockito и ByteBuddy**:
  - Использован Mockito для модульного тестирования контроллеров и сервисов.

3. **Управление транзакциями**:
  - Использована аннотация `@Transactional` в необходимых методах слоя `Service` для атомарного обновления или сохранения данных.
  - **Проблема**: Возможные гонки при одновременных запросах на выдачу книг. **Решение**: Hibernate автоматически управляет транзакциями, а внешние ключи в базе данных обеспечивают целостность.

4. **Пагинация в API**:
  - Реализована пагинация в методах `get` с использованием `Pageable` для возврата списка.

5. **Логирование и отладка**:
  - Настроено логирование SQL-запросов и HTTP-запросов в `application.properties`:
    ```properties
    logging.level.org.springframework.web=DEBUG
    logging.level.org.hibernate.SQL=DEBUG
    logging.level.org.hibernate.type.descriptor.sql=TRACE
    ```
  - Это значительно упростило отладку тестов и проверку корректности запросов.

6. **Валидация данных**:
  - Реализованы проверки на уникальность ISBN и email через `DuplicateIsbnCustomException` и `DuplicateEmailCustomException`.
  - **Проблема**: Одинаковая обработка ошибок валидации для разных полей. **Решение**: В `GlobalExceptionHandler` добавлена обработка `MethodArgumentNotValidException` для возврата ошибок валидации в формате `Map<String, String>`.

---

## TODO

- **OpenAPI/Swagger**: Добавить документацию API с использованием Springdoc OpenAPI.
- **Аутентификация**: Реализовать JWT-аутентификацию для защиты API.
- **Метрики и мониторинг**: Интегрировать Spring Actuator для мониторинга приложения.

---

## API

Подробное описание HTTP API доступно в [REST API](./API.md). Основные эндпоинты:

- **POST /books**: Создание книги.
- **GET /books**: Получение списка всех книг с пагинацией.
- **PUT /books{id}**: Обновление информации о книге.
- **DEL /books{id}**: Удаление информации о книге.
- **POST /library-items**: Создание библиотечного экземпляра.
- **GET /library-items**: Получение списка библиотечных экземпляров с пагинацией.
- **POST /readers**: Регистрация читателя.
- **GET /readers**: Получение списка читателей с пагинацией.
- **POST /loans**: Выдача книги читателю.
- **POST /loans/{loanId}/return**: Возврат книги.
- **GET /loans/readers/{readerId}/loans**: Получение списка займов читателя с пагинацией.


---

## Структура проекта

- **src/main/kotlin/com/example/library_manager**:
  - **config**: Конфигурация приложения, включая `GlobalExceptionHandler`.
  - **controller**: REST-контроллеры (`LoanController`, `BookController`, и т.д.).
  - **service**: Бизнес-логика (`LoanService`, `BookService`, и т.д.).
  - **repository**: Репозитории для работы с базой данных (`LoanRepository`, `BookRepository`, и т.д.).
  - **domain**: JPA-сущности (`BookEntity`, `LibraryItemEntity`, `ReaderEntity`, `LoanEntity`).
  - **service/exceptions**: Пользовательские исключения (`ResourceNotFoundCustomException`, `DuplicateIsbnCustomException`, и т.д.).

- **src/test/kotlin/com/example/library_manager**:
  - Тесты для контроллеров и сервисов, использующие Mockito.

---

## Особенности реализации

- **Модульные тесты**: Использованы Mockito и юнит тестов для тестирования всех операций (создание, возврат, получение займов). Тесты проверяют корректность статусов HTTP и ответов JSON.
- **Обработка ошибок**: Все исключения обрабатываются в `GlobalExceptionHandler`, возвращая JSON-ответы в формате `{"message": "..."}` или `ErrorResponse`.
- **JPA и Hibernate**: Использованы для маппинга сущностей и управления транзакциями. Внешние ключи (`reader_id`, `library_item_id`, `book_id`) обеспечивают целостность данных.
- **Docker**: Поддержка контейнеризации для упрощения развертывания и тестирования.

---

## Контакты и поддержка

Если у вас есть вопросы или предложения по улучшению проекта, свяжитесь с разработчиком через [GitHub Issues](https://github.com/DenisKhanov).
```