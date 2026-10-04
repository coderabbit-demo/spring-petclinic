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

package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link OwnerSearchRestController}
 *
 * @author Spring PetClinic Team
 */
@WebMvcTest(OwnerSearchRestController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerSearchRestControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final String TEST_OWNER_FIRST_NAME = "George";

	private static final String TEST_OWNER_LAST_NAME = "Franklin";

	private static final String TEST_OWNER_TELEPHONE = "6085551023";

	private static final String TEST_OWNER_ADDRESS = "110 W. Liberty St.";

	private static final String TEST_OWNER_CITY = "Madison";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner createTestOwner() {
		Owner owner = new Owner();
		owner.setId(TEST_OWNER_ID);
		owner.setFirstName(TEST_OWNER_FIRST_NAME);
		owner.setLastName(TEST_OWNER_LAST_NAME);
		owner.setAddress(TEST_OWNER_ADDRESS);
		owner.setCity(TEST_OWNER_CITY);
		owner.setTelephone(TEST_OWNER_TELEPHONE);
		Pet pet = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		pet.setType(dog);
		pet.setName("Max");
		pet.setBirthDate(LocalDate.now());
		owner.addPet(pet);
		pet.setId(1);
		return owner;
	}

	@Test
	void testSearchByLastName() throws Exception {
		Owner owner = createTestOwner();
		given(owners.searchByLastNameOrTelephone(eq("Franklin"))).willReturn(List.of(owner));

		mockMvc.perform(get("/api/owners/search").param("q", "Franklin"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].id").value(TEST_OWNER_ID))
			.andExpect(jsonPath("$[0].firstName").value(TEST_OWNER_FIRST_NAME))
			.andExpect(jsonPath("$[0].lastName").value(TEST_OWNER_LAST_NAME))
			.andExpect(jsonPath("$[0].telephone").value(TEST_OWNER_TELEPHONE));
	}

	@Test
	void testSearchByTelephone() throws Exception {
		Owner owner = createTestOwner();
		given(owners.searchByLastNameOrTelephone(eq("6085551023"))).willReturn(List.of(owner));

		mockMvc.perform(get("/api/owners/search").param("q", "6085551023"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].id").value(TEST_OWNER_ID))
			.andExpect(jsonPath("$[0].lastName").value(TEST_OWNER_LAST_NAME));
	}

	@Test
	void testSearchWithPartialTelephone() throws Exception {
		Owner owner = createTestOwner();
		given(owners.searchByLastNameOrTelephone(eq("608"))).willReturn(List.of(owner));

		mockMvc.perform(get("/api/owners/search").param("q", "608"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].telephone").value(TEST_OWNER_TELEPHONE));
	}

	@Test
	void testSearchWithEmptyQuery() throws Exception {
		given(owners.searchByLastNameOrTelephone(anyString())).willReturn(List.of());

		mockMvc.perform(get("/api/owners/search").param("q", ""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void testSearchWithWhitespaceOnlyQuery() throws Exception {
		given(owners.searchByLastNameOrTelephone(anyString())).willReturn(List.of());

		mockMvc.perform(get("/api/owners/search").param("q", "   "))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void testSearchWithNoResults() throws Exception {
		given(owners.searchByLastNameOrTelephone(eq("NonExistent"))).willReturn(List.of());

		mockMvc.perform(get("/api/owners/search").param("q", "NonExistent"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void testSearchWithMultipleResults() throws Exception {
		Owner owner1 = createTestOwner();
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Betty");
		owner2.setLastName("Franklin");
		owner2.setAddress("123 Main St.");
		owner2.setCity("Madison");
		owner2.setTelephone("6085551024");

		given(owners.searchByLastNameOrTelephone(eq("Franklin"))).willReturn(List.of(owner1, owner2));

		mockMvc.perform(get("/api/owners/search").param("q", "Franklin"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].lastName").value("Franklin"))
			.andExpect(jsonPath("$[1].lastName").value("Franklin"));
	}

	@Test
	void testSearchWithoutQueryParameter() throws Exception {
		mockMvc.perform(get("/api/owners/search")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
	}

}
