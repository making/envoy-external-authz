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
import io.envoyproxy.envoy.service.auth.v3.DeniedHttpResponse;
import io.envoyproxy.envoy.type.v3.StatusCode;
import java.util.Base64;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.grpc.client.default-channel.address=0.0.0.0:${local.grpc.port}", "spring.grpc.server.port=0",
		"envoy.authz.users=demo:{noop}password,test:{noop}foo,encoded:{bcrypt}$2a$10$NrE2QCpXLEG.Nn/YvEvqR.fw/9MzlpyQASaxkEyO6LHVTN7RSMv8y",
		"envoy.authz.bearer-tokens={noop}secret,{bcrypt}$2a$10$NrE2QCpXLEG.Nn/YvEvqR.fw/9MzlpyQASaxkEyO6LHVTN7RSMv8y" })
@ImportGrpcClients(types = AuthorizationGrpc.AuthorizationBlockingStub.class)
class AuthorizationServiceImplTest {

	@Autowired
	AuthorizationGrpc.AuthorizationBlockingStub stub;

	@ParameterizedTest
	@CsvSource({ "demo,password", "test,foo", "encoded,password", ",secret", ",password" })
	void testOkBasic(String username, String password) throws Exception {
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
						.putHeaders("authorization", "Basic " + Base64.getEncoder()
							.encodeToString((Objects.requireNonNullElse(username, "") + ":" + password).getBytes()))
						.setPath("/")
						.setHost("example.com")
						.setScheme("https")
						.setProtocol("HTTP/2")))
				.setMetadataContext(Metadata.newBuilder())
				.setRouteMetadataContext(Metadata.newBuilder()))
			.build());
		assertThat(checkResponse.hasOkResponse()).isTrue();
		assertThat(checkResponse.hasDeniedResponse()).isFalse();
		if (username != null) {
			assertThat(checkResponse.getOkResponse().getHeadersList()).anySatisfy(header -> {
				assertThat(header.getHeader().getKey()).isEqualTo("X-User");
				assertThat(header.getHeader().getValue()).isEqualTo(username);
			});
		}
		assertThat(checkResponse.getStatus().getCode()).isEqualTo(Code.OK_VALUE);
	}

	@ParameterizedTest
	@CsvSource({ "secret", "password" })
	void testOkBearer(String token) throws Exception {
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
						.putHeaders("authorization", "Bearer " + token)
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
		DeniedHttpResponse deniedResponse = checkResponse.getDeniedResponse();
		assertThat(deniedResponse.getStatus().getCode()).isEqualTo(StatusCode.Unauthorized);
		assertThat(deniedResponse.getHeadersList()).anySatisfy(header -> {
			assertThat(header.getHeader().getKey()).isEqualTo(HttpHeaders.WWW_AUTHENTICATE);
			assertThat(header.getHeader().getValue()).isEqualTo("Basic realm=\"Envoy External Auth\"");
		});
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
		DeniedHttpResponse deniedResponse = checkResponse.getDeniedResponse();
		assertThat(deniedResponse.getStatus().getCode()).isEqualTo(StatusCode.Unauthorized);
		assertThat(deniedResponse.getHeadersList()).anySatisfy(header -> {
			assertThat(header.getHeader().getKey()).isEqualTo(HttpHeaders.WWW_AUTHENTICATE);
			assertThat(header.getHeader().getValue()).isEqualTo("Basic realm=\"Envoy External Auth\"");
		});
		assertThat(checkResponse.getStatus().getCode()).isEqualTo(Code.PERMISSION_DENIED_VALUE);
	}

	@Test
	void testDeniedWrongToken() throws Exception {
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
						.putHeaders("authorization", "Bearer token")
						.setPath("/")
						.setHost("example.com")
						.setScheme("https")
						.setProtocol("HTTP/2")))
				.setMetadataContext(Metadata.newBuilder())
				.setRouteMetadataContext(Metadata.newBuilder()))
			.build());
		assertThat(checkResponse.hasOkResponse()).isFalse();
		assertThat(checkResponse.hasDeniedResponse()).isTrue();
		DeniedHttpResponse deniedResponse = checkResponse.getDeniedResponse();
		assertThat(deniedResponse.getStatus().getCode()).isEqualTo(StatusCode.Unauthorized);
		assertThat(deniedResponse.getHeadersList()).anySatisfy(header -> {
			assertThat(header.getHeader().getKey()).isEqualTo(HttpHeaders.WWW_AUTHENTICATE);
			assertThat(header.getHeader().getValue()).isEqualTo("Basic realm=\"Envoy External Auth\"");
		});
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
		DeniedHttpResponse deniedResponse = checkResponse.getDeniedResponse();
		assertThat(deniedResponse.getStatus().getCode()).isEqualTo(StatusCode.Unauthorized);
		assertThat(deniedResponse.getHeadersList()).anySatisfy(header -> {
			assertThat(header.getHeader().getKey()).isEqualTo(HttpHeaders.WWW_AUTHENTICATE);
			assertThat(header.getHeader().getValue()).isEqualTo("Basic realm=\"Envoy External Auth\"");
		});
		assertThat(checkResponse.getStatus().getCode()).isEqualTo(Code.PERMISSION_DENIED_VALUE);
	}

}