package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для таблиці container_site (контейнерні майданчики).
 * Містить лише SQL/JDBC: без валідації та предметної логіки.
 */
public class ContainerSiteDAO {

    private static final String COLUMNS =
            "id, code, name, address, district, waste_type, container_count, capacity_l, fill_level, sanitary_risk";

    static final String SQL_INSERT =
            "INSERT INTO container_site(code, name, address, district, waste_type, container_count, capacity_l, fill_level, sanitary_risk) "
                    + "VALUES(?,?,?,?,?,?,?,?,?)";
    static final String SQL_SELECT_ALL = "SELECT " + COLUMNS + " FROM container_site ORDER BY id";
    static final String SQL_SELECT_BY_ID = "SELECT " + COLUMNS + " FROM container_site WHERE id=?";
    static final String SQL_UPDATE =
            "UPDATE container_site SET code=?, name=?, address=?, district=?, waste_type=?, container_count=?, capacity_l=?, fill_level=?, sanitary_risk=? "
                    + "WHERE id=?";
    static final String SQL_DELETE = "DELETE FROM container_site WHERE id=?";

    /** Додає запис; згенерований MySQL id записується в переданий об'єкт. */
    public void add(ContainerSite s) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, s);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<ContainerSite> getAll() throws SQLException {
        List<ContainerSite> list = new ArrayList<>();
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
    public ContainerSite getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_SELECT_BY_ID)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(ContainerSite s) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_UPDATE)) {
            int next = bind(p, s);
            p.setLong(next, s.getId());
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

    /** Java-об'єкт -> параметри PreparedStatement. Повертає індекс наступного параметра. */
    private static int bind(PreparedStatement p, ContainerSite s) throws SQLException {
        int i = 1;
        p.setString(i++, s.getCode());
        p.setString(i++, s.getName());
        p.setString(i++, s.getAddress());
        p.setString(i++, s.getDistrict());
        p.setString(i++, s.getWasteType());
        p.setInt(i++, s.getContainerCount());
        p.setInt(i++, s.getCapacityL());
        p.setDouble(i++, s.getFillLevel());
        p.setDouble(i++, s.getSanitaryRisk());
        return i;
    }

    /** Рядок ResultSet -> Java-об'єкт. */
    private static ContainerSite map(ResultSet rs) throws SQLException {
        return new ContainerSite(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("district"),
                rs.getString("waste_type"),
                rs.getInt("container_count"),
                rs.getInt("capacity_l"),
                rs.getDouble("fill_level"),
                rs.getDouble("sanitary_risk"));
    }
}
