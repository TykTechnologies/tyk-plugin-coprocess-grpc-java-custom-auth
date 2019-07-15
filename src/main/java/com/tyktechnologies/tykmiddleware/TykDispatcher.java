package com.tyktechnologies.tykmiddleware;

import coprocess.DispatcherGrpc;
import coprocess.CoprocessObject;
import coprocess.CoprocessSessionState;
import coprocess.CoprocessReturnOverrides;

public class TykDispatcher extends DispatcherGrpc.DispatcherImplBase {

    final String FOOBAR = "foobar";

    @Override
    public void dispatch(CoprocessObject.Object request,
            io.grpc.stub.StreamObserver<CoprocessObject.Object> responseObserver) {

        System.out.println("*** Incoming Request ***");
        System.out.println("Hook name: " + request.getHookName());

        if (request.getHookName().equals("MyAuthCheck")) {    
            final CoprocessObject.Object modifiedRequest = MyAuthHook(request);
            responseObserver.onNext(modifiedRequest);
            System.out.println("*** Auth Check Complete ***");
        }

        if (request.getHookName().equals("MyPreHook")) {    
            final CoprocessObject.Object modifiedRequest = MyPreHook(request);
            responseObserver.onNext(modifiedRequest);
            System.out.println("*** PRE HOOK Complete ***");
        }

        if (request.getHookName().equals("session_to_jwt")) {    
            final CoprocessObject.Object modifiedRequest = SessionToJWT(request);
            responseObserver.onNext(modifiedRequest);
            System.out.println("*** POST Session to JWT HOOK Complete ***");
        }
        
        responseObserver.onCompleted();
    }

    /** Executed Before Authentication */
    CoprocessObject.Object MyPreHook(CoprocessObject.Object request) {
        CoprocessObject.Object.Builder builder = request.toBuilder();

        
        builder.getRequestBuilder().putSetHeaders("customheader", "customvalue");

        return builder.build();
    }

    /** Executed Right before reverse proxy */
    CoprocessObject.Object SessionToJWT(CoprocessObject.Object request) {
        CoprocessObject.Object.Builder builder = request.toBuilder();

        String authHeader = request.getRequest().getHeadersOrDefault("Authorization", "");
        builder.getRequestBuilder().putSetHeaders("Authorization", "JWT " + authHeader);

        return builder.build();
    }

    /** Successful Authentication is cached By Tyk using its ID Extractor */
    CoprocessObject.Object MyAuthHook(CoprocessObject.Object request) {
        String authHeader = request.getRequest().getHeadersOrDefault("Authorization", "");
        if(!authHeader.equals(FOOBAR)) {
            CoprocessObject.Object.Builder builder = request.toBuilder();
            CoprocessReturnOverrides.ReturnOverrides retOverrides = CoprocessReturnOverrides.ReturnOverrides.newBuilder()
            .setResponseCode(403)
            .setResponseError("Not authorized")
            .build();

            builder.getRequestBuilder().setReturnOverrides(retOverrides);
            return builder.build();
        }

        final long expiryTime = (System.currentTimeMillis() / 1000) + 5;

        CoprocessSessionState.SessionState session = CoprocessSessionState.SessionState.newBuilder()
        .setRate(1000.0)
        .setPer(1.0)
        .setIdExtractorDeadline(expiryTime)
        .build();

        CoprocessObject.Object.Builder builder = request.toBuilder();
        // Mandatory fields
        builder.putMetadata("token", FOOBAR);  // Used for downstream chains
        builder.setSession(session);

        return builder.build();
    }
}
