package ua.edu.nupp.kpp.client;

import javafx.application.Application;

/**
 * Окремий клас запуску: main не міститься в класі, що розширює Application,
 * тому застосунок стартує і з classpath, і зі зібраного jar без module-info.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
