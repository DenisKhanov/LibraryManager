# Документация API для Library Manager

Этот документ описывает HTTP API для приложения Library Manager, сервиса для управления книгами, библиотечными экземплярами, читателями и займами. API построен с использованием Spring Boot и предоставляет RESTful эндпоинты для создания, получения и управления библиотечными ресурсами.

Все эндпоинты возвращают ответы в формате JSON, а ошибки обрабатываются единообразно с соответствующими HTTP-кодами состояния и сообщениями об ошибках.

---

## Базовый URL

Базовый URL для всех эндпоинтов: `http://localhost:8080`.

---

## Эндпоинты

### 1. Создание книги

**POST /books**

Создает новую книгу в системе.

**Тело запроса**:
```json
{
  "title": "string",
  "author": "string",
  "isbn": "string",
  "publishedYear": "integer"
}
```

- `title`: Название книги (обязательное, непустое).
- `author`: Автор книги (обязательное, непустое).
- `isbn`: ISBN книги (обязательное, уникальное, валидный формат).
- `publishedYear`: Год публикации (обязательное, положительное целое число).

**Ответы**:
- **201 Created**: Книга успешно создана.
  ```json
  {
    "id": 1,
    "title": "Тестовая книга",
    "author": "Тестовый автор",
    "isbn": "9781234567890",
    "publishedYear": 2020
  }
  ```
- **400 Bad Request**: Неверный ввод или дублирующий ISBN.
  ```json
  {
    "message": "Book with ISBN 1231312312312312 already exists"
  }
  ```
- **500 Internal Server Error**: Непредвиденная ошибка.
  ```json
  {
    "status": 500,
    "error": "Внутренняя ошибка сервера",
    "message": "Непредвиденная ошибка"
  }
  ```

**Пример**:
```bash
curl -X POST http://localhost:8080/books \
-H "Content-Type: application/json" \
-d '{"title":"Тестовая книга","author":"Тестовый автор","isbn":"9781234567890","publishedYear":2020}'
```

---

### 2. Создание библиотечного экземпляра

**POST /library-items**

Создает новый библиотечный экземпляр (экземпляр книги) с указанным количеством копий.

**Тело запроса**:
```json
{
  "bookId": "integer",
  "totalCopies": "integer",
  "availableCopies": "integer"
}
```

- `bookId`: ID книги для привязки (обязательное, существующее ID книги).
- `totalCopies`: Общее количество копий (обязательное, положительное целое число).
- `availableCopies`: Количество доступных копий (обязательное, неотрицательное целое число, <= `totalCopies`).

**Ответы**:
- **201 Created**: Библиотечный экземпляр успешно создан.
  ```json
  {
    "id": 1,
    "bookId": 1,
    "totalCopies": 2,
    "availableCopies": 2
  }
  ```
- **400 Bad Request**: Неверный ввод (например, отрицательное количество копий или `availableCopies` > `totalCopies`).
  ```json
  {
    "message": "Total copies must be greater than 1"
  }
  ```
- **404 Not Found**: Книга с указанным ID не найдена.
  ```json
  {
    "status": 404,
    "error": "Not found",
    "message": "$Book with ID 1 not found"
  }
  ```
- **500 Internal Server Error**: Непредвиденная ошибка.
  ```json
  {
    "status": 500,
    "error": "Внутренняя ошибка сервера",
    "message": "Непредвиденная ошибка"
  }
  ```

**Пример**:
```bash
curl -X POST http://localhost:8080/library-items \
-H "Content-Type: application/json" \
-d '{"bookId":1,"totalCopies":2,"availableCopies":2}'
```

---

### 3. Регистрация читателя

**POST /readers**

Регистрирует нового читателя в системе.

**Тело запроса**:
```json
{
  "name": "string",
  "email": "string"
}
```

- `name`: Имя читателя (обязательное, непустое).
- `email`: Email читателя (обязательное, уникальное, валидный формат email).

**Ответы**:
- **201 Created**: Читатель успешно зарегистрирован.
  ```json
  {
    "id": 1,
    "name": "Иван Иванов",
    "email": "ivan.ivanov@example.com",
    "registeredAt": "2025-07-30T12:00:00"
  }
  ```
- **400 Bad Request**: Неверный ввод или дублирующий email.
  ```json
  {
    "message": "Reader with email ivan.ivanov@example.com already exists"
  }
  ```
- **500 Internal Server Error**: Непредвиденная ошибка.
  ```json
  {
    "status": 500,
    "error": "Внутренняя ошибка сервера",
    "message": "Непредвиденная ошибка"
  }
  ```

**Пример**:
```bash
curl -X POST http://localhost:8080/readers \
-H "Content-Type: application/json" \
-d '{"name":"Иван Иванов","email":"ivan.ivanov@example.com"}'
```

---

### 4. Создание займа

**POST /loans**

Создает займ для выдачи библиотечного экземпляра читателю.

**Тело запроса**:
```json
{
  "readerId": "integer",
  "libraryItemId": "integer"
}
```

- `readerId`: ID читателя (обязательное, существующее ID читателя).
- `libraryItemId`: ID библиотечного экземпляра (обязательное, существующее ID экземпляра).

