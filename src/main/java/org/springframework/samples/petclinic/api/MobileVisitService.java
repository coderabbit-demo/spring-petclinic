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

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.owner.Visit.VisitStatus;
import org.springframework.samples.petclinic.owner.VisitRepository;

/**
 * Service for managing mobile app visit bookings and queries.
 *
 * @author Claude
 */
@Service
public class MobileVisitService {

	private static final Logger logger = LoggerFactory.getLogger(MobileVisitService.class);

	private final VisitRepository visitRepository;

	private final OwnerRepository ownerRepository;

	public MobileVisitService(VisitRepository visitRepository, OwnerRepository ownerRepository) {
		this.visitRepository = visitRepository;
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Retrieves all visits for a given pet.
	 * @param petId the pet id
	 * @return a list of {@link VisitResponse} objects
	 * @throws IllegalArgumentException if pet is not found
	 */
	@Transactional(readOnly = true)
	public List<VisitResponse> getVisitsByPetId(Integer petId) {
		Assert.notNull(petId, "Pet ID must not be null");

		// Verify that the pet exists
		Owner owner = ownerRepository.findAll().stream()
			.filter(o -> o.getPet(petId) != null)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Pet not found with id: " + petId));

		return visitRepository.findByPetId(petId).stream()
			.map(this::convertToResponse)
			.toList();
	}

	/**
	 * Books a new visit for a pet.
	 * @param petId the pet id
	 * @param visitRequest the visit request containing date and description
	 * @return the {@link VisitResponse} for the booked visit
	 * @throws IllegalArgumentException if pet is not found or if visit date is invalid
	 */
	@Transactional
	public VisitResponse bookVisit(Integer petId, VisitRequest visitRequest) {
		Assert.notNull(petId, "Pet ID must not be null");
		Assert.notNull(visitRequest, "Visit request must not be null");

		// Verify that the pet exists
		Owner owner = ownerRepository.findAll().stream()
			.filter(o -> o.getPet(petId) != null)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Pet not found with id: " + petId));

		Pet pet = owner.getPet(petId);

		// Validate visit date
		if (visitRequest.visitDate() != null && !visitRequest.visitDate().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Visit date must be in the future");
		}

		// Create and save the visit
		Visit visit = new Visit();
		visit.setDate(visitRequest.visitDate());
		visit.setDescription(visitRequest.description());
		visit.setStatus(VisitStatus.BOOKED);

		pet.addVisit(visit);
		ownerRepository.save(owner);

		// Log the booking
		logger.info("Visit booked for pet ID: {}, visit date: {}, owner: {}",
			petId, visitRequest.visitDate(), owner.getFirstName() + " " + owner.getLastName());

		return convertToResponse(visit);
	}

	/**
	 * Converts a {@link Visit} entity to a {@link VisitResponse} DTO.
	 * @param visit the visit entity
	 * @return the visit response
	 */
	private VisitResponse convertToResponse(Visit visit) {
		return new VisitResponse(
			visit.getId(),
			visit.getDate(),
			visit.getDescription(),
			visit.getStatus()
		);
	}

}
