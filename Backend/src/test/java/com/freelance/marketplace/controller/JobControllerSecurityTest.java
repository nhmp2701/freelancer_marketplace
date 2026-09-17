package com.freelance.marketplace.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.freelance.marketplace.config.SecurityConfig;
import com.freelance.marketplace.dto.response.JobResponse;
import com.freelance.marketplace.dto.response.PageResponse;
import com.freelance.marketplace.security.JwtAuthenticationFilter;
import com.freelance.marketplace.security.JwtUtil;
import com.freelance.marketplace.service.job.JobCommandService;
import com.freelance.marketplace.service.job.JobQueryService;
import com.freelance.marketplace.service.proposal.ProposalService;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {JobController.class, ProposalController.class},
    properties = "app.cors.allowed-origins=http://localhost:5173")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class JobControllerSecurityTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JobQueryService jobQueryService;

  @MockitoBean private JobCommandService jobCommandService;

  @MockitoBean private ProposalService proposalService;

  @MockitoBean private JwtUtil jwtUtil;

  @MockitoBean private UserDetailsService userDetailsService;

  @Test
  void jobSearchIsPublic() throws Exception {
    when(jobQueryService.searchJobs(
            any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(PageResponse.<JobResponse>builder().content(List.of()).build());

    mockMvc.perform(get("/api/v1/jobs")).andExpect(status().isOk());
  }

  @Test
  void invalidJobPageSizeReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/api/v1/jobs").queryParam("size", "101"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void negativeBudgetReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/api/v1/jobs").queryParam("minBudget", "-1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void invalidJobStatusReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/api/v1/jobs").queryParam("status", "NOT_A_STATUS"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void personalJobListRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/jobs/me/posted")).andExpect(status().isUnauthorized());
  }

  @Test
  void jobProposalsRequireAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/jobs/1/proposals")).andExpect(status().isUnauthorized());
  }

  @Test
  void malformedTokenOnProtectedRouteReturnsUnauthorized() throws Exception {
    when(jwtUtil.extractEmail("bad-token")).thenThrow(new JwtException("invalid"));

    mockMvc
        .perform(get("/api/v1/jobs/me/posted").header("Authorization", "Bearer bad-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void tokenForDeletedUserReturnsUnauthorized() throws Exception {
    when(jwtUtil.extractEmail("orphan-token")).thenReturn("deleted@example.com");
    when(userDetailsService.loadUserByUsername("deleted@example.com"))
        .thenThrow(new UsernameNotFoundException("deleted"));

    mockMvc
        .perform(get("/api/v1/jobs/me/posted").header("Authorization", "Bearer orphan-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void configuredFrontendOriginIsAllowed() throws Exception {
    mockMvc
        .perform(
            options("/api/v1/jobs")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
  }
}
