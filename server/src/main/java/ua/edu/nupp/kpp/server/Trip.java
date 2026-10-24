package ua.edu.nupp.kpp.server;

import java.time.LocalDate;

/**
 * Виїзд транспортного засобу для виконання заявки (таблиця trip).
 */
public class Trip {

    private Long id;
    private long requestId;
    private long vehicleId;
    private LocalDate tripDate;
    private double collectedVolumeM3;
    private String notes;

    public Trip() {
    }

    public Trip(Long id, long requestId, long vehicleId, LocalDate tripDate,
                double collectedVolumeM3, String notes) {
        this.id = id;
        this.requestId = requestId;
        this.vehicleId = vehicleId;
        this.tripDate = tripDate;
        this.collectedVolumeM3 = collectedVolumeM3;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public long getRequestId() { return requestId; }
    public void setRequestId(long requestId) { this.requestId = requestId; }

    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }

    public LocalDate getTripDate() { return tripDate; }
    public void setTripDate(LocalDate tripDate) { this.tripDate = tripDate; }

    public double getCollectedVolumeM3() { return collectedVolumeM3; }
    public void setCollectedVolumeM3(double collectedVolumeM3) { this.collectedVolumeM3 = collectedVolumeM3; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Trip{id=" + id + ", requestId=" + requestId + ", vehicleId=" + vehicleId
                + ", tripDate=" + tripDate + "}";
    }
}
