package com.markmuwonge.poc_remote_access_tool_server.model;

public class ProjectData {
	
	private Integer tcpServerPort;
	private Integer webServerPort;
	

	public Integer getWebServerPort() {
		return webServerPort;
	}

	public void setWebServerPort(Integer webServerPort) {
		this.webServerPort = webServerPort;
	}

	public Integer getTCPServerPort() {
		return tcpServerPort;
	}

	public void setTCPServerPort(Integer tcpServerPort) {
		this.tcpServerPort = tcpServerPort;
	}
		
	
}
