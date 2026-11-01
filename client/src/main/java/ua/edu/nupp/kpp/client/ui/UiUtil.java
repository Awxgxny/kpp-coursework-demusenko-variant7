package ua.edu.nupp.kpp.client.ui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import ua.edu.nupp.kpp.client.ApiException;

import java.util.function.Function;

/** Спільні допоміжні методи інтерфейсу. */
final class UiUtil {

    private UiUtil() {
    }

    static <S, T> TableColumn<S, T> column(String title, double width, Function<S, T> getter) {
        TableColumn<S, T> c = new TableColumn<>(title);
        c.setPrefWidth(width);
        c.setCellValueFactory(cd -> new SimpleObjectProperty<>(getter.apply(cd.getValue())));
        return c;
    }

    static void showError(ApiException e) {
        show(Alert.AlertType.ERROR, "Помилка", e.getMessage());
    }

    static void showWarning(String message) {
        show(Alert.AlertType.WARNING, "Увага", message);
    }

    static void showInfo(String message) {
        show(Alert.AlertType.INFORMATION, "Повідомлення", message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    static int parseInt(String v, String field) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + field + "» має бути цілим числом");
        }
    }

    static double parseDouble(String v, String field) {
        try {
            return Double.parseDouble(v.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + field + "» має бути числом");
        }
    }
}
