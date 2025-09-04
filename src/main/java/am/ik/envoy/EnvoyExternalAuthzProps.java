package am.ik.envoy;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "envoy.authz")
public record EnvoyExternalAuthzProps(@NestedConfigurationProperty @DefaultValue("") List<User> users,
		@DefaultValue("") List<String> bearerTokens) {

	public record User(String username, String password) {

		public static User valueOf(String s) {
			String[] parts = s.split(":");
			if (parts.length != 2) {
				throw new IllegalArgumentException("Invalid user format: " + s);
			}
			return new User(parts[0], parts[1]);
		}
	}
}
