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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl extends AuthorizationGrpc.AuthorizationImplBase {

	private final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

	@Override
	public void check(CheckRequest request, StreamObserver<CheckResponse> responseObserver) {
		AttributeContext.HttpRequest req = request.getAttributes().getRequest().getHttp();
		if (logger.isInfoEnabled()) {
			logger.info("Check request host={} path={} id={}", req.getHost(), req.getPath(), req.getId());
		}
		System.out.println(request);
		// req.getHeaderMap().getExtension()
		String user = req.getHeadersMap().get("x-user");
		if ("demo".equalsIgnoreCase(user)) {
			responseObserver.onNext(CheckResponse.newBuilder()
				.setStatus(Status.newBuilder().setCode(Code.OK_VALUE).build())
				.setOkResponse(OkHttpResponse.newBuilder()
					.addHeaders(HeaderValueOption.newBuilder()
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
					.setStatus(HttpStatus.newBuilder().setCode(StatusCode.Unauthorized).build())
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
