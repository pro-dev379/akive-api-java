package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.common.exception.ApplicationError;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc(addFilters = false)
public abstract class PublicApiTestSupport {

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected LegalRequirementChecker legalRequirementChecker;

    protected MvcTestResultAssert postJson(String url, Object body) throws Exception {
        return postJson(url, objectMapper.writeValueAsString(body));
    }

    protected MvcTestResultAssert postJson(String url, String json) {
        return mvcTester.post()
                .uri(url)
                .contentType(APPLICATION_JSON)
                .content(json)
                .assertThat();
    }

    protected MvcTestResultAssert patchJson(String uri, String json, Object... uriVariables) {
        return mvcTester.patch()
                .uri(uri, uriVariables)
                .contentType(APPLICATION_JSON)
                .content(json)
                .assertThat();
    }

    protected String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize request body.", e);
        }
    }

    protected void assertErrorResponse(MvcTestResultAssert result, ApplicationError error) {
        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo(error.getCode());
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo(error.getMessage());
    }
}
