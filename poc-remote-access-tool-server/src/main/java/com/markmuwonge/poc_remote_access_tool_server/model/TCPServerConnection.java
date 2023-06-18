package com.markmuwonge.poc_remote_access_tool_server.model;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import reactor.netty.Connection;

@Component
@Scope("prototype") //new object, not shared
public class TCPServerConnection {

	private Connection connection;
	private long initEpochSecond;
	private int initPingPongValue;
	private TCPServerConnectionPing tcpServerConnectionPing;
	private TCPServerConnectionPong tcpServerConnectionPong;


	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired private Logger logger;

	public TCPServerConnection(Connection connection, long initEpochSecond, int initPingPongValue) {
		this.connection = connection;
		this.initEpochSecond = initEpochSecond;
		this.initPingPongValue = initPingPongValue;
	} 

	@PostConstruct
	private void postConstruct() {
		tcpServerConnectionPing = applicationContext.getBean(
				TCPServerConnectionPing.class,
				initEpochSecond,
				initPingPongValue
		);
		tcpServerConnectionPong = applicationContext.getBean(
				TCPServerConnectionPong.class,
				initEpochSecond,
				initPingPongValue
		);
	}

	public Connection getConnection() {
		return connection;
	}
	
	public TCPServerConnectionPing getTCPServerConnectionPing() {
		return tcpServerConnectionPing;
	}
	
	public TCPServerConnectionPong getTCPServerConnectionPong() {
		return tcpServerConnectionPong;
	}

	public void dispose() {
		if (!connection.isDisposed()) {
			logger.info("Disconnecting connection {}", connection.address());
			connection.dispose();
		}
	} 
}
