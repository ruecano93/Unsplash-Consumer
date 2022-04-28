package com.gamero.unsplashconsumer.service;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.util.StringUtils;

import com.gamero.unsplashconsumer.dto.CollectionItemDto;

public enum FilterQueryParamService {
    ID("id", CollectionItemDto::getId),
    TITLE("title", CollectionItemDto::getTitle),
    DESCRIPTION("description", CollectionItemDto::getDescription),
    ORIGIN("cover_photo_id", CollectionItemDto::getCoverPhotoId);

    private String name;
    private Function<CollectionItemDto, Object> valueExtractor;

    private FilterQueryParamService(String name, 
            Function<CollectionItemDto, Object> valueExtractor) {
        this.name = name;
        this.valueExtractor = valueExtractor;
    }
    
    public static boolean applyLikeValuesForMap(CollectionItemDto dto, Map<String, String> queryParams) {
    	Predicate<FilterQueryParamService> filterNotPresent =  filterValue -> !queryParams.containsKey(filterValue.name);
    	Predicate<FilterQueryParamService> containsValue = filterValue -> likeText(queryParams.get(filterValue.name),filterValue.valueExtractor.apply(dto).toString());
    	return Arrays.asList(values())
    			.parallelStream() // we can do parallel stream because order is not mandatory
    			.allMatch(filterNotPresent.or(containsValue)); // if filter key is not present this value dont need anyFiltering
    	// allMatch use circuit breaker and not continue when any value not match
    }
    
    public static boolean applyLikeValues(CollectionItemDto dto, String filter) {
    	try {
    	return Arrays.asList(values())
    			.parallelStream()// we can do parallel stream because order is not mandatory
    			.filter(filterValue -> filterValue.valueExtractor.apply(dto) != null)
    			.anyMatch(filterValue -> !StringUtils.hasText(filter) || likeText(filter, filterValue.valueExtractor.apply(dto).toString())); 
    	// anyMatch use circuit breaker and not continue when any value match
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		return false;
    	}
    }
    
    private static boolean likeText(String filter, String coreText) {
    	if(coreText == null) {
    		return false;
    	}
    	filter = normalize(filter);
    	coreText = normalize(coreText);
    	return coreText.contains(filter); // ignore accents and make case insensitive
    }
    
    private static String normalize(String string) {
        return Normalizer.normalize(string, Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }

}
