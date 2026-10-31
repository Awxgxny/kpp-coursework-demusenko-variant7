package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Виїзди транспортних засобів.
 *
 * GET  /api/trips            — список усіх виїздів;
 * GET  /api/trips?id=N       — один виїзд (404, якщо не знайдено);
 * POST /api/trips            — action=add | update | delete:
 *        add    — requestId, vehicleId, tripDate (рррр-мм-дд), collectedVolumeM3, notes (необов'язково);
 *        update — id та ті самі поля;
 *        delete — id.
 */
public class TripServlet extends ApiServlet {

    private final TripService service = new TripService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            if (req.getParameter("id") == null) {
                return service.getAll();
            }
            Trip t = service.getById(longParam(req, "id"));
            if (t == null) {
                throw new NotFoundException("Виїзд не знайдено");
            }
            return t;
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            String action = action(req);
            switch (action) {
                case "add": {
                    Trip t = readTrip(req, null);
                    service.add(t);
                    return ok(t.getId());
                }
                case "update": {
                    Trip t = readTrip(req, longParam(req, "id"));
                    service.update(t);
                    return ok(t.getId());
                }
                case "delete":
                    service.delete(longParam(req, "id"));
                    return ok();
                default:
                    throw unknownAction(action);
            }
        });
    }

    private static Trip readTrip(HttpServletRequest req, Long id) {
        String notes = req.getParameter("notes");
        return new Trip(id,
                longParam(req, "requestId"),
                longParam(req, "vehicleId"),
                dateParam(req, "tripDate"),
                doubleParam(req, "collectedVolumeM3"),
                notes == null || notes.isBlank() ? null : notes);
    }
}
