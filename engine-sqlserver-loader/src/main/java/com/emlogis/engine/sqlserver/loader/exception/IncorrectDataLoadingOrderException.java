package com.emlogis.engine.sqlserver.loader.exception;

public class IncorrectDataLoadingOrderException extends Exception {
	private String expectedLoadedObjectName;
	private String objectBeingLoaded;
	
	public IncorrectDataLoadingOrderException(String expectedLoadedObjectName,
			String objectBeingLoaded) {
		super();
		this.expectedLoadedObjectName = expectedLoadedObjectName;
		this.objectBeingLoaded = objectBeingLoaded;
	}
	
	@Override
	public String getMessage() {
		return "Object " + expectedLoadedObjectName + " must be loaded before attempting to load " + objectBeingLoaded;
	}
	
	public String getExpectedLoadedObjectName() {
		return expectedLoadedObjectName;
	}
	
	public void setExpectedLoadedObjectName(String expectedLoadedObjectName) {
		this.expectedLoadedObjectName = expectedLoadedObjectName;
	}
	
	public String getObjectBeingLoaded() {
		return objectBeingLoaded;
	}
	
	public void setObjectBeingLoaded(String objectBeingLoaded) {
		this.objectBeingLoaded = objectBeingLoaded;
	}
	
	
	
	
}
