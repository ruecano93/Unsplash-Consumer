package com.gamero.unsplashconsumer.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gamero.unsplashconsumer.dto.CollectionItemDto;
import com.gamero.unsplashconsumer.exception.ClientAuthException;
import com.gamero.unsplashconsumer.exception.UnprocessableRequestException;
import com.gamero.unsplashconsumer.service.CollectionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/collection")
public class CollectionController {
	
	@Autowired 
	private CollectionService service;
	
	@GetMapping("/all")
	public Mono<ResponseEntity<List<CollectionItemDto>>> getAllCollections(@RequestParam(value = "filter", required = false) String filter) {
	    return service.getFilteredCollections(decodeParam(filter))
	    		.map(items -> new ResponseEntity<>(items, HttpStatus.OK))
	    		.onErrorReturn(UnprocessableRequestException.class, new ResponseEntity<>(HttpStatus.BAD_REQUEST))
	    		.onErrorReturn(ClientAuthException.class, new ResponseEntity<>(HttpStatus.UNAUTHORIZED))
	    		.onErrorReturn(new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE));
	}
	
	private String decodeParam(String param) {
		return Optional.ofNullable(param)
				.map(p -> {
					try {
						return URLDecoder.decode(p, StandardCharsets.UTF_8.name());
					} catch (UnsupportedEncodingException e) {
						throw new UnprocessableRequestException();
					}
				})
				.orElse(null);
	}

}
