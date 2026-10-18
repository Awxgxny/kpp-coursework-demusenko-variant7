-- Курсова робота, варіант 7
-- Інформаційна система керування заявками на вивезення відходів
-- Скрипт відтворює базу даних з нуля (виконувати через phpMyAdmin -> Імпорт).

DROP DATABASE IF EXISTS waste_requests;
CREATE DATABASE waste_requests CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE waste_requests;

-- Таблиця контейнерних майданчиків
-- fill_level (F1) та sanitary_risk (F2) зберігаються вже нормованими до діапазону 0-100
CREATE TABLE container_site (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(40)  NOT NULL UNIQUE,
    name            VARCHAR(160) NOT NULL,
    address         VARCHAR(255) NOT NULL,
    district        VARCHAR(120) NOT NULL,
    waste_type      VARCHAR(40)  NOT NULL,
    container_count INT          NOT NULL,
    capacity_l      INT          NOT NULL,
    fill_level      DOUBLE       NOT NULL,
    sanitary_risk   DOUBLE       NOT NULL
);

-- Таблиця транспортних засобів
CREATE TABLE vehicle (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(20)  NOT NULL UNIQUE,
    model        VARCHAR(100) NOT NULL,
    capacity_m3  DOUBLE       NOT NULL,
    driver_name  VARCHAR(120) NOT NULL
);

-- Таблиця заявок на вивезення відходів (об'єкт життєвого циклу)
-- state: CREATED -> PLANNED -> IN_PROGRESS -> DONE
-- f1, f2, f3 та integral_index = 0.40*f1 + 0.35*f2 + 0.25*f3 фіксуються під час створення заявки
CREATE TABLE waste_request (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    container_site_id BIGINT       NOT NULL,
    description       VARCHAR(500) NOT NULL,
    created_date      DATE         NOT NULL,
    due_date          DATE         NOT NULL,
    state             VARCHAR(30)  NOT NULL DEFAULT 'CREATED',
    f1                DOUBLE       NOT NULL,
    f2                DOUBLE       NOT NULL,
    f3                DOUBLE       NOT NULL,
    integral_index    DOUBLE       NOT NULL,
    FOREIGN KEY (container_site_id) REFERENCES container_site(id)
);

-- Таблиця виїздів (заявка + транспортний засіб)
CREATE TABLE trip (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id          BIGINT       NOT NULL,
    vehicle_id          BIGINT       NOT NULL,
    trip_date           DATE         NOT NULL,
    collected_volume_m3 DOUBLE       NOT NULL DEFAULT 0,
    notes               VARCHAR(255) NULL,
    FOREIGN KEY (request_id) REFERENCES waste_request(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
);

-- Початкові демонстраційні дані
INSERT INTO container_site
    (code, name, address, district, waste_type, container_count, capacity_l, fill_level, sanitary_risk)
VALUES
('CS-001', 'Майданчик «Центральний»',  'вул. Соборності, 12',      'Київський',     'MIXED',   6, 6600, 85.0, 70.0),
('CS-002', 'Майданчик «Ринок»',        'вул. Зигіна, 3',           'Шевченківський','ORGANIC', 4, 4400, 95.0, 90.0),
('CS-003', 'Майданчик «Парк Перемоги»','вул. Європейська, 45',     'Подільський',   'PLASTIC', 3, 3300, 20.0, 40.0),
('CS-004', 'Майданчик «Левада»',       'вул. Великотирнівська, 21','Подільський',   'PAPER',   2, 2200, 40.0, 30.0);

INSERT INTO vehicle (plate_number, model, capacity_m3, driver_name) VALUES
('BI 1234 AA', 'МАЗ-5340 сміттєвоз',     16.0, 'Коваленко Олег Петрович'),
('BI 5678 CE', 'КамАЗ-53605 сміттєвоз',  20.0, 'Бондаренко Ігор Миколайович'),
('BI 9012 HK', 'Mercedes-Benz Econic',   12.0, 'Мельник Андрій Васильович');

-- I = 0.40*F1 + 0.35*F2 + 0.25*F3
INSERT INTO waste_request
    (container_site_id, description, created_date, due_date, state, f1, f2, f3, integral_index)
VALUES
(1, 'Вивезення змішаних відходів, контейнери переповнені', '2026-10-10', '2026-10-14', 'PLANNED',     85.0, 70.0, 40.0, 68.5),
(2, 'Термінове вивезення органічних відходів біля ринку',  '2026-10-12', '2026-10-15', 'IN_PROGRESS', 95.0, 90.0, 30.0, 77.0),
(3, 'Планове вивезення пластику',                          '2026-10-05', '2026-10-09', 'DONE',        80.0, 40.0,  0.0, 46.0),
(4, 'Планове вивезення макулатури',                        '2026-10-17', '2026-10-24', 'CREATED',     40.0, 30.0,  0.0, 26.5);

INSERT INTO trip (request_id, vehicle_id, trip_date, collected_volume_m3, notes) VALUES
(1, 1, '2026-10-19', 0.0, 'Виїзд заплановано'),
(2, 2, '2026-10-18', 0.0, 'Виїзд триває'),
(3, 3, '2026-10-09', 6.5, 'Відходи вивезено повністю');
