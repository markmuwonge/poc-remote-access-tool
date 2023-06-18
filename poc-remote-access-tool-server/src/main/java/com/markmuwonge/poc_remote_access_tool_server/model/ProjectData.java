package com.markmuwonge.poc_remote_access_tool_server.model;

public class ProjectData {
	
	private Integer tcpServerPort;
	private Integer webServerPort;
	private Integer tcpServerNoPongReceivedMaxSeconds;
	
	public Integer getTCPServerPort() {
		return tcpServerPort;
	}
	public void setTCPServerPort(Integer tcpServerPort) {
		this.tcpServerPort = tcpServerPort;
	}
	public Integer getWebServerPort() {
		return webServerPort;
	}
	public void setWebServerPort(Integer webServerPort) {
		this.webServerPort = webServerPort;
	}
	public Integer getTCPServerNoPongReceivedMaxSeconds() {
		return tcpServerNoPongReceivedMaxSeconds;
	}
	public void setTCPServerNoPongReceivedMaxSeconds(Integer tcpServerNoPongReceivedMaxSeconds) {
		this.tcpServerNoPongReceivedMaxSeconds = tcpServerNoPongReceivedMaxSeconds;
	}
		
}
