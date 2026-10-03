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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for owner search operations. Provides a JSON API for looking up pet
 * owners.
 *
 * @author Spring PetClinic Team
 */
@RestController
@RequestMapping("/api/owners")
class OwnerSearchRestController {

	private static final Logger logger = LoggerFactory.getLogger(OwnerSearchRestController.class);

	private final OwnerRepository ownerRepository;

	public OwnerSearchRestController(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Search for owners by last name or telephone.
	 * @param q the search query term to match against owner last name or telephone
	 * @return a List of matching {@link Owner}s as JSON
	 */
	@GetMapping("/search")
	public List<Owner> searchOwners(@RequestParam(name = "q", required = false, defaultValue = "") String q) {
		logger.debug("Searching for owners with query: {}", q);

		if (q == null || q.strip().isEmpty()) {
			logger.debug("Empty search query, returning empty list");
			return List.of();
		}

		String searchTerm = q.strip();
		logger.info("Owner search executed with term: {}", searchTerm);

		List<Owner> results = ownerRepository.searchByLastNameOrTelephone(searchTerm);
		logger.debug("Search returned {} results", results.size());

		return results;
	}

}
