package co.com.bancolombia.secretsmanager.vault.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpProperties {

    @Builder.Default
    private int connectionTimeout = 5; //in seconds
}
