# Інформаційна система керування заявками на вивезення відходів

Курсова робота з дисципліни «Кросплатформене програмування», варіант №7.
Java 21, Maven, JavaFX, Servlet API (Apache Tomcat 9.x), MySQL.

Маршрут запиту: JavaFX → HTTP-клієнт → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL.

## Структура

- `database.sql` — створення бази `waste_requests` і початкових даних;
- `server/` — серверна частина (WAR для Tomcat 9.x): моделі, DAO, Service, Servlet, `web.xml`;
- `client/` — JavaFX-клієнт, HTTP-клієнт, клієнтські моделі, `sample-data/` із CSV.

## Запуск

1. Запустити MySQL і виконати `database.sql`. Параметри підключення задано в `server/.../DBUtil.java`.
2. Зібрати сервер: у каталозі `server` виконати `mvn clean package` (з'явиться `server/target/kpp-server.war`).
3. Скопіювати `kpp-server.war` у каталог `webapps` Apache Tomcat 9.x і запустити Tomcat (`bin/startup.bat`).
4. Перевірити сервер у браузері: `http://localhost:8080/kpp-server/ping` та `http://localhost:8080/kpp-server/sites`.
5. Запустити клієнт: у каталозі `client` виконати `mvn javafx:run`.

Адресу сервера клієнт бере з системної властивості `kpp.server.url`
(за замовчуванням `http://localhost:8080/kpp-server`).

## HTTP-маршрути

| URL | Servlet | Призначення |
|-----|---------|-------------|
| `/ping` | PingServlet | перевірка доступності сервера |
| `/sites` | ContainerSiteServlet | контейнерні майданчики |
| `/requests` | WasteRequestServlet | заявки, перерахунок показника, зміна стану |
| `/vehicles` | VehicleServlet | транспортні засоби |
| `/trips` | TripServlet | виїзди |

GET повертає дані у форматі JSON. Зміни виконує POST (`application/x-www-form-urlencoded`)
із параметром `action`: `add`, `update`, `delete`; для заявок також `recalculate` і `transition`.
Помилки повертаються у вигляді `{"error": "..."}` з кодом 400 (вхідні та предметні помилки) або 500.
