package com.gamero.unsplashconsumer.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gamero.unsplashconsumer.client.UnsplashClientService;
import com.gamero.unsplashconsumer.dto.CollectionItemDto;
import com.gamero.unsplashconsumer.exception.UnprocessableRequestException;
import com.gamero.unsplashconsumer.mapper.UnsplashMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CollectionService {

	@Autowired
	private UnsplashClientService restClient;

	@Autowired
	private UnsplashMapper unsplashMapper;

	public Mono<List<CollectionItemDto>> getFilteredCollections(String filter, String filterMap) {
		// with a flux iterable the process could made full parallel and reactive
		return restClient.getCollectionsClient()
				.flatMapMany(c -> Flux.fromIterable(c).map(unsplashMapper::jsonNodeToCollectionItemDto)
						.filter(item -> !Integer.valueOf(0).equals(item.getId())))
				.filter(item -> FilterQueryParamService.applyLikeValues(item, filter))
				.filter(item -> filter != null || FilterQueryParamService.applyLikeValuesForMap(item, this.convertStringToMap(filterMap)))
				.collectList(); // last filter only works when main filter is not present
	}

	// parse request filterParams to map (easy to process filters)
	public Map<String, String> convertStringToMap(String mapAsString) {
		try {
			if (mapAsString == null) {
				return new HashMap<>();
			}
			return Arrays.stream(mapAsString.split(";")).map(s -> s.split("::"))
					.collect(Collectors.toMap(s -> s[0], s -> s[1]));
		} catch (Exception e) {
			throw new UnprocessableRequestException();
		}
	}
}
