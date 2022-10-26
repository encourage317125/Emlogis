package com.emlogis.test.providers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonJSONTest {
	
	public static Map<String, String> messageTemplateMap = new HashMap<>();
	
	public static void main(String[] args) {
		
		messageTemplateMap.put("open_shift_notification_body_html_en", "open_shift_notification_body_html_en.ftl");
		messageTemplateMap.put("open_shift_notification_body_sms_en", "open_shift_notification_body_sms_en.ftl");
		messageTemplateMap.put("open_shift_notification_body_text_en", "open_shift_notification_body_text_en.ftl");
		messageTemplateMap.put("open_shift_notification_subject_html_en", "open_shift_notification_subject_html_en.ftl");
		messageTemplateMap.put("open_shift_notification_subject_text_en", "open_shift_notification_subject_text_en.ftl");
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.writeValue(new File("testmap.txt"), messageTemplateMap);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
