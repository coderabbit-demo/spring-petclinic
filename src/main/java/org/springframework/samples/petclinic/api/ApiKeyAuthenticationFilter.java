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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for API key authentication. Checks the X-Api-Key header for mobile app requests.
 *
 * @author Claude
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

	private static final String API_KEY_HEADER = "X-Api-Key";

	private static final String API_PATH_PREFIX = "/api/";

	@Value("${mobile.api.key:changeme}")
	private String validApiKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();

		// Only apply authentication to /api/** endpoints
		if (path.startsWith(API_PATH_PREFIX)) {
			String apiKey = request.getHeader(API_KEY_HEADER);

			if (!isValidApiKey(apiKey)) {
				logger.warn("Unauthorized API request from IP: {}, requested path: {}",
					request.getRemoteAddr(), path);
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.getWriter().write("{\"error\": \"Unauthorized: Invalid or missing API key\"}");
				response.setContentType("application/json");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Validates the provided API key.
	 * @param apiKey the API key to validate
	 * @return true if the API key is valid, false otherwise
	 */
	private boolean isValidApiKey(String apiKey) {
		return apiKey != null && apiKey.equals(validApiKey);
	}

}
