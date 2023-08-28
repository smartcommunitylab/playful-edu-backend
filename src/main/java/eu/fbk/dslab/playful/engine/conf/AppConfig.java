package eu.fbk.dslab.playful.engine.conf;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/*
 * extend WebMvcConfigurerAdapter and not use annotation @EnableMvc to permit correct static
 * resources publishing and restController functionalities
 */
@Configuration
@EnableWebMvc
public class AppConfig implements WebMvcConfigurer {

    @Value("${server_url:/}")
    private String serverUrl;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedMethods("*");
	}

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(25000);
        return new RestTemplate(factory);        
    }

	@Bean
    public OpenAPI springOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .servers(List.of(new Server().url(serverUrl)))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                    new Components()
                        .addSecuritySchemes(securitySchemeName,
                            new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                )                
                .info(new Info().title("Playful Education Project")
                .version("1.0.0")
                .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                .description("DSLab")
                .url("https://www.smartcommunitylab.it/"));
    }

}
