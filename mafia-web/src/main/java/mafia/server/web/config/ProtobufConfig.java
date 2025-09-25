package mafia.server.web.config;

import com.google.protobuf.util.JsonFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ProtobufConfig implements WebMvcConfigurer {

    @Bean
    @Primary
    public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    @Bean
    public ProtobufJsonFormatHttpMessageConverter protobufJsonFormatHttpMessageConverter() {
        JsonFormat.Parser parser = JsonFormat.parser()
                .ignoringUnknownFields();

        JsonFormat.Printer printer = JsonFormat.printer()
                .omittingInsignificantWhitespace();

        return new ProtobufJsonFormatHttpMessageConverter(parser, printer);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(protobufHttpMessageConverter());
        converters.add(protobufJsonFormatHttpMessageConverter());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .defaultContentType(MediaType.APPLICATION_PROTOBUF)
                .mediaType("protobuf", MediaType.APPLICATION_PROTOBUF)
                .mediaType("json", MediaType.APPLICATION_JSON);
    }
}
