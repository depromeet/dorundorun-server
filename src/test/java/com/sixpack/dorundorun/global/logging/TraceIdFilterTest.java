package com.sixpack.dorundorun.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TraceIdFilterTest {

	private final TraceIdFilter filter = new TraceIdFilter();

	@Test
	void createsTraceIdWhenHeaderIsMissing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> traceIdDuringRequest = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) -> traceIdDuringRequest.set(MDC.get(TraceIdFilter.TRACE_ID)));

		assertThat(traceIdDuringRequest.get()).isNotBlank();
		assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo(traceIdDuringRequest.get());
		assertThat(MDC.get(TraceIdFilter.TRACE_ID)).isNull();
	}

	@Test
	void reusesIncomingTraceId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "upstream-trace-id");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (req, res) ->
			assertThat(MDC.get(TraceIdFilter.TRACE_ID)).isEqualTo("upstream-trace-id"));

		assertThat(response.getHeader(TraceIdFilter.TRACE_ID_HEADER)).isEqualTo("upstream-trace-id");
		assertThat(MDC.get(TraceIdFilter.TRACE_ID)).isNull();
	}
}
