package com.emlogis.engine.domain.communication;

public class ScheduleResult {
	
	private ScheduleCompletion completion = ScheduleCompletion.OK;
	
	private  String completionInfo;
	
	private ScheduleResultDto result;

	public ScheduleResult() {
		super();
	}

	public ScheduleCompletion getCompletion() {
		return completion;
	}

	public void setCompletion(ScheduleCompletion completion) {
		this.completion = completion;
	}

	public String getCompletionInfo() {
		return completionInfo;
	}

	public void setCompletionInfo(String completionInfo) {
		this.completionInfo = completionInfo;
	}

	public ScheduleResultDto getResult() {
		return result;
	}

	public void setResult(ScheduleResultDto result) {
		this.result = result;
	}

}
