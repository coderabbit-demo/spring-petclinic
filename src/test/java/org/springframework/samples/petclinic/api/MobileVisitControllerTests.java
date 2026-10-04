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
package org.springframework.samples.petclinic.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.samples.petclinic.owner.Visit.VisitStatus;

/**
 * Test class for {@link MobileVisitController}
 *
 * @author Claude
 */
@WebMvcTest(MobileVisitController.class)
@DisabledInNativeImage
@DisabledInAotMode
class MobileVisitControllerTests {

	private static final Integer TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MobileVisitService mobileVisitService;

	@Test
	void testGetVisits_Success() throws Exception {
		// Arrange
		VisitResponse visit1 = new VisitResponse(1, LocalDate.now().plusDays(1), "Check-up", VisitStatus.BOOKED);
		VisitResponse visit2 = new VisitResponse(2, LocalDate.now().plusDays(2), "Vaccination", VisitStatus.COMPLETED);

		given(mobileVisitService.getVisitsByPetId(TEST_PET_ID)).willReturn(List.of(visit1, visit2));

		// Act & Assert
		mockMvc.perform(get("/api/pets/{petId}/visits", TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].id", is(1)))
			.andExpect(jsonPath("$[0].description", is("Check-up")))
			.andExpect(jsonPath("$[0].status", is("BOOKED")))
			.andExpect(jsonPath("$[1].id", is(2)))
			.andExpect(jsonPath("$[1].description", is("Vaccination")))
			.andExpect(jsonPath("$[1].status", is("COMPLETED")));

		verify(mobileVisitService).getVisitsByPetId(TEST_PET_ID);
	}

	@Test
	void testGetVisits_Empty() throws Exception {
		// Arrange
		given(mobileVisitService.getVisitsByPetId(TEST_PET_ID)).willReturn(List.of());

		// Act & Assert
		mockMvc.perform(get("/api/pets/{petId}/visits", TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));

		verify(mobileVisitService).getVisitsByPetId(TEST_PET_ID);
	}

	@Test
	void testGetVisits_PetNotFound() throws Exception {
		// Arrange
		given(mobileVisitService.getVisitsByPetId(TEST_PET_ID))
			.willThrow(new IllegalArgumentException("Pet not found with id: " + TEST_PET_ID));

		// Act & Assert
		mockMvc.perform(get("/api/pets/{petId}/visits", TEST_PET_ID))
			.andExpect(status().isNotFound());

		verify(mobileVisitService).getVisitsByPetId(TEST_PET_ID);
	}

	@Test
	void testBookVisit_Success() throws Exception {
		// Arrange
		LocalDate visitDate = LocalDate.now().plusDays(3);
		VisitRequest request = new VisitRequest(visitDate, "Annual check-up");
		VisitResponse response = new VisitResponse(3, visitDate, "Annual check-up", VisitStatus.BOOKED);

		given(mobileVisitService.bookVisit(eq(TEST_PET_ID), any(VisitRequest.class))).willReturn(response);

		// Act & Assert
		mockMvc.perform(post("/api/pets/{petId}/visits", TEST_PET_ID)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id", is(3)))
			.andExpect(jsonPath("$.description", is("Annual check-up")))
			.andExpect(jsonPath("$.status", is("BOOKED")));

		verify(mobileVisitService).bookVisit(eq(TEST_PET_ID), any(VisitRequest.class));
	}

	@Test
	void testBookVisit_InvalidDate() throws Exception {
		// Arrange
		VisitRequest request = new VisitRequest(LocalDate.now(), "Check-up");

		given(mobileVisitService.bookVisit(eq(TEST_PET_ID), any(VisitRequest.class)))
			.willThrow(new IllegalArgumentException("Visit date must be in the future"));

		// Act & Assert
		mockMvc.perform(post("/api/pets/{petId}/visits", TEST_PET_ID)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error", is("Visit date must be in the future")));

		verify(mobileVisitService).bookVisit(eq(TEST_PET_ID), any(VisitRequest.class));
	}

	@Test
	void testBookVisit_PetNotFound() throws Exception {
		// Arrange
		VisitRequest request = new VisitRequest(LocalDate.now().plusDays(1), "Check-up");

		given(mobileVisitService.bookVisit(eq(TEST_PET_ID), any(VisitRequest.class)))
			.willThrow(new IllegalArgumentException("Pet not found with id: " + TEST_PET_ID));

		// Act & Assert
		mockMvc.perform(post("/api/pets/{petId}/visits", TEST_PET_ID)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error", is("Pet not found with id: " + TEST_PET_ID)));

		verify(mobileVisitService).bookVisit(eq(TEST_PET_ID), any(VisitRequest.class));
	}

	@Test
	void testBookVisit_MissingDescription() throws Exception {
		// Arrange
		String invalidRequest = "{\"visitDate\": \"" + LocalDate.now().plusDays(1) + "\"}";

		// Act & Assert
		mockMvc.perform(post("/api/pets/{petId}/visits", TEST_PET_ID)
				.contentType("application/json")
				.content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testBookVisit_MissingVisitDate() throws Exception {
		// Arrange
		String invalidRequest = "{\"description\": \"Check-up\"}";

		// Act & Assert
		mockMvc.perform(post("/api/pets/{petId}/visits", TEST_PET_ID)
				.contentType("application/json")
				.content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

}
