package com.gamero.unsplashconsumer.client;

import java.util.List;
import java.util.function.Consumer;

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
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamero.unsplashconsumer.exception.ClientAuthException;
import com.gamero.unsplashconsumer.util.SingletonAuthToken;

import reactor.core.publisher.Mono;

@Component
public class UnsplashClientService {

	@Autowired
	private WebClient webClient;
	
	@Value("${clients.unsplash.collections-all}")
	private String collectionPath; // Avoiding hardcode urls

	private final Logger logger = LoggerFactory.getLogger(UnsplashClientService.class);

	public Mono<List<JsonNode>> getCollectionsClient() {
		// We suppose that if we do not have an access token we do the authentication before the request
		String tokenCandidate = SingletonAuthToken.getInstance().getAuthToken();
		String validToken = StringUtils.hasText(tokenCandidate) ? tokenCandidate : this.authenticate();
		if (!StringUtils.hasText(validToken)) {
			return Mono.error(new ClientAuthException("AuthToken is not available"));
		}
		Consumer<HttpHeaders> recommendationHeaders = headers -> {
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBearerAuth(validToken);
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
		// here should include the authenticacion
		SingletonAuthToken.getInstance().setAuthToken("Iqfqnakt1u16ifLTxAV_LgB0NGmIG2KfMWhMBmzHt6w");
		return SingletonAuthToken.getInstance().getAuthToken();
	}

}