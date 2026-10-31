package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Заявки на вивезення відходів (об'єкт життєвого циклу).
 * F1, F2, F3 та інтегральний показник I розраховує сервер; клієнт їх не передає.
 *
 * GET  /api/requests          — список усіх заявок (разом із f1, f2, f3, integralIndex, state);
 * GET  /api/requests?id=N     — одна заявка (404, якщо не знайдено);
 * POST /api/requests          — action=add | update | recalculate | changeState | delete:
 *        add         — containerSiteId, description, dueDate (рррр-мм-дд);
 *        update      — id, containerSiteId, description, dueDate;
 *        recalculate — id (перерахунок F1–F3 та I на поточну дату);
 *        changeState — id, state (CREATED | PLANNED | IN_PROGRESS | DONE);
 *        delete      — id.
 */
public class WasteRequestServlet extends ApiServlet {

    private final WasteRequestService service = new WasteRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            if (req.getParameter("id") == null) {
                return service.getAll();
            }
            WasteRequest r = service.getById(longParam(req, "id"));
            if (r == null) {
                throw new NotFoundException("Заявку не знайдено");
            }
            return r;
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            String action = action(req);
            switch (action) {
                case "add":
                    service.add(longParam(req, "containerSiteId"), text(req, "description"),
                            dateParam(req, "dueDate"));
                    return ok();
                case "update":
                    service.update(longParam(req, "id"), longParam(req, "containerSiteId"),
                            text(req, "description"), dateParam(req, "dueDate"));
                    return ok();
                case "recalculate":
                    service.recalculate(longParam(req, "id"));
                    return ok();
                case "changeState":
                    service.changeState(longParam(req, "id"), stateParam(req));
                    return ok();
                case "delete":
                    service.delete(longParam(req, "id"));
                    return ok();
                default:
                    throw unknownAction(action);
            }
        });
    }

    private static RequestState stateParam(HttpServletRequest req) {
        String v = req.getParameter("state");
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Параметр 'state' є обов'язковим");
        }
        try {
            return RequestState.valueOf(v.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан заявки: " + v);
        }
    }
}
