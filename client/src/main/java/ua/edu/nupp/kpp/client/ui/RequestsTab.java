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
import ua.edu.nupp.kpp.client.model.ContainerSite;
import ua.edu.nupp.kpp.client.model.RequestState;
import ua.edu.nupp.kpp.client.model.WasteRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Вкладка «Заявки»: CRUD заявок, перерахунок показників та зміна стану життєвого циклу.
 * F1–F3 та I розраховує сервер; допустимість переходу між станами також перевіряє сервер.
 */
public class RequestsTab extends Tab {

    private final ApiClient api;
    private final TableView<WasteRequest> table = new TableView<>();
    private final Map<Long, ContainerSite> sitesById = new HashMap<>();

    private final ComboBox<ContainerSite> site = new ComboBox<>();
    private final TextField description = new TextField();
    private final DatePicker dueDate = new DatePicker();
    private final ComboBox<RequestState> state = new ComboBox<>(
            FXCollections.observableArrayList(RequestState.values()));

    public RequestsTab(ApiClient api) {
        super("Заявки");
        this.api = api;
        setClosable(false);

        table.getColumns().add(UiUtil.<WasteRequest, Long>column("№", 45, WasteRequest::getId));
        table.getColumns().add(UiUtil.<WasteRequest, String>column("Майданчик", 160, r -> siteTitle(r.getContainerSiteId())));
        table.getColumns().add(UiUtil.<WasteRequest, String>column("Опис", 200, WasteRequest::getDescription));
        table.getColumns().add(UiUtil.<WasteRequest, Object>column("Створено", 90, WasteRequest::getCreatedDate));
        table.getColumns().add(UiUtil.<WasteRequest, Object>column("Термін", 90, WasteRequest::getDueDate));
        table.getColumns().add(UiUtil.<WasteRequest, RequestState>column("Стан", 105, WasteRequest::getState));
        table.getColumns().add(UiUtil.<WasteRequest, String>column("F1", 55, r -> fmt(r.getF1())));
        table.getColumns().add(UiUtil.<WasteRequest, String>column("F2", 55, r -> fmt(r.getF2())));
        table.getColumns().add(UiUtil.<WasteRequest, String>column("F3", 55, r -> fmt(r.getF3())));
        table.getColumns().add(UiUtil.<WasteRequest, String>column("I", 60, r -> fmt(r.getIntegralIndex())));
        table.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> fillForm(r));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.setPadding(new Insets(8));
        form.add(new Label("Майданчик"), 0, 0);
        form.add(site, 1, 0);
        form.add(new Label("Опис"), 2, 0);
        form.add(description, 3, 0);
        form.add(new Label("Термін виконання"), 0, 1);
        form.add(dueDate, 1, 1);
        form.add(new Label("Новий стан"), 2, 1);
        form.add(state, 3, 1);
        site.setPrefWidth(240);
        description.setPrefWidth(300);

        Button add = new Button("Додати");
        add.setOnAction(e -> add());
        Button update = new Button("Змінити");
        update.setOnAction(e -> update());
        Button recalc = new Button("Перерахувати I");
        recalc.setOnAction(e -> recalculate());
        Button changeState = new Button("Змінити стан");
        changeState.setOnAction(e -> changeState());
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> delete());
        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> refresh());
        HBox buttons = new HBox(8, add, update, recalc, changeState, delete, refresh);
        buttons.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(new VBox(form, buttons));
        setContent(root);

        refresh();
    }

    private static String fmt(double v) {
        return String.format("%.1f", v);
    }

    private String siteTitle(long id) {
        ContainerSite s = sitesById.get(id);
        return s == null ? "#" + id : s.toString();
    }

    private void fillForm(WasteRequest r) {
        if (r == null) {
            return;
        }
        site.setValue(sitesById.get(r.getContainerSiteId()));
        description.setText(r.getDescription());
        dueDate.setValue(r.getDueDate());
        state.setValue(r.getState());
    }

    /** Завантажує майданчики (для списку та назв у таблиці) і заявки. */
    public void refresh() {
        try {
            List<ContainerSite> sites = api.getSites();
            sitesById.clear();
            for (ContainerSite s : sites) {
                sitesById.put(s.getId(), s);
            }
            site.setItems(FXCollections.observableArrayList(sites));
            table.setItems(FXCollections.observableArrayList(api.getRequests()));
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void add() {
        if (site.getValue() == null || dueDate.getValue() == null) {
            UiUtil.showWarning("Оберіть майданчик і термін виконання");
            return;
        }
        try {
            api.addRequest(site.getValue().getId(), description.getText(), dueDate.getValue());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void update() {
        WasteRequest selected = selected();
        if (selected == null) {
            return;
        }
        if (site.getValue() == null || dueDate.getValue() == null) {
            UiUtil.showWarning("Оберіть майданчик і термін виконання");
            return;
        }
        try {
            api.updateRequest(selected.getId(), site.getValue().getId(), description.getText(), dueDate.getValue());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void recalculate() {
        WasteRequest selected = selected();
        if (selected == null) {
            return;
        }
        try {
            api.recalculateRequest(selected.getId());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void changeState() {
        WasteRequest selected = selected();
        if (selected == null) {
            return;
        }
        if (state.getValue() == null) {
            UiUtil.showWarning("Оберіть новий стан заявки");
            return;
        }
        try {
            api.changeRequestState(selected.getId(), state.getValue());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e); // наприклад, заборонений перехід
        }
    }

    private void delete() {
        WasteRequest selected = selected();
        if (selected == null) {
            return;
        }
        try {
            api.deleteRequest(selected.getId());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private WasteRequest selected() {
        WasteRequest r = table.getSelectionModel().getSelectedItem();
        if (r == null) {
            UiUtil.showWarning("Оберіть заявку у таблиці");
        }
        return r;
    }
}
