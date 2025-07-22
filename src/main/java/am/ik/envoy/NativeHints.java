package am.ik.envoy;

import com.google.protobuf.DescriptorProtos.FeatureSet;
import io.envoyproxy.envoy.config.core.v3.HeaderMap;
import io.envoyproxy.envoy.config.core.v3.HeaderValue;
import io.envoyproxy.envoy.config.core.v3.Metadata;
import io.envoyproxy.envoy.service.auth.v3.AttributeContext;
import io.envoyproxy.envoy.service.auth.v3.CheckRequest;
import io.envoyproxy.envoy.service.auth.v3.CheckResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(NativeHints.RuntimeHints.class)
public class NativeHints {

	public static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {
			List<Method> methods = new ArrayList<>();
			methods.addAll(Arrays.stream(FeatureSet.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.FieldPresence.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.EnumType.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.RepeatedFieldEncoding.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.Utf8Validation.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.MessageEncoding.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.EnforceNamingStyle.class.getMethods()).toList());
			methods.addAll(Arrays.stream(FeatureSet.JsonFormat.class.getMethods()).toList());
			methods.addAll(Arrays.stream(CheckRequest.class.getMethods()).toList());
			methods.addAll(Arrays.stream(CheckRequest.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(CheckResponse.class.getMethods()).toList());
			methods.addAll(Arrays.stream(CheckResponse.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.Request.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.Request.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.HttpRequest.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.HttpRequest.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.Peer.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.Peer.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.TLSSession.class.getMethods()).toList());
			methods.addAll(Arrays.stream(AttributeContext.TLSSession.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(Metadata.class.getMethods()).toList());
			methods.addAll(Arrays.stream(Metadata.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(HeaderMap.class.getMethods()).toList());
			methods.addAll(Arrays.stream(HeaderMap.Builder.class.getMethods()).toList());
			methods.addAll(Arrays.stream(HeaderValue.class.getMethods()).toList());
			methods.addAll(Arrays.stream(HeaderValue.Builder.class.getMethods()).toList());
			for (Method method : methods) {
				hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
			}
		}

	}

}
