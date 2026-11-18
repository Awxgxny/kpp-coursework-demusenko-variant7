package ua.edu.nupp.kpp.client.csv;

import javafx.concurrent.Task;
import ua.edu.nupp.kpp.client.ApiClient;
import ua.edu.nupp.kpp.client.ApiException;
import ua.edu.nupp.kpp.client.model.ContainerSite;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Фоновий імпорт контейнерних майданчиків із CSV (JavaFX {@link Task}).
 *
 * Читання файла та передавання записів на сервер виконуються у фоновому потоці, тому вікно
 * не блокується. Клієнт перевіряє лише формат; кожен запис іде тим самим HTTP-маршрутом, що й
 * ручне додавання, а предметну валідацію та INSERT виконує сервер (Servlet → Service → DAO).
 *
 * Правила обробки помилок:
 * <ul>
 *   <li>файл не знайдено, не читається, порожній, має інше кодування або неправильний заголовок —
 *       задача завершується помилкою до першого запиту до сервера;</li>
 *   <li>рядок із помилкою формату або запис, який відхилив сервер (HTTP 4xx), — рядок
 *       пропускається з WARNING, імпорт триває;</li>
 *   <li>сервер недоступний або повернув помилку 5xx — задача завершується помилкою; записи,
 *       які сервер уже прийняв, залишаються в базі.</li>
 * </ul>
 * Результат задачі — кількість записів, прийнятих сервером.
 */
public class ImportSitesTask extends Task<Integer> {

    private static final Logger LOG = Logger.getLogger(ImportSitesTask.class.getName());

    private final Path file;
    private final ApiClient api;
    private final List<String> problems = Collections.synchronizedList(new ArrayList<>());

    public ImportSitesTask(Path file, ApiClient api) {
        this.file = file;
        this.api = api;
    }

    /** Повідомлення про пропущені рядки (формат, відхилення сервером); читати після завершення. */
    public List<String> getProblems() {
        synchronized (problems) {
            return new ArrayList<>(problems);
        }
    }

    @Override
    protected Integer call() throws Exception {
        LOG.info("Початок CSV-імпорту, файл: " + file.getFileName());
        updateMessage("Читання файла " + file.getFileName() + "…");

        List<String> lines = readLines();
        if (lines.isEmpty()) {
            throw new IOException("Файл порожній: немає заголовка");
        }
        CsvSiteReader.checkHeader(lines.get(0));

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                numbers.add(i);
            }
        }
        int total = numbers.size();
        if (total == 0) {
            LOG.info("CSV-імпорт: у файлі немає рядків даних");
            updateMessage("Немає рядків для імпорту");
            return 0;
        }

        int imported = 0;
        int processed = 0;
        for (int index : numbers) {
            int lineNumber = index + 1; // фізичний номер рядка: заголовок — перший
            try {
                ContainerSite site = CsvSiteReader.parseLine(lines.get(index));
                api.addSite(site);
                imported++;
            } catch (IllegalArgumentException e) {
                skip("Рядок " + lineNumber + ": " + e.getMessage());
            } catch (ApiException e) {
                if (e.getStatus() < 0 || e.getStatus() >= 500) {
                    LOG.log(Level.SEVERE, "CSV-імпорт перервано на рядку " + lineNumber, e);
                    throw new ApiException(e.getMessage() + " Імпорт перервано на рядку " + lineNumber
                            + "; уже збережено записів: " + imported, e);
                }
                skip("Рядок " + lineNumber + ": сервер відхилив запис — " + e.getMessage());
            }
            processed++;
            updateProgress(processed, total);
            updateMessage("Опрацьовано " + processed + " із " + total);
        }

        LOG.info("Завершення CSV-імпорту: імпортовано " + imported + ", пропущено " + problems.size()
                + " із " + total + " рядків");
        return imported;
    }

    private List<String> readLines() throws IOException {
        if (!Files.isRegularFile(file)) {
            LOG.warning("CSV-файл не знайдено: " + file.getFileName());
            throw new IOException("Файл не знайдено: " + file);
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            throw new IOException("Файл має кодування, відмінне від UTF-8: " + file, e);
        }
    }

    private void skip(String message) {
        problems.add(message);
        LOG.warning("Пропущено: " + message);
    }
}
