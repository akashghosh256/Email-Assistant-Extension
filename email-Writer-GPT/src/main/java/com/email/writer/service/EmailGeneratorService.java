package com.email.writer.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.email.writer.dto.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



@Service
public class EmailGeneratorService {
	
	
	private final WebClient webClient;
	
	public EmailGeneratorService(WebClient.Builder webClientBuilder) {
		System.out.println("Contructor called EmailGeneratorService ");
		this.webClient = webClientBuilder.build();
	}
	
	
	@Value("${gemini.api.key}")
	private String geminiAPIKey;
	
	@Value("${gemini.api.url}")
	private String geminiAPIURL;

	public String generateEmailReply(EmailRequest emailRequest) {
	    // 1. Build the prompt
	    String prompt = buildPrompt(emailRequest);

	    // 2. Craft the request body to match the required JSON
	    Map<String, Object> requestBody = Map.of(
	        "contents", List.of(
	            Map.of(
	                "parts", List.of(
	                    Map.of("text", prompt)
	                )
	            )
	        )
	    );

	    // 3. Do request and get a response
	    String response = webClient.post()
	        .uri(geminiAPIURL)
	        .header("Content-Type", "application/json")
	        .header("X-goog-api-key", geminiAPIKey)
	        .bodyValue(requestBody)
	        .retrieve()
	        .bodyToMono(String.class)
	        .block();

	    // 4. Extract and return the response
	    return extractResponseContent(response);
	}


	private String extractResponseContent(String response) {
		try {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode  rootNode = mapper.readTree(response);
		return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
		}
		catch(Exception e) {
			return "Error processing request: " +e.getMessage();
		}
	}

	private String buildPrompt(EmailRequest emailRequest) {
		StringBuilder prompt = new StringBuilder();
		prompt.append("You are working like email assistant to reply the emails it get so avoid adding extra things like subject etc just give simple professional reply ");
//		if(emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
//			prompt.append("use a ").append(emailRequest.getTone()).append("tone.");
//		}
		
		prompt.append("\n Original email: \n").append(emailRequest.getEmailContent());
		return prompt.toString();
	}
	
}
