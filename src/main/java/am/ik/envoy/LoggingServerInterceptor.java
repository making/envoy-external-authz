package am.ik.envoy;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

@GlobalServerInterceptor
@Component
public class LoggingServerInterceptor implements ServerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(LoggingServerInterceptor.class);

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {

		if (logger.isDebugEnabled()) {
			logger.debug("Intercepting call: {}", call.getMethodDescriptor().getFullMethodName());
		}

		ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
			@Override
			public void sendMessage(RespT message) {
				logger.debug("Sending response message");
				super.sendMessage(message);
			}

			@Override
			public void close(Status status, Metadata trailers) {
				logger.debug("Closing call with status: {}", status);
				super.close(status, trailers);
			}
		};

		return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
				next.startCall(wrappedCall, headers)) {

			@Override
			public void onMessage(ReqT message) {
				logger.debug("Received request message");
				try {
					super.onMessage(message);
				}
				catch (Exception e) {
					logger.error("Error processing message", e);
					throw e;
				}
			}

			@Override
			public void onHalfClose() {
				logger.debug("Client finished sending messages");
				try {
					super.onHalfClose();
				}
				catch (Exception e) {
					logger.error("Error on half close", e);
					throw e;
				}
			}
		};
	}

}