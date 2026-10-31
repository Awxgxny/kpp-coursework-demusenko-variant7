package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Транспортні засоби.
 *
 * GET  /api/vehicles         — список усіх транспортних засобів;
 * GET  /api/vehicles?id=N    — один транспортний засіб (404, якщо не знайдено);
 * POST /api/vehicles         — action=add | update | delete:
 *        add    — plateNumber, model, capacityM3, driverName;
 *        update — id та ті самі поля;
 *        delete — id.
 */
public class VehicleServlet extends ApiServlet {

    private final VehicleService service = new VehicleService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            if (req.getParameter("id") == null) {
                return service.getAll();
            }
            Vehicle v = service.getById(longParam(req, "id"));
            if (v == null) {
                throw new NotFoundException("Транспортний засіб не знайдено");
            }
            return v;
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            String action = action(req);
            switch (action) {
                case "add": {
                    Vehicle v = readVehicle(req, null);
                    service.add(v);
                    return ok(v.getId());
                }
                case "update": {
                    Vehicle v = readVehicle(req, longParam(req, "id"));
                    service.update(v);
                    return ok(v.getId());
                }
                case "delete":
                    service.delete(longParam(req, "id"));
                    return ok();
                default:
                    throw unknownAction(action);
            }
        });
    }

    private static Vehicle readVehicle(HttpServletRequest req, Long id) {
        return new Vehicle(id,
                text(req, "plateNumber"),
                text(req, "model"),
                doubleParam(req, "capacityM3"),
                text(req, "driverName"));
    }
}
