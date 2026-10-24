package ua.edu.nupp.kpp.server;

/**
 * Контейнерний майданчик (таблиця container_site).
 * fillLevel (F1) і sanitaryRisk (F2) подаються у вже нормованому вигляді, 0–100.
 */
public class ContainerSite {

    private Long id;
    private String code;
    private String name;
    private String address;
    private String district;
    private String wasteType;
    private int containerCount;
    private int capacityL;
    private double fillLevel;
    private double sanitaryRisk;

    public ContainerSite() {
    }

    public ContainerSite(Long id, String code, String name, String address, String district,
                         String wasteType, int containerCount, int capacityL,
                         double fillLevel, double sanitaryRisk) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        this.district = district;
        this.wasteType = wasteType;
        this.containerCount = containerCount;
        this.capacityL = capacityL;
        this.fillLevel = fillLevel;
        this.sanitaryRisk = sanitaryRisk;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }

    public int getContainerCount() { return containerCount; }
    public void setContainerCount(int containerCount) { this.containerCount = containerCount; }

    public int getCapacityL() { return capacityL; }
    public void setCapacityL(int capacityL) { this.capacityL = capacityL; }

    public double getFillLevel() { return fillLevel; }
    public void setFillLevel(double fillLevel) { this.fillLevel = fillLevel; }

    public double getSanitaryRisk() { return sanitaryRisk; }
    public void setSanitaryRisk(double sanitaryRisk) { this.sanitaryRisk = sanitaryRisk; }

    @Override
    public String toString() {
        return "ContainerSite{id=" + id + ", code='" + code + "', name='" + name + "'}";
    }
}
