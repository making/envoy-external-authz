package am.ik.envoy;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.envoyproxy.envoy.config.core.v3.HeaderValue;
import io.envoyproxy.envoy.config.core.v3.HeaderValueOption;
import io.envoyproxy.envoy.service.auth.v3.AttributeContext;
import io.envoyproxy.envoy.service.auth.v3.AuthorizationGrpc;
import io.envoyproxy.envoy.service.auth.v3.CheckRequest;
import io.envoyproxy.envoy.service.auth.v3.CheckResponse;
import io.envoyproxy.envoy.service.auth.v3.DeniedHttpResponse;
import io.envoyproxy.envoy.service.auth.v3.OkHttpResponse;
import io.envoyproxy.envoy.type.v3.HttpStatus;
import io.envoyproxy.envoy.type.v3.StatusCode;
import io.grpc.stub.StreamObserver;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl extends AuthorizationGrpc.AuthorizationImplBase {

	private final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

	@Override
	public void check(CheckRequest request, StreamObserver<CheckResponse> responseObserver) {
		if (logger.isTraceEnabled()) {
			logger.trace("request details: {}", request);
		}
		AttributeContext.HttpRequest req = request.getAttributes().getRequest().getHttp();
		if (logger.isInfoEnabled()) {
			logger.info("Check request host={} path={} id={}", req.getHost(), req.getPath(), req.getId());
		}
		String authorization = req.getHeadersMap().get("authorization");
		if (authorization == null || !authorization.startsWith("Basic ")) {
			responseObserver.onNext(CheckResponse.newBuilder()
				.setStatus(Status.newBuilder().setCode(Code.UNAUTHENTICATED_VALUE).build())
				.setDeniedResponse(DeniedHttpResponse.newBuilder()
					.setStatus(HttpStatus.newBuilder().setCode(StatusCode.Unauthorized).build())
					.addHeaders(HeaderValueOption.newBuilder()
						.setHeader(HeaderValue.newBuilder()
							.setKey("X-Auth-Handler")
							.setValue("am.ik.envoy.AuthorizationServiceImpl.check")
							.build())
						.build())
					.build())
				.build());
			responseObserver.onCompleted();
			return;
		}

		String basic = new String(Base64.getDecoder().decode(authorization.substring("Basic ".length())));
		String[] parts = basic.split(":", 2);
		String user = parts[0];
		String password = parts.length > 1 ? parts[1] : "";
		logger.info("Authenticating user={}", user);
		if ("demo".equals(user) && "password".equals(password)) {
			responseObserver.onNext(CheckResponse.newBuilder()
				.setStatus(Status.newBuilder().setCode(Code.OK_VALUE).build())
				.setOkResponse(OkHttpResponse.newBuilder()
					.addHeaders(HeaderValueOption.newBuilder()
						.setHeader(HeaderValue.newBuilder().setKey("X-User").setValue(user).build())
						.setHeader(HeaderValue.newBuilder()
							.setKey("X-Auth-Handler")
							.setValue("am.ik.envoy.AuthorizationServiceImpl.check")
							.build())
						.build())
					.build())
				.build());
		}
		else {
			responseObserver.onNext(CheckResponse.newBuilder()
				.setStatus(Status.newBuilder().setCode(Code.PERMISSION_DENIED_VALUE).build())
				.setDeniedResponse(DeniedHttpResponse.newBuilder()
					.setStatus(HttpStatus.newBuilder().setCode(StatusCode.Forbidden).build())
					.addHeaders(HeaderValueOption.newBuilder()
						.setHeader(HeaderValue.newBuilder()
							.setKey("X-Auth-Handler")
							.setValue("am.ik.envoy.AuthorizationServiceImpl.check")
							.build())
						.build())
					.build())
				.build());
		}
		responseObserver.onCompleted();
	}

}
