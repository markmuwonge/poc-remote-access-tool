package com.markmuwonge.poc_remote_access_tool_server.helper;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JavaObjectToJSONObjectConverter {
	 @Autowired
	 private ObjectMapper objectMapper;
	
	 @Autowired
	 private Logger logger;
	
	 public <S> org.json.JSONObject convert(Object javaObject) {
		
		 org.json.JSONObject jsonObject = null;
		 
			 try {
				 jsonObject = new org.json.JSONObject(objectMapper.writeValueAsString(javaObject));
			 }
			 catch(Exception e) {		
				 logger.error("-An exception occurred when attempting to convert a {} object to a JSONObject. Error message: {}", javaObject.getClass().getName(), e.toString() );
			 }
			 return jsonObject;
		 
	 }
}
