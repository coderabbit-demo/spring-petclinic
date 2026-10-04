/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link AuthenticationController}
 */
@WebMvcTest(AuthenticationController.class)
@DisabledInNativeImage
@DisabledInAotMode
class AuthenticationControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getTokenWithValidAdminCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/token").param("username", "admin").param("password", "admin"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token", notNullValue()));
	}

	@Test
	void getTokenWithValidVetCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/token").param("username", "vet").param("password", "vet"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token", notNullValue()));
	}

	@Test
	void getTokenWithValidFrontDeskCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/token").param("username", "frontdesk").param("password", "frontdesk"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token", notNullValue()));
	}

	@Test
	void getTokenWithInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/auth/token").param("username", "invalid").param("password", "invalid"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message", notNullValue()));
	}

}
