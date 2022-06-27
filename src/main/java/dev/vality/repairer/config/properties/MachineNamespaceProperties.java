package dev.vality.repairer.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "machine-namespace")
public class MachineNamespaceProperties {
    private String invoicingNs;
    private String withdrawalSessionNs;
}
