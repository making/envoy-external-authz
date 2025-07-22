package am.ik.envoy;

import com.google.protobuf.Timestamp;
import com.google.rpc.Code;
import io.envoyproxy.envoy.config.core.v3.Address;
import io.envoyproxy.envoy.config.core.v3.Metadata;
import io.envoyproxy.envoy.config.core.v3.SocketAddress;
import io.envoyproxy.envoy.service.auth.v3.AttributeContext;
import io.envoyproxy.envoy.service.auth.v3.AuthorizationGrpc;
import io.envoyproxy.envoy.service.auth.v3.CheckRequest;
import io.envoyproxy.envoy.service.auth.v3.CheckResponse;
import io.envoyproxy.envoy.type.v3.StatusCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.grpc.client.default-channel.address=0.0.0.0:${local.grpc.port}",
				"spring.grpc.server.port=0" })
@ImportGrpcClients(types = AuthorizationGrpc.AuthorizationBlockingStub.class)
class AuthorizationServiceImplTest {

	@Autowired
	AuthorizationGrpc.AuthorizationBlockingStub stub;

	@Test
	void testOk() throws Exception {
		CheckResponse checkResponse = this.stub.check(CheckRequest.newBuilder()
			.setAttributes(AttributeContext.newBuilder()
				.setSource(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("192.168.11.12").setPortValue(59206))))
				.setDestination(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("10.1.42.159").setPortValue(8443)))
					.setPrincipal("*.example.com"))
				.setRequest(AttributeContext.Request.newBuilder()
					.setTime(Timestamp.newBuilder().setSeconds(1753161991).setNanos(443256000))
					.setHttp(AttributeContext.HttpRequest.newBuilder()
						.setId("10878827033623646048")
						.setMethod("GET")
						.putHeaders("authorization", "Basic ZGVtbzpwYXNzd29yZA==")
						.setPath("/")
						.setHost("example.com")
						.setScheme("https")
						.setProtocol("HTTP/2")))
				.setMetadataContext(Metadata.newBuilder())
				.setRouteMetadataContext(Metadata.newBuilder()))
			.build());
		assertThat(checkResponse.hasOkResponse()).isTrue();
		assertThat(checkResponse.hasDeniedResponse()).isFalse();
		assertThat(checkResponse.getStatus().getCode()).isEqualTo(Code.OK_VALUE);
	}

	@Test
	void testDeniedWrongPassword() throws Exception {
		CheckResponse checkResponse = this.stub.check(CheckRequest.newBuilder()
			.setAttributes(AttributeContext.newBuilder()
				.setSource(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("192.168.11.12").setPortValue(59206))))
				.setDestination(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("10.1.42.159").setPortValue(8443)))
					.setPrincipal("*.example.com"))
				.setRequest(AttributeContext.Request.newBuilder()
					.setTime(Timestamp.newBuilder().setSeconds(1753161991).setNanos(443256000))
					.setHttp(AttributeContext.HttpRequest.newBuilder()
						.setId("10878827033623646048")
						.setMethod("GET")
						.putHeaders("authorization", "Basic ZGVtbzpwYXNzd29yZDI=")
						.setPath("/")
						.setHost("example.com")
						.setScheme("https")
						.setProtocol("HTTP/2")))
				.setMetadataContext(Metadata.newBuilder())
				.setRouteMetadataContext(Metadata.newBuilder()))
			.build());
		assertThat(checkResponse.hasOkResponse()).isFalse();
		assertThat(checkResponse.hasDeniedResponse()).isTrue();
		assertThat(checkResponse.getDeniedResponse().getStatus().getCode()).isEqualTo(StatusCode.Forbidden);
		assertThat(checkResponse.getStatus().getCode()).isEqualTo(Code.PERMISSION_DENIED_VALUE);
	}

	@Test
	void testDeniedNotExistingUser() throws Exception {
		CheckResponse checkResponse = this.stub.check(CheckRequest.newBuilder()
			.setAttributes(AttributeContext.newBuilder()
				.setSource(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("192.168.11.12").setPortValue(59206))))
				.setDestination(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("10.1.42.159").setPortValue(8443)))
					.setPrincipal("*.example.com"))
				.setRequest(AttributeContext.Request.newBuilder()
					.setTime(Timestamp.newBuilder().setSeconds(1753161991).setNanos(443256000))
					.setHttp(AttributeContext.HttpRequest.newBuilder()
						.setId("10878827033623646048")
						.setMethod("GET")
						.putHeaders("authorization", "Basic YmFyOnBhc3N3b3Jk")
						.setPath("/")
						.setHost("example.com")
						.setScheme("https")
						.setProtocol("HTTP/2")))
				.setMetadataContext(Metadata.newBuilder())
				.setRouteMetadataContext(Metadata.newBuilder()))
			.build());
		assertThat(checkResponse.hasOkResponse()).isFalse();
		assertThat(checkResponse.hasDeniedResponse()).isTrue();
		assertThat(checkResponse.getDeniedResponse().getStatus().getCode()).isEqualTo(StatusCode.Forbidden);
		assertThat(checkResponse.getStatus().getCode()).isEqualTo(Code.PERMISSION_DENIED_VALUE);
	}

	@Test
	void testDeniedNoAuthorization() throws Exception {
		CheckResponse checkResponse = this.stub.check(CheckRequest.newBuilder()
			.setAttributes(AttributeContext.newBuilder()
				.setSource(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("192.168.11.12").setPortValue(59206))))
				.setDestination(AttributeContext.Peer.newBuilder()
					.setAddress(Address.newBuilder()
						.setSocketAddress(SocketAddress.newBuilder().setAddress("10.1.42.159").setPortValue(8443)))
					.setPrincipal("*.example.com"))
				.setRequest(AttributeContext.Request.newBuilder()
					.setTime(Timestamp.newBuilder().setSeconds(1753161991).setNanos(443256000))
					.setHttp(AttributeContext.HttpRequest.newBuilder()
						.setId("10878827033623646048")
						.setMethod("GET")
						.putHeaders("foo", "bar")
						.setPath("/")
						.setHost("example.com")
						.setScheme("https")
						.setProtocol("HTTP/2")))
				.setMetadataContext(Metadata.newBuilder())
				.setRouteMetadataContext(Metadata.newBuilder()))
			.build());
		assertThat(checkResponse.hasOkResponse()).isFalse();
		assertThat(checkResponse.hasDeniedResponse()).isTrue();
		assertThat(checkResponse.getDeniedResponse().getStatus().getCode()).isEqualTo(StatusCode.Unauthorized);
		assertThat(checkResponse.getStatus().getCode()).isEqualTo(Code.UNAUTHENTICATED_VALUE);
	}

}