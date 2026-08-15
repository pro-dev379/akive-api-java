package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.plan.apapter.webapi.DailyTodoController;
import net.akive.api.plan.apapter.webapi.SessionalPlanController;
import net.akive.api.plan.apapter.webapi.TotalQuantityController;
import net.akive.api.plan.apapter.webapi.WeeklyPlanController;
import net.akive.api.plan.application.provided.*;
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
public abstract class PrivateApiTestSupport {

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
    protected GetSessionalPlanTotalQuantityUseCase getSessionalPlanTotalQuantityUseCase;

    @MockitoBean
    protected CreateTotalQuantityUseCase createTotalQuantityUseCase;

    @MockitoBean
    protected DeleteTotalQuantityUseCase deleteTotalQuantityUseCase;
}
