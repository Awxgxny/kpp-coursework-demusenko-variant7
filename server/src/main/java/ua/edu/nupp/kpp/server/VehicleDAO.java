package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для таблиці vehicle (транспортні засоби).
 * Містить лише SQL/JDBC: без валідації та предметної логіки.
 */
public class VehicleDAO {

    private static final String COLUMNS = "id, plate_number, model, capacity_m3, driver_name";

    static final String SQL_INSERT =
            "INSERT INTO vehicle(plate_number, model, capacity_m3, driver_name) VALUES(?,?,?,?)";
    static final String SQL_SELECT_ALL = "SELECT " + COLUMNS + " FROM vehicle ORDER BY id";
    static final String SQL_SELECT_BY_ID = "SELECT " + COLUMNS + " FROM vehicle WHERE id=?";
    static final String SQL_UPDATE =
            "UPDATE vehicle SET plate_number=?, model=?, capacity_m3=?, driver_name=? WHERE id=?";
    static final String SQL_DELETE = "DELETE FROM vehicle WHERE id=?";

    /** Додає запис; згенерований MySQL id записується в переданий об'єкт. */
    public void add(Vehicle v) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, v);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    v.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<Vehicle> getAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
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
    public Vehicle getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_SELECT_BY_ID)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Vehicle v) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_UPDATE)) {
            int next = bind(p, v);
            p.setLong(next, v.getId());
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

    private static int bind(PreparedStatement p, Vehicle v) throws SQLException {
        int i = 1;
        p.setString(i++, v.getPlateNumber());
        p.setString(i++, v.getModel());
        p.setDouble(i++, v.getCapacityM3());
        p.setString(i++, v.getDriverName());
        return i;
    }

    private static Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getLong("id"),
                rs.getString("plate_number"),
                rs.getString("model"),
                rs.getDouble("capacity_m3"),
                rs.getString("driver_name"));
    }
}
