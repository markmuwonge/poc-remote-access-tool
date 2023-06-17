package com.markmuwonge.poc_remote_access_tool_server.helper;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

@Component
public class CurrentUTCTimeRetriever {
	public ZonedDateTime retrieve() {
		return ZonedDateTime.now(ZoneId.of("UTC"));
	}
}

