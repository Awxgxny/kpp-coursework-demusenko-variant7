package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Контейнерні майданчики.
 *
 * GET  /api/sites            — список усіх майданчиків (JSON-масив);
 * GET  /api/sites?id=N       — один майданчик (404, якщо не знайдено);
 * POST /api/sites            — action=add | update | delete:
 *        add    — code, name, address, district, wasteType, containerCount, capacityL,
 *                 fillLevel, sanitaryRisk;
 *        update — id та ті самі поля;
 *        delete — id.
 */
public class ContainerSiteServlet extends ApiServlet {

    private final ContainerSiteService service = new ContainerSiteService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            if (req.getParameter("id") == null) {
                return service.getAll();
            }
            ContainerSite s = service.getById(longParam(req, "id"));
            if (s == null) {
                throw new NotFoundException("Контейнерний майданчик не знайдено");
            }
            return s;
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(req, resp, () -> {
            String action = action(req);
            switch (action) {
                case "add": {
                    ContainerSite s = readSite(req, null);
                    service.add(s);
                    return ok(s.getId());
                }
                case "update": {
                    ContainerSite s = readSite(req, longParam(req, "id"));
                    service.update(s);
                    return ok(s.getId());
                }
                case "delete":
                    service.delete(longParam(req, "id"));
                    return ok();
                default:
                    throw unknownAction(action);
            }
        });
    }

    private static ContainerSite readSite(HttpServletRequest req, Long id) {
        return new ContainerSite(id,
                text(req, "code"),
                text(req, "name"),
                text(req, "address"),
                text(req, "district"),
                text(req, "wasteType"),
                intParam(req, "containerCount"),
                intParam(req, "capacityL"),
                doubleParam(req, "fillLevel"),
                doubleParam(req, "sanitaryRisk"));
    }
}
