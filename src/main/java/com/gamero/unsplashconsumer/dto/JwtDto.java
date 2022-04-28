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
public class JwtDto {
	
	@JsonProperty("access_token")
	private String access_token;
	
	@JsonProperty("token_type")
	private String token_type;
	
	@JsonProperty("scope")
	private String scope;
	
	@JsonProperty("create_at")
	private Long create_at;

}
