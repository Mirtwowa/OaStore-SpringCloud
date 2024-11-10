package org.example.oastoregateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "exclusion")
public class ExclusionUrl {
    private List<String> url;
}
