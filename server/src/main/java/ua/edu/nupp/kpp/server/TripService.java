package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service виїздів: валідація власних полів, перевірка існування заявки та транспортного засобу,
 * виклик DAO, логування.
 */
public class TripService {

    private static final Logger LOG = Logger.getLogger(TripService.class.getName());

    private static final int MAX_NOTES_LENGTH = 255; // VARCHAR(255) у таблиці trip

    private final TripDAO dao = new TripDAO();
    private final WasteRequestDAO requestDAO = new WasteRequestDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public List<Trip> getAll() throws SQLException {
        return dao.getAll();
    }

    public Trip getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Trip t) throws SQLException {
        validate(t);
        requireRequest(t.getRequestId());
        Vehicle v = requireVehicle(t.getVehicleId());
        checkVolume(t, v);
        dao.add(t);
        LOG.info("Додано виїзд");
    }

    public void update(Trip t) throws SQLException {
        validate(t);
        if (t.getId() == null || dao.getById(t.getId()) == null) {
            throw reject("Виїзд не знайдено");
        }
        requireRequest(t.getRequestId());
        Vehicle v = requireVehicle(t.getVehicleId());
        checkVolume(t, v);
        dao.update(t);
        LOG.info("Оновлено виїзд");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено виїзд");
    }

    private void validate(Trip t) {
        if (t == null || t.getRequestId() <= 0 || t.getVehicleId() <= 0 || t.getTripDate() == null) {
            throw reject("Некоректні дані виїзду");
        }
        if (!(t.getCollectedVolumeM3() >= 0)) {
            throw reject("Обсяг вивезених відходів не може бути від'ємним");
        }
        if (t.getNotes() != null && t.getNotes().length() > MAX_NOTES_LENGTH) {
            throw reject("Примітка виїзду має бути не довшою за " + MAX_NOTES_LENGTH + " символів");
        }
    }

    private void requireRequest(long requestId) throws SQLException {
        if (requestDAO.getById(requestId) == null) {
            throw reject("Заявку не знайдено");
        }
    }

    private Vehicle requireVehicle(long vehicleId) throws SQLException {
        Vehicle v = vehicleDAO.getById(vehicleId);
        if (v == null) {
            throw reject("Транспортний засіб не знайдено");
        }
        return v;
    }

    private void checkVolume(Trip t, Vehicle v) {
        if (t.getCollectedVolumeM3() > v.getCapacityM3()) {
            throw reject("Обсяг вивезених відходів перевищує місткість транспортного засобу");
        }
    }

    private IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
