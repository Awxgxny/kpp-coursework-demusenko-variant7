package ua.edu.nupp.kpp.client.csv;

import ua.edu.nupp.kpp.client.model.ContainerSite;

import java.nio.file.Path;

/**
 * Формат CSV з контейнерними майданчиками та розбір одного рядка.
 *
 * Формат: UTF-8, роздільник ';', перший рядок — заголовок, далі по одному майданчику в рядку:
 * code;name;address;district;waste_type;container_count;capacity_l;fill_level;sanitary_risk
 *
 * Клієнт перевіряє лише формат (заголовок, кількість стовпців, порожні поля, числа). Предметні
 * правила (діапазони, унікальність коду, допустимі типи відходів) перевіряє сервер.
 * Читання файла й передавання записів на сервер виконує {@link ImportSitesTask}.
 */
public final class CsvSiteReader {

    public static final String DELIMITER = ";";
    public static final int COLUMNS = 9;

    /** Затверджений заголовок: назви та порядок стовпців. */
    public static final String EXPECTED_HEADER =
            "code;name;address;district;waste_type;container_count;capacity_l;fill_level;sanitary_risk";

    /** Файл за замовчуванням: відносний шлях від каталогу запуску клієнта (client/). */
    public static final Path DEFAULT_FILE = Path.of("sample-data", "container_sites.csv");

    private CsvSiteReader() {
    }

    /**
     * Перевіряє перший рядок файла. Початковий маркер UTF-8 (BOM), який додає Excel, ігнорується.
     *
     * @throws IllegalArgumentException якщо заголовок не збігається із затвердженим
     */
    public static void checkHeader(String header) {
        String actual = stripBom(header).trim();
        if (!EXPECTED_HEADER.equals(actual)) {
            throw new IllegalArgumentException("Неправильний заголовок CSV. Очікується: "
                    + EXPECTED_HEADER + "; знайдено: " + actual);
        }
    }

    /**
     * Перетворює рядок даних на майданчик.
     *
     * @throws IllegalArgumentException якщо кількість полів не дорівнює 9, є порожнє поле
     *                                  або число записане некоректно
     */
    public static ContainerSite parseLine(String line) {
        String[] f = line.split(DELIMITER, -1);
        if (f.length != COLUMNS) {
            throw new IllegalArgumentException("очікується " + COLUMNS + " полів, знайдено " + f.length);
        }
        for (int i = 0; i < f.length; i++) {
            f[i] = f[i].trim();
            if (f[i].isEmpty()) {
                throw new IllegalArgumentException("порожнє поле №" + (i + 1));
            }
        }
        ContainerSite s = new ContainerSite();
        s.setCode(f[0]);
        s.setName(f[1]);
        s.setAddress(f[2]);
        s.setDistrict(f[3]);
        s.setWasteType(f[4]);
        s.setContainerCount(toInt(f[5], "container_count"));
        s.setCapacityL(toInt(f[6], "capacity_l"));
        s.setFillLevel(toDouble(f[7], "fill_level"));
        s.setSanitaryRisk(toDouble(f[8], "sanitary_risk"));
        return s;
    }

    private static int toInt(String v, String name) {
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + name + "' має бути цілим числом, а не '" + v + "'");
        }
    }

    private static double toDouble(String v, String name) {
        try {
            return Double.parseDouble(v.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + name + "' має бути числом, а не '" + v + "'");
        }
    }

    static String stripBom(String s) {
        return s.startsWith("﻿") ? s.substring(1) : s;
    }
}
