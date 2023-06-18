package com.markmuwonge.poc_remote_access_tool_server.controller;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.markmuwonge.poc_remote_access_tool_server.tcp.TCPServer;

////
@Controller
public class ServerController {

	@Autowired
	private TCPServer tcpServer;

	@GetMapping(path = "/tcp_server_connection_fields", produces = "text/json")
	public ResponseEntity<String> tcpServerConnectionFields() {
		JSONArray jsonArray = new JSONArray();
		tcpServer.getTCPConnectionFields().forEach(tcpConnectionField -> {
			jsonArray.put(tcpConnectionField);
		});
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(jsonArray.toString());
	}

	@GetMapping("/")
    public String index() {
        String functionName = new Object() {}
        .getClass()
        .getEnclosingMethod()
        .getName();
      return functionName; 
    }
}