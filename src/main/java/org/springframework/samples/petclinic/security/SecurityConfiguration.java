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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for PetClinic application.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails admin = User.builder()
			.username("admin")
			.password(passwordEncoder().encode("admin"))
			.roles("ADMIN")
			.build();

		UserDetails vet = User.builder().username("vet").password(passwordEncoder().encode("vet")).roles("VET").build();

		UserDetails frontDesk = User.builder()
			.username("frontdesk")
			.password(passwordEncoder().encode("frontdesk"))
			.roles("FRONT_DESK")
			.build();

		return new InMemoryUserDetailsManager(admin, vet, frontDesk);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.exceptionHandling(
					exceptionHandling -> exceptionHandling.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/api/auth/token", "/", "/login", "/favicon.ico", "/webjars/**", "/resources/**")
				.permitAll()
				.requestMatchers("/h2-console/**")
				.permitAll()
				.requestMatchers("/actuator/**")
				.permitAll()
				.requestMatchers("/vets.html", "/owners/find", "/owners")
				.permitAll()
				.requestMatchers("/owners/new", "/owners/*/edit", "/owners/*/pets/*/edit")
				.hasRole("ADMIN")
				.requestMatchers("/owners/*/pets/*/visits/new")
				.hasAnyRole("FRONT_DESK", "VET")
				.requestMatchers("/vets", "/api/auth/**")
				.authenticated()
				.anyRequest()
				.permitAll());

		// Allow H2 console
		http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

		// Add JWT filter
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		// Form login for web UI
		http.formLogin(formLogin -> formLogin.loginPage("/login")
			.permitAll()
			.defaultSuccessUrl("/")
			.failureUrl("/login?error"));

		http.logout(logout -> logout.logoutUrl("/logout").permitAll().logoutSuccessUrl("/"));

		return http.build();
	}

}
