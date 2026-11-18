package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Модульні тести валідації у Service-рівні. Некоректні дані відхиляються ще до звернення до DAO,
 * тому тести не потребують MySQL.
 */
class ServiceValidationTest {

    private static ContainerSite site(String code, int containers, double fill, double risk) {
        return new ContainerSite(null, code, "Майданчик «Вокзал»", "вул. Фрунзе, 60", "Київський",
                "MIXED", containers, 5500, fill, risk);
    }

    @Test
    void siteWithBlankCodeIsRejected() {
        ContainerSiteService service = new ContainerSiteService();
        assertThrows(IllegalArgumentException.class, () -> service.add(site(" ", 5, 75, 65)));
    }

    @Test
    void siteWithFillLevelAbove100IsRejected() {
        ContainerSiteService service = new ContainerSiteService();
        assertThrows(IllegalArgumentException.class, () -> service.add(site("CS-900", 5, 140, 65)));
    }

    @Test
    void siteWithNegativeSanitaryRiskIsRejected() {
        ContainerSiteService service = new ContainerSiteService();
        assertThrows(IllegalArgumentException.class, () -> service.add(site("CS-900", 5, 75, -1)));
    }

    @Test
    void siteWithZeroContainersIsRejected() {
        ContainerSiteService service = new ContainerSiteService();
        assertThrows(IllegalArgumentException.class, () -> service.add(site("CS-900", 0, 75, 65)));
    }

    @Test
    void vehicleWithZeroCapacityIsRejected() {
        VehicleService service = new VehicleService();
        Vehicle v = new Vehicle(null, "AA1234BB", "МАЗ", 0, "Іваненко І.І.");
        assertThrows(IllegalArgumentException.class, () -> service.add(v));
    }

    @Test
    void requestWithBlankDescriptionIsRejected() {
        WasteRequestService service = new WasteRequestService(fixedClock());
        assertThrows(IllegalArgumentException.class,
                () -> service.add(1L, "  ", LocalDate.of(2026, 11, 10)));
    }

    @Test
    void requestDueBeforeCreationIsRejected() {
        WasteRequestService service = new WasteRequestService(fixedClock());
        assertThrows(IllegalArgumentException.class,
                () -> service.add(1L, "Вивезення сміття", LocalDate.of(2026, 11, 1)));
    }

    @Test
    void tripWithNegativeVolumeIsRejected() {
        TripService service = new TripService();
        Trip t = new Trip(null, 1L, 1L, LocalDate.of(2026, 11, 4), -5.0, null);
        assertThrows(IllegalArgumentException.class, () -> service.add(t));
    }

    private static Clock fixedClock() {
        ZoneId zone = ZoneOffset.UTC;
        return Clock.fixed(LocalDate.of(2026, 11, 4).atStartOfDay(zone).toInstant(), zone);
    }
}
