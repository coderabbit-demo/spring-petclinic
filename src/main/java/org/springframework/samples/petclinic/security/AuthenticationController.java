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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

	private final AuthenticationManager authenticationManager;

	private final JwtTokenProvider jwtTokenProvider;

	public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@PostMapping("/token")
	public ResponseEntity<?> getToken(@RequestParam String username, @RequestParam String password) {
		try {
			Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			String token = jwtTokenProvider.generateToken(authentication);
			return ResponseEntity.ok().body(new TokenResponse(token));
		}
		catch (AuthenticationException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse("Invalid username or password"));
		}
	}

	/**
	 * DTO for JWT token response
	 */
	public static class TokenResponse {

		public final String token;

		public TokenResponse(String token) {
			this.token = token;
		}

		public String getToken() {
			return token;
		}

	}

	/**
	 * DTO for error response
	 */
	public static class ErrorResponse {

		public final String message;

		public ErrorResponse(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}

}
