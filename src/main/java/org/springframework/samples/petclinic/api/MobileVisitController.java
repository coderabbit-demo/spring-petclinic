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

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST controller for mobile app visit management.
 *
 * @author Claude
 */
@RestController
@RequestMapping("/api/pets")
public class MobileVisitController {

	private final MobileVisitService mobileVisitService;

	public MobileVisitController(MobileVisitService mobileVisitService) {
		this.mobileVisitService = mobileVisitService;
	}

	/**
	 * GET /api/pets/{petId}/visits - Retrieve all visits for a pet.
	 * @param petId the pet id
	 * @return a list of {@link VisitResponse} objects or 404 if pet not found
	 */
	@GetMapping("/{petId}/visits")
	public ResponseEntity<List<VisitResponse>> getVisits(@PathVariable Integer petId) {
		try {
			List<VisitResponse> visits = mobileVisitService.getVisitsByPetId(petId);
			return ResponseEntity.ok(visits);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * POST /api/pets/{petId}/visits - Book a new visit for a pet.
	 * @param petId the pet id
	 * @param visitRequest the visit booking request
	 * @return the created {@link VisitResponse} with 201 status or 404 if pet not found
	 */
	@PostMapping("/{petId}/visits")
	public ResponseEntity<?> bookVisit(@PathVariable Integer petId, @Valid @RequestBody VisitRequest visitRequest) {
		try {
			VisitResponse response = mobileVisitService.bookVisit(petId, visitRequest);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
		}
	}

}
