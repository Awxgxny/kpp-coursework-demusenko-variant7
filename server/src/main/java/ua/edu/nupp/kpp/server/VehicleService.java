package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service транспортних засобів: валідація, виклик DAO, логування.
 */
public class VehicleService {

    private static final Logger LOG = Logger.getLogger(VehicleService.class.getName());

    private final VehicleDAO dao = new VehicleDAO();

    public List<Vehicle> getAll() throws SQLException {
        return dao.getAll();
    }

    public Vehicle getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Vehicle v) throws SQLException {
        validate(v);
        dao.add(v);
        LOG.info("Додано транспортний засіб");
    }

    public void update(Vehicle v) throws SQLException {
        validate(v);
        if (v.getId() == null || dao.getById(v.getId()) == null) {
            throw reject("Транспортний засіб не знайдено");
        }
        dao.update(v);
        LOG.info("Оновлено транспортний засіб");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено транспортний засіб");
    }

    private void validate(Vehicle v) {
        if (v == null || blank(v.getPlateNumber()) || blank(v.getModel()) || blank(v.getDriverName())) {
            throw reject("Номерний знак, модель і водій транспортного засобу є обов'язковими");
        }
        if (!(v.getCapacityM3() > 0)) {
            throw reject("Місткість транспортного засобу має бути більшою за 0");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }

    private IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
