package com.email.writer.dto;

import lombok.Data;

@Data
public class EmailRequest {
	
private String emailContent;
private String tone;

public String getTone() {
	return tone;
}
public String getEmailContent() {
	return emailContent;
	}
}
