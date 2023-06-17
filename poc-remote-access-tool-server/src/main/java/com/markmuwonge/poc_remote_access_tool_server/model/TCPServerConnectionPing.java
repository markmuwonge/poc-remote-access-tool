package com.markmuwonge.poc_remote_access_tool_server.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Component
@Scope("prototype")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TCPServerConnectionPing {
	
	private final String action = "ping"; 
	
	@JsonIgnore private long epochSecond;
	@JsonIgnore private boolean locked;
	private int value;
	
	public TCPServerConnectionPing(long epochSecond, int value) {
		this.epochSecond = epochSecond;
		this.value = value;
		this.locked = false;
	}

	public long getEpochSecond() {
		return epochSecond;
	}

	public void setEpochSecond(long epochSecond) {
		this.epochSecond = epochSecond;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getAction() {
		return action;
	}
}
