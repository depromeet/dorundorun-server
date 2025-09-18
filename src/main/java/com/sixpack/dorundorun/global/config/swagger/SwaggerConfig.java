package com.sixpack.dorundorun.global.config.swagger;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

	private static final String SERVER_NAME = "sixpack";
	private static final String API_TITLE = "sixpack 서버 API 문서";
	private static final String API_DESCRIPTION = "sixpack 서버 API 문서입니다.";

	@Value("${swagger.version:0.0.1}")
	private String version;

	@Value("${swagger.server-url:http://localhost:8080}")
	private String serverUrl;

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.servers(swaggerServers())
			.components(swaggerComponents())
			.info(swaggerInfo())
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}

	private List<Server> swaggerServers() {
		Server server = new Server().url(serverUrl).description(API_DESCRIPTION);
		return List.of(server);
	}

	private Components swaggerComponents() {
		return new Components()
			.addSecuritySchemes("bearerAuth", new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.in(SecurityScheme.In.HEADER)
				.name("Authorization"));
	}

	private Info swaggerInfo() {
		License license = new License();
		license.setName(SERVER_NAME);

		return new Info()
			.version("v" + version)
			.title(API_TITLE)
			.description(API_DESCRIPTION)
			.license(license);
	}

	@Bean
	public ModelResolver modelResolver(ObjectMapper objectMapper) {
		return new ModelResolver(objectMapper);
	}
}

