package com.gamero.unsplashconsumer.exception;

public class UnprocessableRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public UnprocessableRequestException(String message) {
		super(message);
	}
	
	public UnprocessableRequestException() {}

}
