package ua.edu.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ua.edu.nupp.kpp.client.ApiClient;
import ua.edu.nupp.kpp.client.ApiException;
import ua.edu.nupp.kpp.client.csv.CsvSiteReader;
import ua.edu.nupp.kpp.client.model.ContainerSite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Вкладка «Майданчики»: CRUD контейнерних майданчиків та початковий імпорт із CSV. */
public class SitesTab extends Tab {

    private final ApiClient api;
    private final TableView<ContainerSite> table = new TableView<>();

    private final TextField code = new TextField();
    private final TextField name = new TextField();
    private final TextField address = new TextField();
    private final TextField district = new TextField();
    private final TextField wasteType = new TextField("MIXED");
    private final TextField containerCount = new TextField();
    private final TextField capacityL = new TextField();
    private final TextField fillLevel = new TextField();
    private final TextField sanitaryRisk = new TextField();

    public SitesTab(ApiClient api) {
        super("Майданчики");
        this.api = api;
        setClosable(false);

        table.getColumns().add(UiUtil.<ContainerSite, Long>column("ID", 50, ContainerSite::getId));
        table.getColumns().add(UiUtil.<ContainerSite, String>column("Код", 80, ContainerSite::getCode));
        table.getColumns().add(UiUtil.<ContainerSite, String>column("Назва", 170, ContainerSite::getName));
        table.getColumns().add(UiUtil.<ContainerSite, String>column("Адреса", 170, ContainerSite::getAddress));
        table.getColumns().add(UiUtil.<ContainerSite, String>column("Район", 110, ContainerSite::getDistrict));
        table.getColumns().add(UiUtil.<ContainerSite, String>column("Тип", 80, ContainerSite::getWasteType));
        table.getColumns().add(UiUtil.<ContainerSite, Integer>column("К-сть", 55, ContainerSite::getContainerCount));
        table.getColumns().add(UiUtil.<ContainerSite, Integer>column("Об'єм, л", 75, ContainerSite::getCapacityL));
        table.getColumns().add(UiUtil.<ContainerSite, Double>column("Заповн., %", 85, ContainerSite::getFillLevel));
        table.getColumns().add(UiUtil.<ContainerSite, Double>column("Сан. ризик, %", 100, ContainerSite::getSanitaryRisk));
        table.getSelectionModel().selectedItemProperty().addListener((o, old, s) -> fillForm(s));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.setPadding(new Insets(8));
        addField(form, 0, 0, "Код", code);
        addField(form, 0, 2, "Назва", name);
        addField(form, 1, 0, "Адреса", address);
        addField(form, 1, 2, "Район", district);
        addField(form, 2, 0, "Тип відходів", wasteType);
        addField(form, 2, 2, "К-сть контейнерів", containerCount);
        addField(form, 3, 0, "Об'єм, л", capacityL);
        addField(form, 3, 2, "Заповненість, %", fillLevel);
        addField(form, 4, 0, "Сан. ризик, %", sanitaryRisk);

        Button add = new Button("Додати");
        add.setOnAction(e -> add());
        Button update = new Button("Змінити");
        update.setOnAction(e -> update());
        Button delete = new Button("Видалити");
        delete.setOnAction(e -> delete());
        Button refresh = new Button("Оновити");
        refresh.setOnAction(e -> refresh());
        Button importCsv = new Button("Імпорт з CSV…");
        importCsv.setOnAction(e -> importCsv());

        HBox buttons = new HBox(8, add, update, delete, refresh, importCsv);
        buttons.setPadding(new Insets(8));

        VBox bottom = new VBox(form, buttons);
        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(bottom);
        setContent(root);

        refresh();
    }

    private static void addField(GridPane g, int row, int col, String label, TextField f) {
        g.add(new Label(label), col, row);
        g.add(f, col + 1, row);
        f.setPrefWidth(240);
    }

    private void fillForm(ContainerSite s) {
        if (s == null) {
            return;
        }
        code.setText(s.getCode());
        name.setText(s.getName());
        address.setText(s.getAddress());
        district.setText(s.getDistrict());
        wasteType.setText(s.getWasteType());
        containerCount.setText(String.valueOf(s.getContainerCount()));
        capacityL.setText(String.valueOf(s.getCapacityL()));
        fillLevel.setText(String.valueOf(s.getFillLevel()));
        sanitaryRisk.setText(String.valueOf(s.getSanitaryRisk()));
    }

    /** Збирає майданчик із полів форми; перевіряє лише, що числа записані як числа. */
    private ContainerSite readForm() {
        ContainerSite s = new ContainerSite();
        s.setCode(code.getText());
        s.setName(name.getText());
        s.setAddress(address.getText());
        s.setDistrict(district.getText());
        s.setWasteType(wasteType.getText());
        s.setContainerCount(UiUtil.parseInt(containerCount.getText(), "К-сть контейнерів"));
        s.setCapacityL(UiUtil.parseInt(capacityL.getText(), "Об'єм"));
        s.setFillLevel(UiUtil.parseDouble(fillLevel.getText(), "Заповненість"));
        s.setSanitaryRisk(UiUtil.parseDouble(sanitaryRisk.getText(), "Сан. ризик"));
        return s;
    }

    public void refresh() {
        try {
            table.setItems(FXCollections.observableArrayList(api.getSites()));
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void add() {
        try {
            api.addSite(readForm());
            refresh();
        } catch (IllegalArgumentException e) {
            UiUtil.showWarning(e.getMessage());
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void update() {
        ContainerSite selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.showWarning("Оберіть майданчик у таблиці");
            return;
        }
        try {
            ContainerSite s = readForm();
            s.setId(selected.getId());
            api.updateSite(s);
            refresh();
        } catch (IllegalArgumentException e) {
            UiUtil.showWarning(e.getMessage());
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    private void delete() {
        ContainerSite selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiUtil.showWarning("Оберіть майданчик у таблиці");
            return;
        }
        try {
            api.deleteSite(selected.getId());
            refresh();
        } catch (ApiException e) {
            UiUtil.showError(e);
        }
    }

    // ---------------------------------------------------------------- імпорт CSV

    private void importCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Оберіть CSV-файл із майданчиками");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File dir = new File("sample-data"); // відносний шлях: папка запуску client/
        if (dir.isDirectory()) {
            chooser.setInitialDirectory(dir);
        }
        File file = chooser.showOpenDialog(getTabPane() == null ? null : getTabPane().getScene().getWindow());
        if (file == null) {
            return;
        }

        CsvSiteReader.Result parsed;
        try {
            parsed = new CsvSiteReader().read(file.toPath());
        } catch (IOException e) {
            UiUtil.showWarning("Не вдалося прочитати файл: " + e.getMessage());
            return;
        }

        List<String> problems = new ArrayList<>(parsed.getErrors());
        int imported = 0;
        for (CsvSiteReader.Row row : parsed.getRows()) {
            try {
                api.addSite(row.getSite());
                imported++;
            } catch (ApiException e) {
                problems.add("Рядок " + row.getLine() + ": " + e.getMessage());
            }
        }
        refresh();
        showImportReport(imported, problems);
    }

    private void showImportReport(int imported, List<String> problems) {
        if (problems.isEmpty()) {
            UiUtil.showInfo("Імпортовано майданчиків: " + imported);
            return;
        }
        TextArea area = new TextArea("Імпортовано: " + imported + "\nПропущено: " + problems.size()
                + "\n\n" + String.join("\n", problems));
        area.setEditable(false);
        area.setPrefSize(560, 260);
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setTitle("Результат імпорту");
        a.setHeaderText(null);
        a.getDialogPane().setContent(area);
        a.showAndWait();
    }
}