**Ответы**:
- **201 Created**: Займ успешно создан.
  ```json
  {
    "id": 1,
    "readerId": 1,
    "libraryItemId": 1,
    "loanDate": "2025-07-30T12:00:00",
    "returnDate": null
  }
  ```
- **400 Bad Request**: Библиотечный экземпляр недоступен (нет доступных копий).
  ```json
  {
    "message": "Library item with ID 1 is not available"
  }
  ```
- **404 Not Found**: Читатель или библиотечный экземпляр не найдены.
  ```json
  {
    "status": 404,
    "error": "Не найдено",
    "message": "Reader with ID 1 not found"
  }
  ```
- **500 Internal Server Error**: Непредвиденная ошибка.
  ```json
  {
    "status": 500,
    "error": "Внутренняя ошибка сервера",
    "message": "Непредвиденная ошибка"
  }
  ```

**Пример**:
```bash
curl -X POST http://localhost:8080/loans \
-H "Content-Type: application/json" \
-d '{"readerId":1,"libraryItemId":1}'
```

---

### 5. Возврат займа

**POST /loans/{loanId}/return**

Отмечает займ как возвращенный, обновляя дату возврата и увеличивая количество доступных копий библиотечного экземпляра.

**Параметры пути**:
- `loanId`: ID займа для возврата (обязательное).

**Ответы**:
- **200 OK**: Займ успешно возвращен.
  ```json
  {
    "id": 1,
    "readerId": 1,
    "libraryItemId": 1,
    "loanDate": "2025-07-30T12:00:00",
    "returnDate": "2025-07-30T12:30:00"
  }
  ```
- **400 Bad Request**: Займ уже возвращен.
  ```json
  {
    "message": "Loan with ID 1 has already been returned"
  }
  ```
- **404 Not Found**: Займ не найден.
  ```json
  {
    "status": 404,
    "error": "Не найдено",
    "message": "Loan with ID 1 not found"
  }
  ```
- **500 Internal Server Error**: Непредвиденная ошибка.
  ```json
  {
    "status": 500,
    "error": "Внутренняя ошибка сервера",
    "message": "Непредвиденная ошибка"
  }
  ```

**Пример**:
```bash
curl -X POST http://localhost:8080/loans//1/return \
-H "Content-Type: application/json"
```

---

### 6. Получение займов читателя

**GET /loans/readers/{readerId}/loans**

Возвращает постраничный список займов для указанного читателя.

**Параметры пути**:
- `readerId`: ID читателя (обязательное).

**Параметры запроса**:
- `page`: Номер страницы (по умолчанию: 0).
- `size`: Количество элементов на странице (по умолчанию: 10).

**Ответы**:
- **200 OK**: Займы успешно получены.
  ```json
  {
    "content": [
      {
        "id": 1,
        "readerId": 1,
        "libraryItemId": 1,
        "loanDate": "2025-07-30T12:00:00",
        "returnDate": null
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    "numberOfElements": 1,
    "first": true,
    "empty": false
  }
  ```
- **404 Not Found**: Читатель не найден.
  ```json
  {
    "status": 404,
    "error": "Не найдено",
    "message": "Reader with ID 999 not found"
  }
  ```
- **500 Internal Server Error**: Непредвиденная ошибка.
  ```json
  {
    "status": 500,
    "error": "Внутренняя ошибка сервера",
    "message": "Непредвиденная ошибка"
  }
  ```

**Пример**:
```bash
curl -X GET "http://localhost:8080/loans/readers/1/loans?page=0&size=10"
```

---

## Обработка ошибок

Ошибки возвращаются в одном из двух форматов в зависимости от типа исключения:

1. **Ошибки валидации и бизнес-логики** (например, `ResponseStatusException`, `MethodArgumentNotValidException`):
   ```json
   {
     "message": "Сообщение об ошибке"
   }
   ```
   или, для ошибок валидации:
   ```json
   {
     "fieldName": "Сообщение об ошибке"
   }
   ```

2. **Пользовательские исключения** (например, `ResourceNotFoundCustomException`, `DuplicateIsbnCustomException`):
   ```json
{
  "status": "integer",
  "error": "string",
  "message": "string"
}
```

Общие коды состояния HTTP:
- `200 OK`: Успешный запрос.
- `201 Created`: Ресурс успешно создан.
- `400 Bad Request`: Неверный ввод или нарушение бизнес-правила.
- `404 Not Found`: Ресурс не найден.
- `500 Internal Server Error`: Непредвиденная ошибка сервера.

---

## Примечания

- API предполагает использование базы данных PostgreSQL со схемой, описанной в [README](README.md).
- Постраничная навигация поддерживается для эндпоинта `GET /loans/readers/{readerId}/loans` с использованием интерфейса `Pageable` из Spring Data.
- Все эндпоинты в настоящее время не требуют аутентификации. В будущих версиях планируется добавить JWT-аутентификацию (см. раздел TODO в [README](README.md)).
- `GlobalExceptionHandler` обеспечивает единообразные ответы об ошибках для всех эндпоинтов.

```