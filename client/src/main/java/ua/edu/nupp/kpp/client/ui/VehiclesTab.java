package ua.edu.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
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
import ua.edu.nupp.kpp.client.model.Vehicle;

/** Вкладка «Транспорт»: CRUD транспортних засобів. */
public class VehiclesTab extends Tab {

    private final ApiClient api;
    private final TableView<Vehicle> table = new TableView<>();

    private final TextField plate = new TextField();
    private final TextField model = new TextField();
    private final TextField capacity = new TextField();
    private final TextField driver = new TextField();

    public VehiclesTab(ApiClient api) {
        super("Транспорт");
        this.api = api;
        setClosable(false);

        table.getColumns().add(UiUtil.<Vehicle, Long>column("ID", 50, Vehicle::getId));
        table.getColumns().add(UiUtil.<Vehicle, String>column("Номер", 120, Vehicle::getPlateNumber));
        table.getColumns().add(UiUtil.<Vehicle, String>column("Модель", 200, Vehicle::getModel));
        table.getColumns().add(UiUtil.<Vehicle, Double>column("Місткість, м³", 110, Vehicle::getCapacityM3));
        table.getColumns().add(UiUtil.<Vehicle, String>column("Водій", 200, Vehicle::getDriverName));
        table.getSelectionModel().selectedItemProperty().addListener((o, old, v) -> fillForm(v));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.setPadding(new Insets(8));
        form.add(new Label("Номер"), 0, 0);
        form.add(plate, 1, 0);
        form.add(new Label("Модель"), 2, 0);
        form.add(model, 3, 0);
        form.add(new Label("Місткість, м³"), 0, 1);
        form.add(capacity, 1, 1);
        form.add(new Label("Водій"), 2, 1);
        form.add(driver, 3, 1);

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

    private void fillForm(Vehicle v) {
        if (v == null) {
            return;
        }
        plate.setText(v.getPlateNumber());
        model.setText(v.getModel());
        capacity.setText(String.valueOf(v.getCapacityM3()));
        driver.setText(v.getDriverName());
    }

    private Vehicle readForm() {
        Vehicle v = new Vehicle();
        v.setPlateNumber(plate.getText());
        v.setModel(model.getText());
        v.setCapacityM3(UiUtil.parseDouble(capacity.getText(), "Місткість"));
        v.setDriverName(driver.getText());
        return v;
    }

    public void refresh() {
        try {
            table.setItems(FXCollections.observableArrayList(api.getVehicles()));
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void add() {
        try {
            api.addVehicle(readForm());
            refresh();
        } catch (IllegalArgumentException e) {
            UiUtil.showWarning(e.getMessage());
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void update() {
        Vehicle selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.showWarning("Оберіть транспортний засіб у таблиці");
            return;
        }
        try {
            Vehicle v = readForm();
            v.setId(selected.getId());
            api.updateVehicle(v);
            refresh();
        } catch (IllegalArgumentException e) {
            UiUtil.showWarning(e.getMessage());
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void delete() {
        Vehicle selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.showWarning("Оберіть транспортний засіб у таблиці");
            return;
        }
        try {
            api.deleteVehicle(selected.getId());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }
}
