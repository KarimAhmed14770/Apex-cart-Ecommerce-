package com.Kee.V2C.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class JwtSecurityTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void protectedEndpoint_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/vendors/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withCorruptToken_returns403() throws Exception {
        mockMvc.perform(get("/api/vendors/profile")
                        .header("Authorization", "Bearer this.is.not.a.valid.jwt"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicEndpoint_withoutToken_returns200() throws Exception {
        mockMvc.perform(get("/api/categories/"))
                .andExpect(status().isOk());
    }
}
