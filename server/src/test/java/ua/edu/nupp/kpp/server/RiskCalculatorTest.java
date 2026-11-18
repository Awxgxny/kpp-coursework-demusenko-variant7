package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Модульні тести розрахунку факторів F1–F3 та інтегрального показника (без MySQL і Tomcat). */
class RiskCalculatorTest {

    private static final double DELTA = 1e-9;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 4);

    @Test
    void factor1EqualsFillLevel() {
        assertEquals(75.0, RiskCalculator.factor1(75.0), DELTA);
    }

    @Test
    void factor3IsZeroWhenNotOverdue() {
        assertEquals(0.0, RiskCalculator.factor3(TODAY, TODAY), DELTA);
        assertEquals(0.0, RiskCalculator.factor3(TODAY.plusDays(5), TODAY), DELTA);
    }

    @Test
    void factor3IsTenPointsPerOverdueDay() {
        // строк 01.11, сьогодні 04.11: 3 доби прострочення → 30 балів
        assertEquals(30.0, RiskCalculator.factor3(LocalDate.of(2026, 11, 1), TODAY), DELTA);
    }

    @Test
    void factor3IsCappedAt100() {
        assertEquals(100.0, RiskCalculator.factor3(TODAY.minusDays(25), TODAY), DELTA);
    }

    @Test
    void integralIndexForControlDataIs60_5() {
        // 0,40·80 + 0,35·60 + 0,25·30 = 32 + 21 + 7,5
        assertEquals(60.5, RiskCalculator.calculate(80, 60, 30), DELTA);
    }

    @Test
    void integralIndexUsesWeights040_035_025() {
        assertEquals(40.0, RiskCalculator.calculate(100, 0, 0), DELTA);
        assertEquals(35.0, RiskCalculator.calculate(0, 100, 0), DELTA);
        assertEquals(25.0, RiskCalculator.calculate(0, 0, 100), DELTA);
    }

    @Test
    void integralIndexStaysWithinZeroAndHundred() {
        assertEquals(0.0, RiskCalculator.calculate(0, 0, 0), DELTA);
        assertEquals(100.0, RiskCalculator.calculate(100, 100, 100), DELTA);
    }

    @Test
    void factorAbove100IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(101, 10, 10));
    }

    @Test
    void negativeFactorIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor2(-1));
    }

    @Test
    void notANumberFactorIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor1(Double.NaN));
    }
}
