package ua.edu.nupp.kpp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

/**
 * Заявка на вивезення відходів (об'єкт життєвого циклу).
 * Поля f1, f2, f3 та integralIndex розраховує сервер.
 * Клієнтська модель: лише дані, отримані від сервера у форматі JSON; без DAO та JDBC.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WasteRequest {

    private Long id;
    private long containerSiteId;
    private String description;
    private LocalDate createdDate;
    private LocalDate dueDate;
    private RequestState state;
    private double f1;
    private double f2;
    private double f3;
    private double integralIndex;

    public WasteRequest() {
    }

    public WasteRequest(Long id,
            long containerSiteId,
            String description,
            LocalDate createdDate,
            LocalDate dueDate,
            RequestState state,
            double f1,
            double f2,
            double f3,
            double integralIndex) {
        this.id = id;
        this.containerSiteId = containerSiteId;
        this.description = description;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.state = state;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.integralIndex = integralIndex;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getContainerSiteId() { return containerSiteId; }
    public void setContainerSiteId(long containerSiteId) { this.containerSiteId = containerSiteId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public RequestState getState() { return state; }
    public void setState(RequestState state) { this.state = state; }

    public double getF1() { return f1; }
    public void setF1(double f1) { this.f1 = f1; }

    public double getF2() { return f2; }
    public void setF2(double f2) { this.f2 = f2; }

    public double getF3() { return f3; }
    public void setF3(double f3) { this.f3 = f3; }

    public double getIntegralIndex() { return integralIndex; }
    public void setIntegralIndex(double integralIndex) { this.integralIndex = integralIndex; }

    /** Текст для списків вибору та підписів. */
    @Override
    public String toString() {
        return "Заявка №" + id + " (" + description + ")";
    }
}
