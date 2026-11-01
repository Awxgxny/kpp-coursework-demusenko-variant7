package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ua.edu.nupp.kpp.client.ui.SitesTab;

/** Головне вікно JavaFX-клієнта: чотири вкладки, що працюють із сервером через HTTP/JSON. */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        ApiClient api = new ApiClient();

        TabPane tabs = new TabPane();
        SitesTab sites = new SitesTab(api);
        tabs.getTabs().addAll(sites);


        stage.setTitle("Заявки на вивезення відходів — сервер: " + api.getBaseUrl());
        stage.setScene(new Scene(tabs, 1050, 640));
        stage.show();
    }
}
