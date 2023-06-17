package com.markmuwonge.poc_remote_access_tool_server.tcp;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.json.JSONObject;
//
//import java.util.List;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.primitives.Bytes;
//
import com.markmuwonge.poc_remote_access_tool_server.enumerated.TCPServerStatus;
import com.markmuwonge.poc_remote_access_tool_server.helper.CurrentUTCTimeRetriever;
import com.markmuwonge.poc_remote_access_tool_server.helper.JavaObjectToJSONObjectConverter;
import com.markmuwonge.poc_remote_access_tool_server.model.ProjectData;
import com.markmuwonge.poc_remote_access_tool_server.model.TCPServerConnection;
import com.markmuwonge.poc_remote_access_tool_server.model.TCPServerConnectionPing;
import com.markmuwonge.poc_remote_access_tool_server.web_socket.WebSocketHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.DisposableServer;


@Component
public class TCPServer {
	
	private PropertyChangeSupport propertyChangeSupport;
	private TCPServerStatus tcpServerStatus = TCPServerStatus.STOPPED;
	private reactor.netty.tcp.TcpServer tcpServer;
	private DisposableServer disposableServer;
	private List<TCPServerConnection> tcpServerConnections;
	private final int noPongReceivedMaxSeconds = 60;
	
	@Autowired private Logger logger;
	@Autowired private WebSocketHandler webSocketHandler;
	@Autowired private ProjectData projectData;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private CurrentUTCTimeRetriever currentUTCTimeRetriever;
	@Autowired private Random random;
	@Autowired private JavaObjectToJSONObjectConverter javaObjectToJSONObjectConverter;
	
	public TCPServer(){
		tcpServerConnections = new ArrayList<TCPServerConnection>();
	}

	@PostConstruct
	private void postConstruct() {
		logger.info("Starting TCP Server");
		propertyChangeSupport = new PropertyChangeSupport(this);
		propertyChangeSupport.addPropertyChangeListener(webSocketHandler);
	}
	
	public void start() {
		if (tcpServerStatus == TCPServerStatus.STARTED) return;
		tcpServerStatus = TCPServerStatus.STARTED;
		
		tcpServer = reactor.netty.tcp.TcpServer.create()
		.port(projectData.getTCPServerPort()) 
		.doOnConnection(connection -> {
			logger.info(
				"TCP Server connection received from {}", 
				connection.address()
			);
			List<TCPServerConnection> oldTCPServerConnections = new ArrayList<TCPServerConnection>(tcpServerConnections);
			TCPServerConnection tcpServerConnection = applicationContext.getBean(
				TCPServerConnection.class,
				connection,
				currentUTCTimeRetriever.retrieve().toInstant().getEpochSecond(),
				random.nextInt(50)
			);
			tcpServerConnections.add(tcpServerConnection);
			logger.info("TCP Server connection count: {}", tcpServerConnections.size());
			propertyChangeSupport.firePropertyChange(
				"tcp_server_connections",
				oldTCPServerConnections,
				tcpServerConnections
			);
			
			runPingSequence(tcpServerConnection);
			runPongCheckSequence(tcpServerConnection);
			connection.addHandler(getChannelInboundHandlerAdapter(tcpServerConnection));
		})
		.handle((inbound, outbound) -> {
			inbound.receive().subscribe();
			return outbound.neverComplete();
		})
		.observe(new ConnectionObserver() {
			@Override
			public void onStateChange(Connection connection, State newState) {
				if (newState == State.DISCONNECTING) {
					logger.info(
						"Connection from {} has disconnected",
						connection.address()
					);
					TCPServerConnection tcpServerConnection = tcpServerConnections.stream()
					.filter(tsc -> tsc.getConnection() == connection )
					.findFirst().orElse(null);
					if (tcpServerConnection != null) {
						disconnect(tcpServerConnection);
					}
				}
			}
		}); 
		tcpServerStatus = TCPServerStatus.STARTED;
		propertyChangeSupport.firePropertyChange(
				"tcp_server_status",
				TCPServerStatus.STOPPED,
				tcpServerStatus
		);
		logger.info("TCP Server started");
		
		disposableServer = tcpServer.bindNow();
	}
	
	public void stop() {
		if (tcpServerStatus == TCPServerStatus.STOPPED) return;
		
		disposableServer.dispose();
		
		tcpServerStatus = TCPServerStatus.STOPPED;
		
		tcpServerConnections.stream().forEach(tcpServerConnection -> {
			disconnect(tcpServerConnection);
		});
		List<TCPServerConnection> oldTCPServerConnections = new ArrayList<TCPServerConnection>(
				tcpServerConnections
		);
		tcpServerConnections.clear();
		
		propertyChangeSupport.firePropertyChange(
				"tcp_server_status",
				TCPServerStatus.STARTED,
				tcpServerStatus
		);
		propertyChangeSupport.firePropertyChange(
				"tcp_server_connections",
				oldTCPServerConnections,
				tcpServerConnections
			);
		logger.info("TCP Server stopped");
	}
	
