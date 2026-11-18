package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Модульні тести правил переходів життєвого циклу заявки: CREATED → PLANNED → IN_PROGRESS → DONE. */
class RequestLifecycleTest {

    @Test
    void createdToPlannedAllowed() {
        assertTrue(WasteRequestService.isAllowedTransition(RequestState.CREATED, RequestState.PLANNED));
    }

    @Test
    void plannedToInProgressAllowed() {
        assertTrue(WasteRequestService.isAllowedTransition(RequestState.PLANNED, RequestState.IN_PROGRESS));
    }

    @Test
    void inProgressToDoneAllowed() {
        assertTrue(WasteRequestService.isAllowedTransition(RequestState.IN_PROGRESS, RequestState.DONE));
    }

    @Test
    void createdToDoneRejected() {
        assertFalse(WasteRequestService.isAllowedTransition(RequestState.CREATED, RequestState.DONE));
    }

    @Test
    void backwardTransitionRejected() {
        assertFalse(WasteRequestService.isAllowedTransition(RequestState.PLANNED, RequestState.CREATED));
        assertFalse(WasteRequestService.isAllowedTransition(RequestState.DONE, RequestState.IN_PROGRESS));
    }

    @Test
    void doneIsFinalState() {
        for (RequestState next : RequestState.values()) {
            assertFalse(WasteRequestService.isAllowedTransition(RequestState.DONE, next));
        }
    }

    @Test
    void stateCannotStayTheSame() {
        for (RequestState state : RequestState.values()) {
            assertFalse(WasteRequestService.isAllowedTransition(state, state));
        }
    }
}
