package com.markmuwonge.poc_remote_access_tool_server.model;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TCPServerConnectionPong {
	
	private long epochSecond;
	private int value;
	
	public TCPServerConnectionPong(long epochSecond, int value) {
		this.epochSecond = epochSecond;
		this.value = value;
	}

	public long getEpochSecond() {
		return epochSecond;
	}

	public void setEpochSecond(long epochSecond) {
		this.epochSecond = epochSecond;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
