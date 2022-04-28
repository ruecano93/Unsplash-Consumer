package com.gamero.unsplashconsumer.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamero.unsplashconsumer.dto.CollectionItemDto;

@Component
public class UnsplashMapper {
	
	public CollectionItemDto jsonNodeToCollectionItemDto(JsonNode jsonNode) {
		return CollectionItemDto.builder()
				.id(jsonNode.get("id").asInt())
				.title(jsonNode.get("title").asText(null)) // Jackson should be configurable to avoid null fields
				.description(jsonNode.get("description").asText(null))
				.coverPhotoId(jsonNode.get("cover_photo").get("id").asText(null))
				.build();
	}
	
	public List<CollectionItemDto> jsonNodeToCollectionItemDtoList(List<JsonNode> jsonNodeList) {
		return jsonNodeList.stream()
				.map(this::jsonNodeToCollectionItemDto)
				.filter(item -> item.getId().intValue() != 0) // we assume that the id field is mandatory
				.collect(Collectors.toList());
	}
}
