package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Базовий клас HTTP/JSON-сервлетів. Містить лише протокольні речі:
 * читання параметрів запиту, серіалізацію відповіді в JSON та перетворення помилок
 * у HTTP-коди. SQL і предметної логіки тут немає: уся робота виконується в Service.
 *
 * Успішна відповідь: код 200 і JSON-тіло. Помилка: JSON {"error": "повідомлення"}
 * з кодом 400 (некоректні дані), 404 (не знайдено), 409 (порушення цілісності БД)
 * або 500 (внутрішня помилка).
 */
public abstract class ApiServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ApiServlet.class.getName());

    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** Операція сервлета, яка повертає об'єкт для серіалізації в JSON. */
    @FunctionalInterface
    protected interface Action {
        Object run() throws Exception;
    }

    /** Запитаний запис не знайдено (HTTP 404). */
    protected static class NotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public NotFoundException(String message) {
            super(message);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // параметри POST-форми читаємо як UTF-8 (потрібно виконати до першого getParameter)
        req.setCharacterEncoding("UTF-8");
        super.service(req, resp);
    }

    /** Виконує операцію та записує результат або повідомлення про помилку у відповідь. */
    protected void respond(HttpServletRequest req, HttpServletResponse resp, Action action)
            throws IOException {
        try {
            Object result = action.run();
            write(resp, HttpServletResponse.SC_OK, result);
        } catch (NotFoundException e) {
            LOG.warning(req.getMethod() + " " + req.getRequestURI() + " -> 404: " + e.getMessage());
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            LOG.warning(req.getMethod() + " " + req.getRequestURI() + " -> 400: " + e.getMessage());
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                LOG.warning(req.getMethod() + " " + req.getRequestURI() + " -> 409: " + e.getMessage());
                writeError(resp, HttpServletResponse.SC_CONFLICT, integrityMessage(e));
            } else {
                LOG.log(Level.SEVERE, "Помилка доступу до бази даних", e);
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Помилка доступу до бази даних");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Внутрішня помилка сервера", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Внутрішня помилка сервера");
        }
    }

    private static String integrityMessage(SQLException e) {
        switch (e.getErrorCode()) {
            case 1062:
                return "Запис з таким унікальним значенням уже існує";
            case 1451:
                return "Неможливо видалити запис: на нього посилаються інші записи";
            case 1452:
                return "Пов'язаний запис не існує";
            default:
                return "Порушення цілісності даних";
        }
    }

    protected static Map<String, Object> ok() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", "ok");
        return m;
    }

    protected static Map<String, Object> ok(long id) {
        Map<String, Object> m = ok();
        m.put("id", id);
        return m;
    }

    private static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(MAPPER.writeValueAsString(body));
    }

    private static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", message);
        write(resp, status, m);
    }

    // ------------------------------------------------------------ читання параметрів

    /** Текстовий параметр; може бути null: його коректність перевіряє Service. */
    protected static String text(HttpServletRequest req, String name) {
        return req.getParameter(name);
    }

    protected static long longParam(HttpServletRequest req, String name) {
        String v = required(req, name);
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути цілим числом");
        }
    }

    protected static int intParam(HttpServletRequest req, String name) {
        String v = required(req, name);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути цілим числом");
        }
    }

    protected static double doubleParam(HttpServletRequest req, String name) {
        String v = required(req, name);
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути числом");
        }
    }

    /** Дата у форматі рррр-мм-дд. */
    protected static LocalDate dateParam(HttpServletRequest req, String name) {
        String v = required(req, name);
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути датою у форматі рррр-мм-дд");
        }
    }

    /** Необов'язковий цілий параметр: null, якщо не передано. */
    protected static Long optionalLong(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) {
            return null;
        }
        return longParam(req, name);
    }

    protected static String action(HttpServletRequest req) {
        String a = req.getParameter("action");
        if (a == null || a.isBlank()) {
            throw new IllegalArgumentException("Параметр 'action' є обов'язковим");
        }
        return a;
    }

    protected static IllegalArgumentException unknownAction(String action) {
        return new IllegalArgumentException("Невідома дія: " + action);
    }

    private static String required(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Параметр '" + name + "' є обов'язковим");
        }
        return v.trim();
    }
}
