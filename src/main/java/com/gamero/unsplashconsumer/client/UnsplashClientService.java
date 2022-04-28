package com.gamero.unsplashconsumer.client;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamero.unsplashconsumer.dto.JwtDto;
import com.gamero.unsplashconsumer.dto.JwtRequestDto;
import com.gamero.unsplashconsumer.exception.ClientAuthException;
import com.gamero.unsplashconsumer.util.SingletonAuthToken;

import reactor.core.publisher.Mono;

@Component
public class UnsplashClientService {

	@Autowired
	private WebClient webClient;
	
	@Value("${clients.unsplash.collections-all}")
	private String collectionPath; // Avoiding hardcode urls, keys...
	
	@Value("${clients.unsplash.oauth-login}")
	private String oauthPath;
	
	@Value("${clients.unsplash.oauth-token}")
	private String tokenPath;
	
	@Value("${clients.unsplash.client-id}")
	private String clientId; 
	
	@Value("${clients.unsplash.client-secret}")
	private String clientSecret; 
	
	@Value("${clients.unsplash.redirect-uri}")
	private String redirectUri; // redirect uri needs to be configurable for current environment

	private final Logger logger = LoggerFactory.getLogger(UnsplashClientService.class);

	public Mono<List<JsonNode>> getCollectionsClient() {
		// We suppose that if we do not have an access token we do the authentication before the request
		String validToken = SingletonAuthToken.getInstance().getAuthToken();
		logger.info("valid Token = "+ validToken); // Only for proofs
		if (!StringUtils.hasText(validToken)) {
			return Mono.error(new ClientAuthException("AuthToken is not available"));
		}
		Consumer<HttpHeaders> recommendationHeaders = headers -> {
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(this.authenticate());
		};
		return webClient.get().uri(collectionPath)
				.headers(recommendationHeaders).retrieve()
				.onStatus(HttpStatus.UNAUTHORIZED::equals, error -> Mono.error(new ClientAuthException("AuthToken is not valid")))
				.bodyToMono(new ParameterizedTypeReference<List<JsonNode>>() {})
				.doOnError(error -> this.logger.error(
						"Error obtaining collections : {}. Error message : {}", error,
						error.getMessage()));
	}
	
	// TODO pending use authenticacion
	public String authenticate() {
		SingletonAuthToken.getInstance().setAuthToken("fgHVUbfxabSkMMJz-cxEuJXwlvD9iwtqPTLN5LonvMU");
		return SingletonAuthToken.getInstance().getAuthToken();
	}
	
	public Mono<JwtDto> updateToken(String code) {
		return webClient.post().uri(tokenPath)
				.body(BodyInserters.fromValue(getTokenRequest(code)))
				.retrieve()
				.bodyToMono(JwtDto.class)
				.doOnSuccess(jwt -> SingletonAuthToken.getInstance().setAuthToken(jwt.getAccess_token()))
				.doOnError(error -> this.logger.error(
						"Error obtaining auth token : {}. Error message : {}", error,
						error.getMessage()));	
	}
	
	@PostConstruct  // start an authentication workflow after run application
	public Mono<String> getAuthCode() {
		this.authenticate();
		Consumer<Map<String,Object>> queryParams = querys -> {
			querys.put("client_id", clientId);
			querys.put("redirect_uri", redirectUri);
			querys.put("response_type", "code");
			querys.put("scope","public");
		};
		return webClient.get()
				.uri(oauthPath)
				.attributes(queryParams)
				.retrieve()
				.onStatus(HttpStatus.UNAUTHORIZED::equals, error -> Mono.error(new ClientAuthException("AuthFailure is not valid")))
				.bodyToMono(String.class)
				.log()
				.doOnError(error -> this.logger.error(
						"Error obtaining collections : {}. Error message : {}", error,
						error.getMessage()));
	}
	
	private JwtRequestDto getTokenRequest(String code) {
		return JwtRequestDto.builder()
		.client_id(clientId)
		.client_secret(clientSecret)
		.redirect_uri(redirectUri)
		.code(code)
		.grant_type("authorization_code").build();
	}

}