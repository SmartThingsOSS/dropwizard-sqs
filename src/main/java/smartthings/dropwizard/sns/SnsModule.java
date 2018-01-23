package smartthings.dropwizard.sns;

import com.google.inject.multibindings.OptionalBinder;
import smartthings.dropwizard.sns.internal.DefaultSnsService;
import smartthings.dropwizard.sns.internal.providers.DefaultAmazonSNSProvider;
import smartthings.dw.guice.AbstractDwModule;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SnsModule extends AbstractDwModule {

    @Override
    protected void configure() {
        OptionalBinder.newOptionalBinder(binder(), AmazonSNSProvider.class)
            .setDefault()
            .to(DefaultAmazonSNSProvider.class);

        OptionalBinder.newOptionalBinder(binder(), SnsService.class)
            .setDefault()
            .to(DefaultSnsService.class);
    }

    public static class Config {
        private boolean enabled;
        private List<EndpointConfig> endpoints = Collections.emptyList();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<EndpointConfig> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<EndpointConfig> endpoints) {
            this.endpoints = endpoints;
        }
    }

    public static class EndpointConfig {
        private String regionName;
        private String endpoint;

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Optional<String> endpoint() {
            return Optional.ofNullable(endpoint);
        }
    }
}
