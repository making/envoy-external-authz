package am.ik.envoy;

import io.envoyproxy.envoy.service.auth.v3.AttributeContext;
import io.envoyproxy.envoy.service.auth.v3.AuthorizationGrpc;
import io.envoyproxy.envoy.service.auth.v3.CheckRequest;
import io.envoyproxy.envoy.service.auth.v3.CheckResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.grpc.client.default-channel.address=0.0.0.0:${local.server.port}")
@ImportGrpcClients(types = AuthorizationGrpc.AuthorizationBlockingStub.class)
class AuthorizationServiceImplTest {

	@Autowired
	AuthorizationGrpc.AuthorizationBlockingStub stub;

	@Test
	void testOk() throws Exception {
		CheckResponse checkResponse = this.stub.check(CheckRequest.newBuilder()
			.setAttributes(AttributeContext.newBuilder()
				.setRequest(AttributeContext.Request.newBuilder()
					.setHttp(AttributeContext.HttpRequest.newBuilder()
						.setHost("example.com")
						.setPath("/")
						.setId("12345")
						.putHeaders("X-User", "demo")
						.build())
					.build())
				.build())
			.build());
		assertThat(checkResponse.hasOkResponse()).isTrue();
		assertThat(checkResponse.hasDeniedResponse()).isFalse();
	}

	@Test
	void testDenied() throws Exception {
		CheckResponse checkResponse = this.stub.check(CheckRequest.newBuilder()
			.setAttributes(AttributeContext.newBuilder()
				.setRequest(AttributeContext.Request.newBuilder()
					.setHttp(AttributeContext.HttpRequest.newBuilder()
						.setHost("example.com")
						.setPath("/")
						.setId("12345")
						.putHeaders("X-User", "bar")
						.build())
					.build())
				.build())
			.build());
		assertThat(checkResponse.hasOkResponse()).isFalse();
		assertThat(checkResponse.hasDeniedResponse()).isTrue();
	}

}