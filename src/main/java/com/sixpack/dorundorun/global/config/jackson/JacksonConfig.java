package com.sixpack.dorundorun.global.config.jackson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalDateTime.class, new ZuluLocalDateTimeSerializer());
		javaTimeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());

		objectMapper.registerModule(javaTimeModule);
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return objectMapper;
	}

	public static class ZuluLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
		private static final DateTimeFormatter ZULU_FORMATTER = DateTimeFormatter.ofPattern(
			"yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

		@Override
		public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws
			IOException {
			gen.writeString(value.format(ZULU_FORMATTER));
		}
	}

	public static class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
		private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
			// Zulu 형식들
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
			// 일반 ISO 형식들 (Z 없음)
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		);

		@Override
		public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			String value = p.getValueAsString();
			if (value == null || value.trim().isEmpty()) {
				return null;
			}

			for (DateTimeFormatter formatter : FORMATTERS) {
				try {
					return LocalDateTime.parse(value, formatter);
				} catch (Exception ignored) {
					// 다음 formatter 시도
				}
			}

			throw new IOException("Cannot parse LocalDateTime: " + value +
				". Supported formats: ISO formats with/without Z suffix and microseconds/milliseconds");
		}
	}
}
