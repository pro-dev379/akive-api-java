package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.plan.adapter.webapi.dailyplan.DailyPlanApi;
import net.akive.api.plan.adapter.webapi.sessionalplan.SessionalPlanApi;
import net.akive.api.plan.adapter.webapi.totalquantity.TotalQuantityApi;
import net.akive.api.plan.adapter.webapi.weeklyplan.WeeklyPlanApi;
import net.akive.api.plan.application.provided.dailyplan.DailyPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanCreator;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityCreator;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityModifier;
import net.akive.api.plan.application.provided.weeklyplan.WeeklyPlanFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(controllers = {
        SessionalPlanApi.class,
        DailyPlanApi.class,
        WeeklyPlanApi.class,
        TotalQuantityApi.class
})
public abstract class PrivateApiTestSupport {

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected DailyPlanFinder dailyPlanFinder;

    @MockitoBean
    protected SessionalPlanFinder sessionalPlanFinder;

    @MockitoBean
    protected WeeklyPlanFinder weeklyPlanFinder;

    @MockitoBean
    protected SessionalPlanCreator sessionalPlanCreator;

    @MockitoBean
    protected TotalQuantityCreator totalQuantityCreator;

    @MockitoBean
    protected TotalQuantityFinder totalQuantityFinder;

    @MockitoBean
    protected TotalQuantityModifier totalQuantityModifier;

    protected MvcTestResultAssert get(Long userId, String uri, Object... uriVariables) {
        return mvcTester.get()
                .uri(uri, uriVariables)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert postJson(Long userId, String uri, String json, Object... uriVariables) {
        return mvcTester.post()
                .uri(uri, uriVariables)
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert putJson(Long userId, String uri, String json, Object... uriVariables) {
        return mvcTester.put()
                .uri(uri, uriVariables)
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert patchJson(Long userId, String uri, String json, Object... uriVariables) {
        return mvcTester.patch()
                .uri(uri, uriVariables)
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert delete(Long userId, String uri, Object... uriVariables) {
        return mvcTester.delete()
                .uri(uri, uriVariables)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize request body.", e);
        }
    }
}
