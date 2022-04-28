package com.gamero.unsplashconsumer.exception;

public class ClientAuthException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ClientAuthException(String message) {
		super(message);
	}
	
	public ClientAuthException() {}

}
