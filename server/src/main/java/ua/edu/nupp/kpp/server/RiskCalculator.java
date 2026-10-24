package ua.edu.nupp.kpp.server;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Розрахунок факторів та інтегрального показника заявки на вивезення відходів:
 * I = 0,40·F1 + 0,35·F2 + 0,25·F3, усі фактори нормовані до діапазону 0..100.
 * <ul>
 *   <li>F1 — рівень заповнення контейнерів майданчика (%);</li>
 *   <li>F2 — санітарний ризик майданчика (бали 0..100);</li>
 *   <li>F3 — прострочення вивезення: 10 балів за кожну добу після строку, максимум 100.</li>
 * </ul>
 * Клас не працює з БД і не є моделлю: використовується Service-рівнем.
 */
public final class RiskCalculator {

    public static final double WEIGHT_F1 = 0.40;
    public static final double WEIGHT_F2 = 0.35;
    public static final double WEIGHT_F3 = 0.25;

    /** Скільки балів F3 додається за кожну добу прострочення. */
    public static final double OVERDUE_POINTS_PER_DAY = 10.0;

    private RiskCalculator() {
    }

    /** F1: рівень заповнення вже подається у відсотках 0..100. */
    public static double factor1(double fillLevel) {
        requireFactorRange(fillLevel, "Рівень заповнення");
        return fillLevel;
    }

    /** F2: санітарний ризик вже подається в балах 0..100. */
    public static double factor2(double sanitaryRisk) {
        requireFactorRange(sanitaryRisk, "Санітарний ризик");
        return sanitaryRisk;
    }

    /**
     * F3 = min(100; 10 · кількість діб після dueDate); якщо строк ще не минув, F3 = 0.
     *
     * @param dueDate строк вивезення
     * @param today   дата, на яку виконується розрахунок
     */
    public static double factor3(LocalDate dueDate, LocalDate today) {
        if (dueDate == null || today == null) {
            throw new IllegalArgumentException("Дати для розрахунку прострочення не задані");
        }
        long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
        if (overdueDays <= 0) {
            return 0.0;
        }
        return Math.min(100.0, overdueDays * OVERDUE_POINTS_PER_DAY);
    }

    /** I = 0,40·F1 + 0,35·F2 + 0,25·F3, округлено до двох знаків після коми. */
    public static double calculate(double f1, double f2, double f3) {
        requireFactorRange(f1, "F1");
        requireFactorRange(f2, "F2");
        requireFactorRange(f3, "F3");
        double result = WEIGHT_F1 * f1 + WEIGHT_F2 * f2 + WEIGHT_F3 * f3;
        return Math.round(result * 100.0) / 100.0;
    }

    private static void requireFactorRange(double value, String name) {
        // запис через !(...) відхиляє і NaN
        if (!(value >= 0 && value <= 100)) {
            throw new IllegalArgumentException(name + " має бути в діапазоні 0..100");
        }
    }
}
