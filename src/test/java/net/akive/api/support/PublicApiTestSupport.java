package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.common.exception.ApplicationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc(addFilters = false)
public abstract class PublicApiTestSupport {

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

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

    protected MvcTestResultAssert postWithAuthorization(String url, String authorizationHeader) {
        return mvcTester.post()
                .uri(url)
                .header(AUTHORIZATION, authorizationHeader)
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
