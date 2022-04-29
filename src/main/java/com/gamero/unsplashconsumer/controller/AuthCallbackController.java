package com.gamero.unsplashconsumer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamero.unsplashconsumer.client.UnsplashClientService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/prueba-apz")
public class AuthCallbackController {

	@Autowired 
	private UnsplashClientService client;
	
	private final Logger logger = LoggerFactory.getLogger(AuthCallbackController.class);
	
	@GetMapping("/auth")
	public Mono<String> getAuth() {
		return client.getAuthCode();
	}
	
	@GetMapping("/token")
	public Mono<ResponseEntity<String>> getCodeAndUpdateToken(@RequestParam(value = "code", required = false) String code) {
		logger.info("requested update jwt with code "+ code);
		return client.updateToken(code)
				.map(items -> new ResponseEntity<>(HttpStatus.OK.getReasonPhrase(), HttpStatus.OK))
	    		.onErrorReturn(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE));
	}
}
