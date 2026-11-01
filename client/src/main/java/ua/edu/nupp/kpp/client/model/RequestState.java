package ua.edu.nupp.kpp.client.model;

/**
 * Стани заявки, що надходять від сервера. Правила переходів між станами визначає
 * лише серверний Service; клієнт використовує перелік тільки для відображення та вибору.
 */
public enum RequestState {
    CREATED("СТВОРЕНО"),
    PLANNED("ЗАПЛАНОВАНО"),
    IN_PROGRESS("ВИКОНУЄТЬСЯ"),
    DONE("ВИКОНАНО");

    private final String title;

    RequestState(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