	public void disconnect(TCPServerConnection tcpServerConnection) {
		List<TCPServerConnection> oldTcpServerConnections = new ArrayList<TCPServerConnection>(
				tcpServerConnections
		);

		tcpServerConnection.dispose();
		tcpServerConnections.remove(tcpServerConnection);
		logger.info("TCP Server connection count: {}", tcpServerConnections.size());
		propertyChangeSupport.firePropertyChange(
				"tcp_server_connections",
				oldTcpServerConnections,
				tcpServerConnections
		);		
	}
	
	private void runPingSequence(TCPServerConnection tcpServerConnection) {
		Connection connection = tcpServerConnection.getConnection();
		CompletableFuture.runAsync(() -> {
			while (!connection.isDisposed() && tcpServerConnections.contains(tcpServerConnection)) {
				TCPServerConnectionPing tcpServerConnectionPing = tcpServerConnection.getTCPServerConnectionPing();
				
				if (tcpServerConnection.getTCPServerConnectionPong().getEpochSecond() >= 
						tcpServerConnection.getTCPServerConnectionPing().getEpochSecond() &&
						!tcpServerConnectionPing.isLocked()
					) {
					logger.info("Pinging connection {}", connection.address());	
					tcpServerConnectionPing.setEpochSecond(
							currentUTCTimeRetriever.retrieve().toInstant().getEpochSecond()
					);
					tcpServerConnectionPing.setValue(random.nextInt(50));	
					tcpServerConnectionPing.setLocked(true);
					send(
							connection,
							Bytes.concat(
								javaObjectToJSONObjectConverter.convert(
										tcpServerConnectionPing
								).toString().getBytes(),
									new byte[] {0} //null byte
							)
					);
				}
				try {
					Thread.sleep(Duration.ofSeconds(1).toMillis());
				}
				catch(Exception e) {
					logger.error("An exception occurred attempting to sleep thread during connection ping sending process. Error message: {}", e.toString());
				}
			}
		});
	}
	
	private void runPongCheckSequence(TCPServerConnection tcpServerConnection) {
		Connection connection = tcpServerConnection.getConnection();
		CompletableFuture.runAsync(() -> {
			while (!connection.isDisposed() && tcpServerConnections.contains(tcpServerConnection)) {
				if (currentUTCTimeRetriever.retrieve().toInstant().getEpochSecond() - 
					tcpServerConnection.getTCPServerConnectionPong().getEpochSecond() > 
					noPongReceivedMaxSeconds
				) {
						logger.warn("Haven't received a pong from {} in {} seconds", connection.address(), noPongReceivedMaxSeconds);
						disconnect(tcpServerConnection);
				}
				
				try {
					Thread.sleep(Duration.ofSeconds(1).toMillis());
				}
				catch(Exception e) {
					logger.error("An exception occurred attempting to sleep thread during connection pong checking process. Error message: {}", e.toString());
				}
			}
		});
	}
	
	public void send(Connection connection, byte[] message) {
		logger.info("Sending {} to {}", new String(message), connection.address());
		ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
		connection.channel().writeAndFlush(byteBuf);
	}
	
	private ChannelInboundHandlerAdapter getChannelInboundHandlerAdapter(TCPServerConnection tcpServerConnection) {
		Connection connection = tcpServerConnection.getConnection();
		return new ChannelInboundHandlerAdapter() {
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object inbound) throws Exception {
				ByteBuf byteBuf = (ByteBuf) inbound;
				byte[] receivedDataBytes = new byte[byteBuf.readableBytes()];
				byteBuf.readBytes(receivedDataBytes);
				logger.info("Received {} bytes from {}", receivedDataBytes.length, connection.address());
				try {
					JSONObject receivedJSONObject = new JSONObject(new String(receivedDataBytes));
					if (receivedJSONObject.getString("action").equals("pong")) {
////
						if (receivedJSONObject.getInt("value") == tcpServerConnection.getTCPServerConnectionPing().getValue() + 1) {
							logger.info("Pong received from {}", connection.address());
							tcpServerConnection.getTCPServerConnectionPong().setEpochSecond(
									currentUTCTimeRetriever.retrieve().toInstant().getEpochSecond()
							);
							tcpServerConnection.getTCPServerConnectionPing().setLocked(false);
						}
					}
				}catch(Exception e) {
					logger.warn("-An exception occurred attempting to read {} bytes from {}. Error message: {}",
							receivedDataBytes.length, connection.address(), e.toString());
				}
			}
		};
	}
	
	
	
	public TCPServerStatus getTCPServerStatus() {
		return tcpServerStatus; 
	}
	
	public List<JSONObject> getTCPServerConnections() {
		return tcpServerConnections.stream().map(tcpServerConnection -> {
			return new JSONObject()
					.put(
							"ip_address",
							tcpServerConnection.getConnection().address().getHostString()
					)
					.put(
							"port", 
							tcpServerConnection.getConnection().address().getPort()
					);
		}).collect(Collectors.toList());
	}
	
	@PreDestroy
	private void preDestroy() {
		stop();
	}
}
