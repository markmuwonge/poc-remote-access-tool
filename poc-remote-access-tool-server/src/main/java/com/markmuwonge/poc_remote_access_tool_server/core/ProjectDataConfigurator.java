package com.markmuwonge.poc_remote_access_tool_server.core;

import java.io.File;
import java.nio.file.Files;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;

import com.markmuwonge.poc_remote_access_tool_server.model.ProjectData;

@Configuration
public class ProjectDataConfigurator {
	@Autowired private Logger logger;
	
	@Autowired
    private ApplicationContext appContext;

	
	@Bean
	public ProjectData projectData() {
		ProjectData projectData = new ProjectData();
		try {
			String userDir = System.getProperty("user.dir");
			File file = new File(userDir + "//" + "config.json");
			if (!file.exists()) {
				throw new Exception("Config file not found");
			}
			JSONObject config = new JSONObject(Files.readString(file.toPath()));
			
			int tcpServerPort = config.getJSONObject("tcp_server").getInt("port");
			SocketUtils.findAvailableTcpPort(tcpServerPort, tcpServerPort); //throws exception
			projectData.setTCPServerPort(tcpServerPort);
			
			int webServerPort = config.getJSONObject("web_server").getInt("port");
			SocketUtils.findAvailableTcpPort(webServerPort, webServerPort); //throws exception
			projectData.setWebServerPort(webServerPort);
		}catch(Exception e) {
			logger.error("*An error occurred setting project data bean: {}", e.toString());
			System.exit(SpringApplication.exit(appContext, () -> 1));
		}
		finally {
			logger.info("Project data bean set:{}", projectData != null ? true : false);
		}
		
		return projectData;
	}
}
