package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service контейнерних майданчиків: валідація, виклик DAO, логування.
 */
public class ContainerSiteService {

    private static final Logger LOG = Logger.getLogger(ContainerSiteService.class.getName());

    private final ContainerSiteDAO dao = new ContainerSiteDAO();

    public List<ContainerSite> getAll() throws SQLException {
        return dao.getAll();
    }

    public ContainerSite getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(ContainerSite s) throws SQLException {
        validate(s);
        dao.add(s);
        LOG.info("Додано контейнерний майданчик");
    }

    public void update(ContainerSite s) throws SQLException {
        validate(s);
        if (s.getId() == null || dao.getById(s.getId()) == null) {
            throw reject("Контейнерний майданчик не знайдено");
        }
        dao.update(s);
        LOG.info("Оновлено контейнерний майданчик");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено контейнерний майданчик");
    }

    private void validate(ContainerSite s) {
        if (s == null
                || blank(s.getCode()) || blank(s.getName()) || blank(s.getAddress())
                || blank(s.getDistrict()) || blank(s.getWasteType())) {
            throw reject("Усі текстові поля контейнерного майданчика є обов'язковими");
        }
        if (s.getContainerCount() <= 0 || s.getCapacityL() <= 0) {
            throw reject("Кількість контейнерів і місткість мають бути додатними");
        }
        if (!inRange(s.getFillLevel()) || !inRange(s.getSanitaryRisk())) {
            throw reject("Рівень заповнення (F1) і санітарний ризик (F2) мають бути в діапазоні 0..100");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }

    private static boolean inRange(double v) {
        return v >= 0 && v <= 100; // NaN дає false
    }

    private IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
