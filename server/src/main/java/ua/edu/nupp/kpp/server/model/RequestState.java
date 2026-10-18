package ua.edu.nupp.kpp.server.model;

/**
 * Стани життєвого циклу заявки на вивезення відходів:
 * СТВОРЕНО → ЗАПЛАНОВАНО → ВИКОНУЄТЬСЯ → ВИКОНАНО.
 * Порядок констант відповідає допустимій послідовності переходів.
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
}
