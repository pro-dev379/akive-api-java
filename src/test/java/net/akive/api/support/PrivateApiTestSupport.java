package net.akive.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.common.exception.ApplicationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public abstract class PrivateApiTestSupport {

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MvcTestResultAssert get(String uri, Long userId, Object... uriVariables) {
        return mvcTester.get()
                .uri(uri, uriVariables)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert postJson(String uri, Long userId, String json, Object... uriVariables) {
        return mvcTester.post()
                .uri(uri, uriVariables)
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert putJson(String uri, Long userId, String json, Object... uriVariables) {
        return mvcTester.put()
                .uri(uri, uriVariables)
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert patchJson(String uri, Long userId, String json, Object... uriVariables) {
        return mvcTester.patch()
                .uri(uri, uriVariables)
                .contentType(APPLICATION_JSON)
                .content(json)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert delete(String uri, Long userId, Object... uriVariables) {
        return mvcTester.delete()
                .uri(uri, uriVariables)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))))
                .assertThat();
    }

    protected MvcTestResultAssert delete(
            String uri,
            Long userId,
            String headerName,
            String headerValue,
            Object... uriVariables
    ) {
        var requestBuilder = mvcTester.delete()
                .uri(uri, uriVariables)
                .with(jwt().jwt(jwt -> jwt.subject(String.valueOf(userId))));

        if (headerValue != null) {
            requestBuilder.header(headerName, headerValue);
        }

        return requestBuilder.assertThat();
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
