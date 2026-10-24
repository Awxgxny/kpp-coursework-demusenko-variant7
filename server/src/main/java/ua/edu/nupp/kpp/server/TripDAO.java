package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для таблиці trip (виїзди транспортних засобів).
 * Містить лише SQL/JDBC: без валідації та предметної логіки.
 */
public class TripDAO {

    private static final String COLUMNS = "id, request_id, vehicle_id, trip_date, collected_volume_m3, notes";

    static final String SQL_INSERT =
            "INSERT INTO trip(request_id, vehicle_id, trip_date, collected_volume_m3, notes) VALUES(?,?,?,?,?)";
    static final String SQL_SELECT_ALL = "SELECT " + COLUMNS + " FROM trip ORDER BY id";
    static final String SQL_SELECT_BY_ID = "SELECT " + COLUMNS + " FROM trip WHERE id=?";
    static final String SQL_UPDATE =
            "UPDATE trip SET request_id=?, vehicle_id=?, trip_date=?, collected_volume_m3=?, notes=? WHERE id=?";
    static final String SQL_DELETE = "DELETE FROM trip WHERE id=?";

    /** Додає запис; згенерований MySQL id записується в переданий об'єкт. */
    public void add(Trip t) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, t);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<Trip> getAll() throws SQLException {
        List<Trip> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /** Повертає запис або null, якщо запису з таким id немає. */
    public Trip getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_SELECT_BY_ID)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Trip t) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_UPDATE)) {
            int next = bind(p, t);
            p.setLong(next, t.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_DELETE)) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private static int bind(PreparedStatement p, Trip t) throws SQLException {
        int i = 1;
        p.setLong(i++, t.getRequestId());
        p.setLong(i++, t.getVehicleId());
        p.setDate(i++, Date.valueOf(t.getTripDate()));
        p.setDouble(i++, t.getCollectedVolumeM3());
        p.setString(i++, t.getNotes());
        return i;
    }

    private static Trip map(ResultSet rs) throws SQLException {
        return new Trip(
                rs.getLong("id"),
                rs.getLong("request_id"),
                rs.getLong("vehicle_id"),
                rs.getDate("trip_date").toLocalDate(),
                rs.getDouble("collected_volume_m3"),
                rs.getString("notes"));
    }
}
