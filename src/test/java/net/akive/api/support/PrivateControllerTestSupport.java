package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.plan.application.port.in.*;
import net.akive.api.plan.presentation.DailyTodoController;
import net.akive.api.plan.presentation.SessionalPlanController;
import net.akive.api.plan.presentation.TotalQuantityController;
import net.akive.api.plan.presentation.WeeklyPlanController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        SessionalPlanController.class,
        DailyTodoController.class,
        WeeklyPlanController.class,
        TotalQuantityController.class
})
public abstract class PrivateControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected GetSessionalPlansUseCase getSessionalPlansUseCase;

    @MockitoBean
    protected GetDailyTodosUseCase getDailyTodosUseCase;

    @MockitoBean
    protected GetSessionalPlanUseCase getSessionalPlanUseCase;

    @MockitoBean
    protected GetWeeklyPlanUseCase getWeeklyPlanUseCase;

    @MockitoBean
    protected CreateSessionalPlanUseCase createSessionalPlanUseCase;

    @MockitoBean
    protected UpdateSessionalPlanUseCase updateSessionalPlanUseCase;

    @MockitoBean
    protected DeleteSessionalPlanUseCase deleteSessionalPlanUseCase;

    @MockitoBean
    protected CreateTotalQuantityUseCase createTotalQuantityUseCase;
}
