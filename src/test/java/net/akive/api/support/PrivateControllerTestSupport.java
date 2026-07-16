package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.plan.application.port.in.CreateSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.DeleteSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.GetSessionalPlansUseCase;
import net.akive.api.plan.application.port.in.GetDailyTodosUseCase;
import net.akive.api.plan.application.port.in.GetSessionalPlanDetailUseCase;
import net.akive.api.plan.application.port.in.UpdateSessionalPlanUseCase;
import net.akive.api.plan.presentation.SessionalPlanController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SessionalPlanController.class)
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
    protected GetSessionalPlanDetailUseCase getSessionalPlanDetailUseCase;

    @MockitoBean
    protected CreateSessionalPlanUseCase createSessionalPlanUseCase;

    @MockitoBean
    protected UpdateSessionalPlanUseCase updateSessionalPlanUseCase;

    @MockitoBean
    protected DeleteSessionalPlanUseCase deleteSessionalPlanUseCase;
}
