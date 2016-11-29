package com.rsomeara.github.integration.server;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class GitHubWebHookRestServer {

	@RequestMapping(path = "/github/webhook", method = RequestMethod.POST)
	public ResponseEntity<Void> receiveEvent(HttpEntity<String> httpEntity){
		List<String> eventTypeHeaders = httpEntity.getHeaders().get("X-GitHub-Event");
		
		for(String eventType : eventTypeHeaders){
			System.out.println("Received event type: " + eventType);
		}
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
}
