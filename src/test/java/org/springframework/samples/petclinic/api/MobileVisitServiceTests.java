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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.owner.Visit.VisitStatus;
import org.springframework.samples.petclinic.owner.VisitRepository;

/**
 * Test class for {@link MobileVisitService}
 *
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
class MobileVisitServiceTests {

	private static final Integer TEST_PET_ID = 1;

	private static final Integer TEST_OWNER_ID = 1;

	@Mock
	private VisitRepository visitRepository;

	@Mock
	private OwnerRepository ownerRepository;

	private MobileVisitService service;

	private Owner testOwner;

	private Pet testPet;

	@BeforeEach
	void init() {
		service = new MobileVisitService(visitRepository, ownerRepository);

		// Setup test owner and pet
		testOwner = new Owner();
		testOwner.setId(TEST_OWNER_ID);
		testOwner.setFirstName("John");
		testOwner.setLastName("Doe");

		testPet = new Pet();
		testPet.setId(TEST_PET_ID);
		testPet.setName("Rex");
		testOwner.addPet(testPet);
	}

	@Test
	void testGetVisitsByPetId_Success() {
		// Arrange
		List<Visit> visits = new ArrayList<>();
		Visit visit1 = new Visit();
		visit1.setId(1);
		visit1.setDate(LocalDate.now().plusDays(1));
		visit1.setDescription("Check-up");
		visit1.setStatus(VisitStatus.BOOKED);
		visits.add(visit1);

		given(ownerRepository.findAll()).willReturn(List.of(testOwner));
		given(visitRepository.findByPetId(TEST_PET_ID)).willReturn(visits);

		// Act
		List<VisitResponse> result = service.getVisitsByPetId(TEST_PET_ID);

		// Assert
		assertThat(result).hasSize(1);
		assertThat(result.get(0).visitDate()).isEqualTo(LocalDate.now().plusDays(1));
		assertThat(result.get(0).description()).isEqualTo("Check-up");
		assertThat(result.get(0).status()).isEqualTo(VisitStatus.BOOKED);

		verify(visitRepository).findByPetId(TEST_PET_ID);
	}

	@Test
	void testGetVisitsByPetId_PetNotFound() {
		// Arrange
		given(ownerRepository.findAll()).willReturn(List.of());

		// Act & Assert
		assertThatThrownBy(() -> service.getVisitsByPetId(TEST_PET_ID))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Pet not found");
	}

	@Test
	void testGetVisitsByPetId_PetIdNull() {
		// Act & Assert
		assertThatThrownBy(() -> service.getVisitsByPetId(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Pet ID must not be null");
	}

	@Test
	void testBookVisit_Success() {
		// Arrange
		LocalDate visitDate = LocalDate.now().plusDays(2);
		VisitRequest request = new VisitRequest(visitDate, "Annual check-up");

		given(ownerRepository.findAll()).willReturn(List.of(testOwner));
		given(ownerRepository.save(any())).willReturn(testOwner);

		// Act
		VisitResponse response = service.bookVisit(TEST_PET_ID, request);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.visitDate()).isEqualTo(visitDate);
		assertThat(response.description()).isEqualTo("Annual check-up");
		assertThat(response.status()).isEqualTo(VisitStatus.BOOKED);

		verify(ownerRepository).save(any());
	}

	@Test
	void testBookVisit_PetNotFound() {
		// Arrange
		VisitRequest request = new VisitRequest(LocalDate.now().plusDays(1), "Check-up");
		given(ownerRepository.findAll()).willReturn(List.of());

		// Act & Assert
		assertThatThrownBy(() -> service.bookVisit(TEST_PET_ID, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Pet not found");
	}

	@Test
	void testBookVisit_InvalidDate() {
		// Arrange
		VisitRequest request = new VisitRequest(LocalDate.now(), "Check-up");
		given(ownerRepository.findAll()).willReturn(List.of(testOwner));

		// Act & Assert
		assertThatThrownBy(() -> service.bookVisit(TEST_PET_ID, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Visit date must be in the future");
	}

	@Test
	void testBookVisit_InvalidDateInPast() {
		// Arrange
		VisitRequest request = new VisitRequest(LocalDate.now().minusDays(1), "Check-up");
		given(ownerRepository.findAll()).willReturn(List.of(testOwner));

		// Act & Assert
		assertThatThrownBy(() -> service.bookVisit(TEST_PET_ID, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Visit date must be in the future");
	}

	@Test
	void testBookVisit_NullPetId() {
		// Arrange
		VisitRequest request = new VisitRequest(LocalDate.now().plusDays(1), "Check-up");

		// Act & Assert
		assertThatThrownBy(() -> service.bookVisit(null, request))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Pet ID must not be null");
	}

	@Test
	void testBookVisit_NullRequest() {
		// Act & Assert
		assertThatThrownBy(() -> service.bookVisit(TEST_PET_ID, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Visit request must not be null");
	}

}
