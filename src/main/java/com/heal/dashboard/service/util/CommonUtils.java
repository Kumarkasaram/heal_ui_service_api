package com.heal.dashboard.service.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;

@Slf4j
public class CommonUtils {

	public static ObjectMapper getObjectMapperWithHtmlEncoder() {
		ObjectMapper objectMapper = new ObjectMapper();

		SimpleModule simpleModule = new SimpleModule("HTML-Encoder", objectMapper.version()).addDeserializer(String.class, new EscapeHTML());

		objectMapper.registerModule(simpleModule);

		return objectMapper;
	}
}

class EscapeHTML extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException {
		String s = jp.getValueAsString();
		return StringEscapeUtils.escapeHtml4(s);
	}
}

