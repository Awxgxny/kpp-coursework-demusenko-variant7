package ua.edu.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ua.edu.nupp.kpp.client.ApiClient;
import ua.edu.nupp.kpp.client.ApiException;
import ua.edu.nupp.kpp.client.model.Trip;
import ua.edu.nupp.kpp.client.model.Vehicle;
import ua.edu.nupp.kpp.client.model.WasteRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Вкладка «Виїзди»: CRUD виїздів, що прив'язують заявку до транспортного засобу. */
public class TripsTab extends Tab {

    private final ApiClient api;
    private final TableView<Trip> table = new TableView<>();
    private final Map<Long, WasteRequest> requestsById = new HashMap<>();
    private final Map<Long, Vehicle> vehiclesById = new HashMap<>();

    private final ComboBox<WasteRequest> request = new ComboBox<>();
    private final ComboBox<Vehicle> vehicle = new ComboBox<>();
    private final DatePicker tripDate = new DatePicker();
    private final TextField volume = new TextField();
    private final TextField notes = new TextField();

    public TripsTab(ApiClient api) {
        super("Виїзди");
        this.api = api;
        setClosable(false);

        table.getColumns().add(UiUtil.<Trip, Long>column("ID", 45, Trip::getId));
        table.getColumns().add(UiUtil.<Trip, String>column("Заявка", 190, t -> label(requestsById.get(t.getRequestId()), t.getRequestId())));
        table.getColumns().add(UiUtil.<Trip, String>column("Транспорт", 190, t -> label(vehiclesById.get(t.getVehicleId()), t.getVehicleId())));
        table.getColumns().add(UiUtil.<Trip, Object>column("Дата", 90, Trip::getTripDate));
        table.getColumns().add(UiUtil.<Trip, Double>column("Обсяг, м³", 85, Trip::getCollectedVolumeM3));
        table.getColumns().add(UiUtil.<Trip, String>column("Примітки", 200, Trip::getNotes));
        table.getSelectionModel().selectedItemProperty().addListener((o, old, t) -> fillForm(t));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.setPadding(new Insets(8));
        form.add(new Label("Заявка"), 0, 0);
        form.add(request, 1, 0);
        form.add(new Label("Транспорт"), 2, 0);
        form.add(vehicle, 3, 0);
        form.add(new Label("Дата виїзду"), 0, 1);
        form.add(tripDate, 1, 1);
        form.add(new Label("Обсяг, м³"), 2, 1);
        form.add(volume, 3, 1);
        form.add(new Label("Примітки"), 0, 2);
        form.add(notes, 1, 2);
        request.setPrefWidth(240);
        vehicle.setPrefWidth(240);

        Button add = new Button("Додати");
        add.setOnAction(e -> add());
        Button update = new Button("Змінити");
        update.setOnAction(e -> update());
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> delete());
        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> refresh());
        HBox buttons = new HBox(8, add, update, delete, refresh);
        buttons.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(new VBox(form, buttons));
        setContent(root);

        refresh();
    }

    private static String label(Object o, long id) {
        return o == null ? "#" + id : o.toString();
    }

    private void fillForm(Trip t) {
        if (t == null) {
            return;
        }
        request.setValue(requestsById.get(t.getRequestId()));
        vehicle.setValue(vehiclesById.get(t.getVehicleId()));
        tripDate.setValue(t.getTripDate());
        volume.setText(String.valueOf(t.getCollectedVolumeM3()));
        notes.setText(t.getNotes() == null ? "" : t.getNotes());
    }

    private Trip readForm() {
        if (request.getValue() == null || vehicle.getValue() == null || tripDate.getValue() == null) {
            throw new IllegalArgumentException("Оберіть заявку, транспорт і дату виїзду");
        }
        Trip t = new Trip();
        t.setRequestId(request.getValue().getId());
        t.setVehicleId(vehicle.getValue().getId());
        t.setTripDate(tripDate.getValue());
        t.setCollectedVolumeM3(UiUtil.parseDouble(volume.getText(), "Обсяг"));
        t.setNotes(notes.getText());
        return t;
    }

    public void refresh() {
        try {
            List<WasteRequest> requests = api.getRequests();
            List<Vehicle> vehicles = api.getVehicles();
            requestsById.clear();
            vehiclesById.clear();
            requests.forEach(r -> requestsById.put(r.getId(), r));
            vehicles.forEach(v -> vehiclesById.put(v.getId(), v));
            request.setItems(FXCollections.observableArrayList(requests));
            vehicle.setItems(FXCollections.observableArrayList(vehicles));
            table.setItems(FXCollections.observableArrayList(api.getTrips()));
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void add() {
        try {
            api.addTrip(readForm());
            refresh();
        } catch (IllegalArgumentException e) {
            UiUtil.showWarning(e.getMessage());
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void update() {
        Trip selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.showWarning("Оберіть виїзд у таблиці");
            return;
        }
        try {
            Trip t = readForm();
            t.setId(selected.getId());
            api.updateTrip(t);
            refresh();
        } catch (IllegalArgumentException e) {
            UiUtil.showWarning(e.getMessage());
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void delete() {
        Trip selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.showWarning("Оберіть виїзд у таблиці");
            return;
        }
        try {
            api.deleteTrip(selected.getId());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }
}
