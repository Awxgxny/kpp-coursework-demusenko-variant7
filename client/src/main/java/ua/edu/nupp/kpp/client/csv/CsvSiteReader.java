package ua.edu.nupp.kpp.client.csv;

import ua.edu.nupp.kpp.client.model.ContainerSite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Читання CSV з контейнерними майданчиками.
 *
 * Формат: UTF-8, роздільник ';', перший рядок — заголовок, далі по одному майданчику в рядку:
 * code;name;address;district;waste_type;container_count;capacity_l;fill_level;sanitary_risk
 *
 * Клієнт перевіряє лише формат (кількість стовпців, порожні поля, числа). Предметні правила
 * (діапазони, унікальність коду, допустимі типи відходів) перевіряє сервер.
 */
public class CsvSiteReader {

    public static final String DELIMITER = ";";
    public static final int COLUMNS = 9;

    /** Результат читання: коректні за форматом рядки та повідомлення про помилкові. */
    public static class Result {
        private final List<Row> rows = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        public List<Row> getRows() {
            return rows;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /** Майданчик із номером рядка у файлі (для повідомлень про помилки). */
    public static class Row {
        private final int line;
        private final ContainerSite site;

        public Row(int line, ContainerSite site) {
            this.line = line;
            this.site = site;
        }

        public int getLine() {
            return line;
        }

        public ContainerSite getSite() {
            return site;
        }
    }

    public Result read(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        Result result = new Result();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (i == 0) {
                line = stripBom(line);
            }
            if (i == 0 || line.isBlank()) {
                continue; // заголовок та порожні рядки пропускаємо
            }
            int number = i + 1;
            try {
                result.rows.add(new Row(number, parseLine(line)));
            } catch (IllegalArgumentException e) {
                result.errors.add("Рядок " + number + ": " + e.getMessage());
            }
        }
        return result;
    }

    static ContainerSite parseLine(String line) {
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

    private static String stripBom(String s) {
        return s.startsWith("﻿") ? s.substring(1) : s;
    }
}
