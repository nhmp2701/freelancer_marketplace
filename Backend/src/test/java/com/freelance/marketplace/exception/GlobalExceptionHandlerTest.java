package com.freelance.marketplace.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
  private final MockHttpServletRequest request =
      new MockHttpServletRequest("POST", "/api/v1/auth/login");

  @Test
  void mapsAuthenticationFailureToGenericUnauthorizedResponse() {
    var response = handler.handleAuthentication(new BadCredentialsException("sensitive"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
  }

  @Test
  void doesNotExposeUnexpectedExceptionMessage() {
    var response =
        handler.handleGlobalException(new RuntimeException("database password"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    assertThat(response.getBody().getPath()).isEqualTo("/api/v1/auth/login");
  }

  @Test
  void mapsAuthorizationAndStateErrors() {
    var forbidden = handler.handleAccessDenied(new AccessDeniedException("Not the owner"), request);
    var conflict =
        handler.handleIllegalState(new IllegalStateException("Already started"), request);

    assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }
}
