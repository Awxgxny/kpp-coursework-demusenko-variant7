package ua.edu.nupp.kpp.client.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
import ua.edu.nupp.kpp.client.csv.ImportSitesTask;
import ua.edu.nupp.kpp.client.model.ContainerSite;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Вкладка «Майданчики»: CRUD контейнерних майданчиків та фоновий імпорт із CSV. */
public class SitesTab extends Tab {

    private static final Logger LOG = Logger.getLogger(SitesTab.class.getName());

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

    // керування імпортом CSV
    private final TextField csvPath = new TextField(CsvSiteReader.DEFAULT_FILE.toString());
    private final Button browseCsv = new Button("Огляд…");
    private final Button importCsv = new Button("Імпортувати CSV");
    private final ProgressBar progress = new ProgressBar(0);
    private final Label status = new Label("CSV не імпортовано");

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

        HBox buttons = new HBox(8, add, update, delete, refresh);
        buttons.setPadding(new Insets(8));

        browseCsv.setOnAction(e -> browseCsv());
        importCsv.setOnAction(e -> startImport());
        csvPath.setPrefWidth(320);
        HBox csvBox = new HBox(8, new Label("CSV-файл"), csvPath, browseCsv, importCsv);
        csvBox.setPadding(new Insets(8));
        progress.setPrefWidth(220);
        HBox progressBox = new HBox(8, progress, status);
        progressBox.setPadding(new Insets(0, 8, 8, 8));

        VBox bottom = new VBox(form, buttons, csvBox, progressBox);
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
            UiUtil.showWarning("Виберіть запис");
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
            UiUtil.showWarning("Виберіть запис");
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

    private void browseCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Оберіть CSV-файл із майданчиками");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        // відносний шлях sample-data/...: програму запускають з каталогу client/
        File dir = CsvSiteReader.DEFAULT_FILE.getParent().toFile();
        if (dir.isDirectory()) {
            chooser.setInitialDirectory(dir);
        }
        File file = chooser.showOpenDialog(getTabPane() == null ? null : getTabPane().getScene().getWindow());
        if (file != null) {
            csvPath.setText(file.getPath());
        }
    }

    /** Запускає імпорт у фоновому потоці; поки він триває, повторний запуск заблоковано. */
    private void startImport() {
        String text = csvPath.getText() == null ? "" : csvPath.getText().trim();
        if (text.isEmpty()) {
            UiUtil.showWarning("Вкажіть шлях до CSV-файлу");
            return;
        }
        Path file;
        try {
            file = Path.of(text);
        } catch (InvalidPathException e) {
            UiUtil.showWarning("Некоректний шлях до файла");
            return;
        }

        ImportSitesTask task = new ImportSitesTask(file, api);
        progress.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());
        setImportRunning(true);

        task.setOnSucceeded(e -> {
            unbindImport();
            int imported = task.getValue();
            List<String> problems = task.getProblems();
            progress.setProgress(imported + problems.size() > 0 ? 1.0 : 0.0);
            status.setText(imported == 0 && problems.isEmpty()
                    ? "Немає рядків для імпорту"
                    : "Готово. Імпортовано: " + imported + ", пропущено: " + problems.size());
            refresh();
            if (!problems.isEmpty()) {
                showImportReport(imported, problems);
            }
        });
        task.setOnFailed(e -> {
            unbindImport();
            Throwable error = task.getException();
            LOG.log(Level.WARNING, "CSV-імпорт завершився помилкою", error);
            status.setText("Помилка імпорту");
            refresh(); // сервер міг прийняти частину записів до збою
            String message = error == null || error.getMessage() == null
                    ? "Невідома помилка імпорту" : error.getMessage();
            UiUtil.showError(message);
        });

        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    /** Знімає прив'язки до Task: після bind() властивості не можна змінювати вручну. */
    private void unbindImport() {
        progress.progressProperty().unbind();
        status.textProperty().unbind();
        setImportRunning(false);
    }

    private void setImportRunning(boolean running) {
        importCsv.setDisable(running);
        browseCsv.setDisable(running);
        csvPath.setDisable(running);
    }

    private void showImportReport(int imported, List<String> problems) {
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
