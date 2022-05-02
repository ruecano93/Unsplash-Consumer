package com.gamero.unsplashconsumer.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gamero.unsplashconsumer.client.UnsplashClientService;
import com.gamero.unsplashconsumer.dto.CollectionItemDto;
import com.gamero.unsplashconsumer.exception.ClientAuthException;
import com.gamero.unsplashconsumer.mapper.UnsplashMapper;
import com.gamero.unsplashconsumer.service.CollectionService;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = CollectionController.class)
@Import({ CollectionService.class, UnsplashMapper.class })
public class CollectionControllerTest {

	@MockBean
	private UnsplashClientService unsplashClient;

	@Autowired
	private WebTestClient webClient;

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	void getAllCollectionsTest_WhenNotFilterAndItsOK() throws Exception {
		// prepare mocks
		List<JsonNode> clientResponse = loadClientResponseMock("UnsplashCollectionResponseOk");
		Mockito.when(unsplashClient.getCollectionsClient()).thenReturn(Mono.just(clientResponse));
		List<CollectionItemDto> response = new ArrayList<>();
		// made request
		webClient.get().uri("/collection/all").exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(CollectionItemDto.class)
				.consumeWith(result -> response.addAll(result.getResponseBody()));
		// check response
		assertEquals(2, response.size());
		assertEquals("C-mgameroq01", response.get(1).getCoverPhotoId());
		Mockito.verify(unsplashClient, times(1)).getCollectionsClient();
	}

	@Test
	void getAllCollectionsTest_WhenFilterSucessfullyAndItsOK() throws Exception {
		// prepare mocks
		List<JsonNode> clientResponse = loadClientResponseMock("UnsplashCollectionResponseOk");
		Mockito.when(unsplashClient.getCollectionsClient()).thenReturn(Mono.just(clientResponse));
		List<CollectionItemDto> response = new ArrayList<>();
		// made request
		webClient.get().uri("/collection/all?filter=gamero").exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(CollectionItemDto.class)
				.consumeWith(result -> response.addAll(result.getResponseBody()));
		// check response
		assertEquals(1, response.size());
		assertEquals("C-mgameroq01", response.get(0).getCoverPhotoId());
		Mockito.verify(unsplashClient, times(1)).getCollectionsClient();
	}

	@Test
	void getAllCollectionsTest_WhenFilterSucessfullyButEmptyResponse() throws Exception {
		// prepare mocks
		List<JsonNode> clientResponse = loadClientResponseMock("UnsplashCollectionResponseOk");
		Mockito.when(unsplashClient.getCollectionsClient()).thenReturn(Mono.just(clientResponse));
		List<CollectionItemDto> response = new ArrayList<>();
		// made request
		webClient.get().uri("/collection/all?filter=fjasdhjsdfhj").exchange().expectStatus().is2xxSuccessful()
				.expectBodyList(CollectionItemDto.class)
				.consumeWith(result -> response.addAll(result.getResponseBody()));
		// check response
		assertTrue(response.isEmpty());
		Mockito.verify(unsplashClient, times(1)).getCollectionsClient();
	}

	@Test
	void getAllCollectionsTest_WhenClientFailedReturnFailedStatus() throws Exception {
		// prepare mocks
		Mockito.when(unsplashClient.getCollectionsClient()).thenReturn(Mono.error(new ClientAuthException()));
		// made request
		webClient.get().uri("/collection/all?filter=fjasdhjsdfhj").exchange().expectStatus().is4xxClientError();
	}

	private List<JsonNode> loadClientResponseMock(String fileName) throws Exception {
		String path = "src/test/resources/" + fileName + ".json";
		List<JsonNode> response = new ArrayList<>();
		((ArrayNode) mapper.readTree(new File(path))).forEach(jsonItem -> response.add(jsonItem));
		return response;
	}
}
