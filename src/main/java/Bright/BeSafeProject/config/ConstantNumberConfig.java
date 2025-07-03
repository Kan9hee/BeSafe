package Bright.BeSafeProject.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "constant")
@Getter
@Setter
public class ConstantNumberConfig {
    private int firstPage;
    private int parallelStartPage;
    private int pageSize;
    private int concurrency;
    private int codecSize;
    private double radius;
    private double metersPerDegree;
}
