package com.markmuwonge.poc_remote_access_tool_server.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

import com.markmuwonge.poc_remote_access_tool_server.model.ProjectData;

@Configuration("ServerPortCustomizer")
public class WebServerPortCustomizer   implements WebServerFactoryCustomizer<ConfigurableWebServerFactory>{
	
	@Autowired private ProjectData projectData;

	
	@Override
	public void customize(ConfigurableWebServerFactory factory) {
		 factory.setPort(projectData.getWebServerPort()); 
	}
}

