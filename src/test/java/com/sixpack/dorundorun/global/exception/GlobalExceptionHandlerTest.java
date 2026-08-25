package com.sixpack.dorundorun.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void missingStaticResourceIsNotReportedAsServerError() {
		MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.GET.name(), "/missing");
		NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "missing");

		var response = handler.handleNoResourceFoundException(exception, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
	}
}
