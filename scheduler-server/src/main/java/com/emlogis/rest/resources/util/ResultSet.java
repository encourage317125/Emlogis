package com.emlogis.rest.resources.util;

import java.util.Collection;

public class ResultSet<T> {

	private Collection<T> result;
    private int total = -1;	        // total records matching query (-1 if no value)

	public ResultSet() {
		super();
	}
	
	public ResultSet(Collection<T> result) {
		super();
		this.result = result;
	}

	public ResultSet(Collection<T> result, int total) {
		super();
		this.result = result;
		this.total = total;
	}

	public Collection<T> getResult() {
		return result;
	}

	public void setResult(Collection<T> result) {
		this.result = result;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
