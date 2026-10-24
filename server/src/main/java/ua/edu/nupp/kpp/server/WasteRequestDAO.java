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
 * DAO для таблиці waste_request (заявки на вивезення відходів).
 * Стан життєвого циклу зберігається в MySQL текстом: state.name() / RequestState.valueOf(...).
 * DAO лише записує дані: правила переходів між станами належать Service-рівню.
 */
public class WasteRequestDAO {

    private static final String COLUMNS =
            "id, container_site_id, description, created_date, due_date, state, f1, f2, f3, integral_index";

    static final String SQL_INSERT =
            "INSERT INTO waste_request(container_site_id, description, created_date, due_date, state, f1, f2, f3, integral_index) "
                    + "VALUES(?,?,?,?,?,?,?,?,?)";
    static final String SQL_SELECT_ALL = "SELECT " + COLUMNS + " FROM waste_request ORDER BY id";
    static final String SQL_SELECT_BY_ID = "SELECT " + COLUMNS + " FROM waste_request WHERE id=?";
    static final String SQL_UPDATE =
            "UPDATE waste_request SET container_site_id=?, description=?, created_date=?, due_date=?, state=?, f1=?, f2=?, f3=?, integral_index=? "
                    + "WHERE id=?";
    static final String SQL_UPDATE_STATE = "UPDATE waste_request SET state=? WHERE id=?";
    static final String SQL_DELETE = "DELETE FROM waste_request WHERE id=?";

    /** Додає запис; згенерований MySQL id записується в переданий об'єкт. */
    public void add(WasteRequest r) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, r);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<WasteRequest> getAll() throws SQLException {
        List<WasteRequest> list = new ArrayList<>();
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
    public WasteRequest getById(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_SELECT_BY_ID)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(WasteRequest r) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_UPDATE)) {
            int next = bind(p, r);
            p.setLong(next, r.getId());
            p.executeUpdate();
        }
    }

    /** Додаткова вузька операція: змінює лише стан заявки (без перевірки переходу). */
    public void updateState(long id, RequestState state) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(SQL_UPDATE_STATE)) {
            p.setString(1, state.name());
            p.setLong(2, id);
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

    private static int bind(PreparedStatement p, WasteRequest r) throws SQLException {
        int i = 1;
        p.setLong(i++, r.getContainerSiteId());
        p.setString(i++, r.getDescription());
        p.setDate(i++, Date.valueOf(r.getCreatedDate()));
        p.setDate(i++, Date.valueOf(r.getDueDate()));
        p.setString(i++, r.getState().name());
        p.setDouble(i++, r.getF1());
        p.setDouble(i++, r.getF2());
        p.setDouble(i++, r.getF3());
        p.setDouble(i++, r.getIntegralIndex());
        return i;
    }

    private static WasteRequest map(ResultSet rs) throws SQLException {
        return new WasteRequest(
                rs.getLong("id"),
                rs.getLong("container_site_id"),
                rs.getString("description"),
                rs.getDate("created_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                RequestState.valueOf(rs.getString("state")),
                rs.getDouble("f1"),
                rs.getDouble("f2"),
                rs.getDouble("f3"),
                rs.getDouble("integral_index"));
    }
}
