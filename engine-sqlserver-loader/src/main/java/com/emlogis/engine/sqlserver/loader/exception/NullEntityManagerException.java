package com.emlogis.engine.sqlserver.loader.exception;


public class NullEntityManagerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1044552423677197353L;

	@Override
	public String getMessage() {
		return "The EntityManager has not been set. Please set the EntityManager to be used for data retrieval.";

	}
	
}
