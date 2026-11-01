package ua.edu.nupp.kpp.client;

/**
 * Помилка взаємодії із сервером: або сервер повернув код помилки з повідомленням
 * {"error": "..."}, або сервер недоступний / відповідь не вдалося прочитати.
 */
public class ApiException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.status = -1;
    }

    /** HTTP-код відповіді сервера або -1, якщо відповіді не було. */
    public int getStatus() {
        return status;
    }
}
