package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service заявок на вивезення відходів — об'єкта чотиристанного життєвого циклу.
 * Відповідає за валідацію, розрахунок факторів F1–F3 та інтегрального показника,
 * контроль переходів станів і логування. SQL виконує лише WasteRequestDAO.
 *
 * Життєвий цикл: CREATED → PLANNED → IN_PROGRESS → DONE.
 */
public class WasteRequestService {

    private static final Logger LOG = Logger.getLogger(WasteRequestService.class.getName());

    private final WasteRequestDAO dao = new WasteRequestDAO();
    private final ContainerSiteDAO containerSiteDAO = new ContainerSiteDAO();
    private final Clock clock;

    public WasteRequestService() {
        this(Clock.systemDefaultZone());
    }

    /** Конструктор із заданим годинником (для перевірки прострочення на задану дату). */
    public WasteRequestService(Clock clock) {
        this.clock = clock;
    }

    public List<WasteRequest> getAll() throws SQLException {
        return dao.getAll();
    }

    public WasteRequest getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Створює заявку для контейнерного майданчика. Користувач не вводить F1, F2, F3 та I:
     * сервер бере їх з майданчика й розраховує сам. Початковий стан — CREATED,
     * дата створення — поточна.
     */
    public void add(long containerSiteId, String description, LocalDate dueDate) throws SQLException {
        LocalDate today = LocalDate.now(clock);
        validate(containerSiteId, description, dueDate, today);
        WasteRequest r = fromContainerSite(null, containerSiteId, description, today, dueDate,
                RequestState.CREATED);
        dao.add(r);
        LOG.info("Створено заявку на вивезення відходів");
    }

    /**
     * Редагує дані заявки (майданчик, опис, строк). Стан і дата створення не змінюються,
     * показники F1–F3 та I розраховуються заново.
     */
    public void update(long id, long containerSiteId, String description, LocalDate dueDate)
            throws SQLException {
        WasteRequest old = requireEditable(id);
        validate(containerSiteId, description, dueDate, old.getCreatedDate());
        dao.update(fromContainerSite(id, containerSiteId, description, old.getCreatedDate(), dueDate,
                old.getState()));
        LOG.info("Оновлено заявку на вивезення відходів");
    }

    /**
     * Перераховує F1–F3 та I за поточними даними майданчика і на поточну дату
     * (потрібно, коли змінився рівень заповнення або настав строк вивезення).
     */
    public void recalculate(long id) throws SQLException {
        WasteRequest old = requireEditable(id);
        dao.update(fromContainerSite(id, old.getContainerSiteId(), old.getDescription(),
                old.getCreatedDate(), old.getDueDate(), old.getState()));
        LOG.info("Перераховано інтегральний показник заявки");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено заявку на вивезення відходів");
    }

    /**
     * Змінює стан заявки лише за схемою CREATED → PLANNED → IN_PROGRESS → DONE.
     * Дані заявки не змінюються.
     */
    public void changeState(long id, RequestState next) throws SQLException {
        WasteRequest current = dao.getById(id);
        if (current == null) {
            throw reject("Заявку не знайдено");
        }
        RequestState from = current.getState();
        if (next == null || !isAllowedTransition(from, next)) {
            LOG.warning("Недопустимий перехід стану: " + from + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан заявки: " + from + " -> " + next);
    }

    /** Дозволені лише три послідовні переходи; пропуски, повернення назад і вихід з DONE заборонені. */
    public static boolean isAllowedTransition(RequestState from, RequestState to) {
        return (from == RequestState.CREATED && to == RequestState.PLANNED)
                || (from == RequestState.PLANNED && to == RequestState.IN_PROGRESS)
                || (from == RequestState.IN_PROGRESS && to == RequestState.DONE);
    }

    // ---------------------------------------------------------------- допоміжні методи

    /** Збирає заявку з розрахованими F1–F3 та I; стан задає викликаючий метод. */
    private WasteRequest fromContainerSite(Long id, long containerSiteId, String description,
                                           LocalDate createdDate, LocalDate dueDate, RequestState state)
            throws SQLException {
        ContainerSite site = containerSiteDAO.getById(containerSiteId);
        if (site == null) {
            throw reject("Контейнерний майданчик не знайдено");
        }
        double f1 = RiskCalculator.factor1(site.getFillLevel());
        double f2 = RiskCalculator.factor2(site.getSanitaryRisk());
        double f3 = RiskCalculator.factor3(dueDate, LocalDate.now(clock));
        double index = RiskCalculator.calculate(f1, f2, f3);
        return new WasteRequest(id, containerSiteId, description.trim(), createdDate, dueDate, state,
                f1, f2, f3, index);
    }

    private WasteRequest requireEditable(long id) throws SQLException {
        WasteRequest r = dao.getById(id);
        if (r == null) {
            throw reject("Заявку не знайдено");
        }
        if (r.getState() == RequestState.DONE) {
            throw reject("Заявку у стані ВИКОНАНО не можна змінювати");
        }
        return r;
    }

    private void validate(long containerSiteId, String description, LocalDate dueDate, LocalDate createdDate) {
        if (containerSiteId <= 0) {
            throw reject("Некоректний контейнерний майданчик");
        }
        if (description == null || description.isBlank()) {
            throw reject("Опис заявки є обов'язковим");
        }
        if (dueDate == null) {
            throw reject("Строк вивезення є обов'язковим");
        }
        if (dueDate.isBefore(createdDate)) {
            throw reject("Строк вивезення не може бути раніше дати створення заявки");
        }
    }

    private IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
