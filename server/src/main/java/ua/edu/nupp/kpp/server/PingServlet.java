package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * GET /api/ping — перевірка доступності сервера (без звернення до бази даних).
 * Відповідь: {"status":"ok","service":"kpp-server"}.
 */
public class PingServlet extends ApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> Map.of("status", "ok", "service", "kpp-server"));
    }
}
