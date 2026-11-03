package net.akive.api.core.config.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.akive.api.core.config.logging.RequestCorrelationFilter.REQUEST_ID_HEADER_NAME;
import static net.akive.api.core.config.logging.RequestCorrelationFilter.REQUEST_ID_MDC_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter requestCorrelationFilter = new RequestCorrelationFilter();

    @Test
    void 요청마다_새로운_요청_식별자를_생성해_로그와_응답에_동일한_값을_사용한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInLogContext = new AtomicReference<>();

        requestCorrelationFilter.doFilter(
                request,
                response,
                (servletRequest, servletResponse) -> requestIdInLogContext.set(MDC.get(REQUEST_ID_MDC_KEY))
        );

        String requestId = requestIdInLogContext.get();
        assertThat(UUID.fromString(requestId).toString()).isEqualTo(requestId);
        assertThat(response.getHeader(REQUEST_ID_HEADER_NAME)).isEqualTo(requestId);
        assertThat(MDC.get(REQUEST_ID_MDC_KEY)).isNull();
    }
}
