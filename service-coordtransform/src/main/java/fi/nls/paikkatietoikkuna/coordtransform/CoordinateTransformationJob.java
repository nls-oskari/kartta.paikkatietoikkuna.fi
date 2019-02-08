package fi.nls.paikkatietoikkuna.coordtransform;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

public class CoordinateTransformationJob {

    private static final String PENDING = "pending";

    private DeferredResult<CoordinatesPayload> subscriber;
    private DeferredResult.DeferredResultHandler subscriberResultHandler;
    private Object result;
    private TransformParams params;
    private String id;

    public CoordinateTransformationJob(TransformParams params) {
        this.params = params;
        this.id = UUID.randomUUID().toString();
        this.result = PENDING;
    }

    public String getId() {
        return id;
    }

    public Object getResult() {
        return result;
    }

    public TransformParams getParams() {
        return params;
    }

    public void subscibe(DeferredResult<CoordinatesPayload> subscriber, DeferredResult.DeferredResultHandler handler) {
        this.subscriber = subscriber;
        this.subscriberResultHandler = handler;
    }

    public boolean isCompleted() {
        return result == null || !result.equals(PENDING);
    }

    public void complete(Object finishedWithResult) {
        result = finishedWithResult;
        if (subscriber == null) {
            return;
        }
        subscriber.setResultHandler(subscriberResultHandler);
        if (finishedWithResult instanceof Exception) {
            subscriber.setErrorResult(finishedWithResult);
        } else {
            subscriber.setResult((CoordinatesPayload) finishedWithResult);
        }
    }
}
