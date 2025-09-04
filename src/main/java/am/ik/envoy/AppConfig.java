package am.ik.envoy;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
class AppConfig {

	@Bean
	@SuppressWarnings("deprecation")
	PasswordEncoder passwordEncoder() {
		return new DelegatingPasswordEncoder("bcrypt",
				Map.of("bcrypt", new BCryptPasswordEncoder(), "noop", NoOpPasswordEncoder.getInstance()));
	}

}
