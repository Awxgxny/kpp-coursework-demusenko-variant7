package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ua.edu.nupp.kpp.client.model.ContainerSite;
import ua.edu.nupp.kpp.client.model.RequestState;
import ua.edu.nupp.kpp.client.model.Trip;
import ua.edu.nupp.kpp.client.model.Vehicle;
import ua.edu.nupp.kpp.client.model.WasteRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-клієнт JavaFX-застосунку. Виконує GET- і POST-запити до сервера (Servlet),
 * передає параметри у вигляді форми (UTF-8), а JSON-відповіді перетворює на клієнтські
 * моделі за допомогою ObjectMapper. SQL та підключення до MySQL тут відсутні.
 *
 * Адреса сервера береться з системної властивості kpp.server.url
 * (за замовчуванням http://localhost:8080/kpp-server/api).
 */
public class ApiClient {

    public static final String DEFAULT_BASE_URL = "http://localhost:8080/kpp-server/api";

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ApiClient() {
        this(System.getProperty("kpp.server.url", DEFAULT_BASE_URL));
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    // ------------------------------------------------------------------ перевірка сервера

    /** GET /ping: повертає true, якщо сервер відповів кодом 200. */
    public boolean ping() {
        try {
            get("/ping", new TypeReference<Map<String, Object>>() { });
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ контейнерні майданчики

    public List<ContainerSite> getSites() throws ApiException {
        return get("/sites", new TypeReference<List<ContainerSite>>() { });
    }

    public void addSite(ContainerSite s) throws ApiException {
        post("/sites", siteParams("add", s));
    }

    public void updateSite(ContainerSite s) throws ApiException {
        Map<String, String> p = siteParams("update", s);
        p.put("id", String.valueOf(s.getId()));
        post("/sites", p);
    }

    public void deleteSite(long id) throws ApiException {
        post("/sites", params("action", "delete", "id", String.valueOf(id)));
    }

    private static Map<String, String> siteParams(String action, ContainerSite s) {
        return params("action", action,
                "code", s.getCode(),
                "name", s.getName(),
                "address", s.getAddress(),
                "district", s.getDistrict(),
                "wasteType", s.getWasteType(),
                "containerCount", String.valueOf(s.getContainerCount()),
                "capacityL", String.valueOf(s.getCapacityL()),
                "fillLevel", String.valueOf(s.getFillLevel()),
                "sanitaryRisk", String.valueOf(s.getSanitaryRisk()));
    }

    // ------------------------------------------------------------------ транспортні засоби

    public List<Vehicle> getVehicles() throws ApiException {
        return get("/vehicles", new TypeReference<List<Vehicle>>() { });
    }

    public void addVehicle(Vehicle v) throws ApiException {
        post("/vehicles", vehicleParams("add", v));
    }

    public void updateVehicle(Vehicle v) throws ApiException {
        Map<String, String> p = vehicleParams("update", v);
        p.put("id", String.valueOf(v.getId()));
        post("/vehicles", p);
    }

    public void deleteVehicle(long id) throws ApiException {
        post("/vehicles", params("action", "delete", "id", String.valueOf(id)));
    }

    private static Map<String, String> vehicleParams(String action, Vehicle v) {
        return params("action", action,
                "plateNumber", v.getPlateNumber(),
                "model", v.getModel(),
                "capacityM3", String.valueOf(v.getCapacityM3()),
                "driverName", v.getDriverName());
    }

    // ------------------------------------------------------------------ заявки

    public List<WasteRequest> getRequests() throws ApiException {
        return get("/requests", new TypeReference<List<WasteRequest>>() { });
    }

    /** F1–F3 та I клієнт не передає: їх розраховує сервер. */
    public void addRequest(long containerSiteId, String description, LocalDate dueDate) throws ApiException {
        post("/requests", params("action", "add",
                "containerSiteId", String.valueOf(containerSiteId),
                "description", description,
                "dueDate", String.valueOf(dueDate)));
    }

    public void updateRequest(long id, long containerSiteId, String description, LocalDate dueDate)
            throws ApiException {
        post("/requests", params("action", "update",
                "id", String.valueOf(id),
                "containerSiteId", String.valueOf(containerSiteId),
                "description", description,
                "dueDate", String.valueOf(dueDate)));
    }

    public void recalculateRequest(long id) throws ApiException {
        post("/requests", params("action", "recalculate", "id", String.valueOf(id)));
    }

    /** Допустимість переходу перевіряє сервер; у разі заборони буде ApiException. */
    public void changeRequestState(long id, RequestState state) throws ApiException {
        post("/requests", params("action", "changeState", "id", String.valueOf(id), "state", state.name()));
    }

    public void deleteRequest(long id) throws ApiException {
        post("/requests", params("action", "delete", "id", String.valueOf(id)));
    }

    // ------------------------------------------------------------------ виїзди

    public List<Trip> getTrips() throws ApiException {
        return get("/trips", new TypeReference<List<Trip>>() { });
    }

    public void addTrip(Trip t) throws ApiException {
        post("/trips", tripParams("add", t));
    }

    public void updateTrip(Trip t) throws ApiException {
        Map<String, String> p = tripParams("update", t);
        p.put("id", String.valueOf(t.getId()));
        post("/trips", p);
    }

    public void deleteTrip(long id) throws ApiException {
        post("/trips", params("action", "delete", "id", String.valueOf(id)));
    }

    private static Map<String, String> tripParams(String action, Trip t) {
        return params("action", action,
                "requestId", String.valueOf(t.getRequestId()),
                "vehicleId", String.valueOf(t.getVehicleId()),
                "tripDate", String.valueOf(t.getTripDate()),
                "collectedVolumeM3", String.valueOf(t.getCollectedVolumeM3()),
                "notes", t.getNotes() == null ? "" : t.getNotes());
    }

    // ------------------------------------------------------------------ HTTP

    private <T> T get(String path, TypeReference<T> type) throws ApiException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();
        return parse(send(request), type);
    }

    private void post(String path, Map<String, String> form) throws ApiException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(encode(form), StandardCharsets.UTF_8))
                .build();
        send(request);
    }

    /** Виконує запит; відповідь з кодом не 2xx перетворює на ApiException із текстом помилки сервера. */
    private HttpResponse<String> send(HttpRequest request) throws ApiException {
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запит до сервера перервано", e);
        } catch (IOException e) {
            throw new ApiException("Сервер недоступний (" + baseUrl + "). Перевірте, чи запущено Tomcat.", e);
        }
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new ApiException(errorMessage(response.body(), status), status);
        }
        return response;
    }

    private <T> T parse(HttpResponse<String> response, TypeReference<T> type) throws ApiException {
        try {
            return mapper.readValue(response.body(), type);
        } catch (IOException e) {
            throw new ApiException("Некоректна відповідь сервера", e);
        }
    }

    private String errorMessage(String body, int status) {
        try {
            Map<String, Object> map = mapper.readValue(body, new TypeReference<Map<String, Object>>() { });
            Object message = map.get("error");
            if (message != null) {
                return message.toString();
            }
        } catch (IOException | RuntimeException e) {
            // тіло не є JSON — повертаємо загальне повідомлення нижче
        }
        return "Сервер повернув помилку (код " + status + ")";
    }

    private static Map<String, String> params(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private static String encode(Map<String, String> form) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : form.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
