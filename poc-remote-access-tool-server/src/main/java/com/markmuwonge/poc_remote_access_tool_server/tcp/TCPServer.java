package com.markmuwonge.poc_remote_access_tool_server.tcp;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.markmuwonge.poc_remote_access_tool_server.enumerated.TcpServerStatus;
import com.markmuwonge.poc_remote_access_tool_server.model.ProjectData;

import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.DisposableServer;

@Component
public class TCPServer {

	@Autowired
	private Logger logger;
	
	@Autowired private ProjectData projectData;

	private reactor.netty.tcp.TcpServer tcpServer;
	private DisposableServer disposableServer;
	private TcpServerStatus tcpServerStatus = TcpServerStatus.STOPPED;

	@PostConstruct
	private void postConstruct() {
		logger.info("Starting TCP Server");
		
		tcpServer = reactor.netty.tcp.TcpServer.create()
				.port(projectData.getTCPServerPort()) 
				.doOnConnection(connection -> {
					logger.info("Tcp Server connection received from {}", connection.address());
				})
				.handle((inbound, outbound) -> {
					inbound.receive().subscribe();
					return outbound.neverComplete();
				})
				.observe(new ConnectionObserver() {
					@Override
					public void onStateChange(Connection connection, State newState) {
						if (newState == State.DISCONNECTING) {
							logger.info("Connection from {} has disconnected", connection.address());
						}
					}
				});
				disposableServer = tcpServer.bindNow();
				tcpServerStatus = TcpServerStatus.STARTED;
	}
}
