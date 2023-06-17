package com.markmuwonge.poc_remote_access_tool_server.web_socket;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.markmuwonge.poc_remote_access_tool_server.enumerated.TCPServerStatus;
import com.markmuwonge.poc_remote_access_tool_server.tcp.TCPServer;

@Component
public class WebSocketHandler extends TextWebSocketHandler implements PropertyChangeListener {

	private List<WebSocketSession> clientWebSocketSessions;

	@Autowired
	private Logger logger;

	@Autowired
	private TCPServer tcpServer;

	public WebSocketHandler() {
		clientWebSocketSessions = new ArrayList<WebSocketSession>();
	}

	public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
		logger.info("-Client connection established from {}", webSocketSession.getRemoteAddress());
		clientWebSocketSessions.add(webSocketSession);
	}

	public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
		logger.info("-Client connection closed from {} with close status {}", webSocketSession.getRemoteAddress(),
				closeStatus.getCode());
		clientWebSocketSessions.remove(webSocketSession);
	}

	public void handleTextMessage(WebSocketSession webSocketSession, TextMessage textMessage)
			throws InterruptedException, IOException {
		String receivedMessage = textMessage.getPayload();
		logger.info("Received {} from {}", receivedMessage, webSocketSession.getRemoteAddress());

		String response = "";
		if (receivedMessage.equals("TCP_SERVER_STATUS")) {
			response = receivedMessage + ":" + tcpServer.getTCPServerStatus().toString();
		} else if (receivedMessage.equals("START_TCP_SERVER")) {
			tcpServer.start();
			return;
		} else if (receivedMessage.equals("STOP_TCP_SERVER")) {
			tcpServer.stop();
			return;
		} else if (receivedMessage.equals("TCP_SERVER_CONNECTIONS")) {
			response = receivedMessage + ":" + tcpServer.getTCPServerConnections().toString();
		}else {
			return;
		}

		webSocketSession.sendMessage(new TextMessage(response));
	}

	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
		if (propertyChangeEvent.getPropertyName().equals("tcp_server_status")) {
			clientWebSocketSessions.forEach(clientWebSocketSession -> {
				TCPServerStatus newTCPServerStatus = (TCPServerStatus) propertyChangeEvent.getNewValue();
				try {
					clientWebSocketSession.sendMessage(new TextMessage(
							propertyChangeEvent.getPropertyName() + ":" + newTCPServerStatus.toString()));
				} catch (Exception e) {
					logger.error("-An exception occurred handling property change '{}': {}",
							propertyChangeEvent.getPropertyName(), e.toString());
				}
			});
		} else if (propertyChangeEvent.getPropertyName().equals("tcp_server_connections")) {	
			clientWebSocketSessions.forEach(clientWebSocketSession -> {
				try {
					clientWebSocketSession.sendMessage(new TextMessage(
							propertyChangeEvent.getPropertyName() + ":" + tcpServer.getTCPServerConnections()));
				} catch (Exception e) {
					logger.error("-An exception occurred handling tcp server connection property change '{}': {}",
							propertyChangeEvent.getPropertyName(), e.toString());
				}
			});
		}
	}
}