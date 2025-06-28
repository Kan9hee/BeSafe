package Bright.BeSafeProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class CommonBeanConfig {
    @Bean
    public PathPatternParser pathPatternParser() {
        return new PathPatternParser();
    }
}
