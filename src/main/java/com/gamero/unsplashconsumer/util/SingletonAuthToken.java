package com.gamero.unsplashconsumer.util;

public class SingletonAuthToken {
	
	private static SingletonAuthToken INSTANCE;
	
	private String authToken;
	
	private SingletonAuthToken() {}
	
	public static synchronized SingletonAuthToken getInstance(){
        if (INSTANCE == null) {
        	INSTANCE = new SingletonAuthToken();
        }	
        return INSTANCE;
    }
	
	public void setAuthToken(String authToken) {
		SingletonAuthToken.getInstance().authToken = authToken;
	}
	
	public String getAuthToken() {
		return authToken;
	}
}
