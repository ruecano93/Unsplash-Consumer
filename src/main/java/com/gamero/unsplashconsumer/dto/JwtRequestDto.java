package com.gamero.unsplashconsumer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequestDto {
	
	@JsonProperty("client_id")
	private String client_id;
	
	@JsonProperty("client_secret")
	private String client_secret;
	
	@JsonProperty("redirect_uri")
	private String redirect_uri;
	
	@JsonProperty("code")
	private String code;
	
	@JsonProperty("grant_type")
	private String grant_type;

}
