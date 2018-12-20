package fi.nls.paikkatietoikkuna.terrainprofile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeoutException;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

/**
 * HystrixCommand that sends a GetCoverage request to the WCS service via HTTP
 * and reads the response to a byte array.
 *
 * In case the service responds with a 504 status code this
 * code sleeps for a bit and then tries again up to MAX_RETRIES times
 */
public class CommandGetCoverage extends HystrixCommand<byte[]> {

    private static final String GROUP_KEY = "oskari.terrainprofile";
    private static final String COMMAND_NAME = "getCoverage";
    private static final int MAX_RETRIES = 5;
    private static final int SLEEP_BETWEEN_RETRY_MS = 100;

    private final String request;

    public CommandGetCoverage(String request) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(GROUP_KEY))
                .andCommandKey(HystrixCommandKey.Factory.asKey(COMMAND_NAME))
                .andThreadPoolPropertiesDefaults(
                        HystrixThreadPoolProperties.Setter()
                        .withCoreSize(PropertyUtil.getOptional(GROUP_KEY + ".job.pool.size", 4))
                        .withMaxQueueSize(PropertyUtil.getOptional(GROUP_KEY + ".job.pool.limit", 100))
                        .withQueueSizeRejectionThreshold(PropertyUtil.getOptional(GROUP_KEY + ".job.pool.queue", 100)))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(PropertyUtil.getOptional(GROUP_KEY + ".job.timeoutms", 15000))
                        .withCircuitBreakerRequestVolumeThreshold(PropertyUtil.getOptional(GROUP_KEY + ".failrequests", 10))
                        .withCircuitBreakerSleepWindowInMilliseconds(PropertyUtil.getOptional(GROUP_KEY + ".sleepwindow", 10000)))
                );
        this.request = request;
    }

    @Override
    protected byte[] run() throws TimeoutException, IOException, Exception {
        HttpURLConnection conn = IOHelper.getConnection(request);
        int sc = conn.getResponseCode();

        int tryCounter = 0;
        while (sc == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
            if (++tryCounter == MAX_RETRIES) {
                throw new TimeoutException();
            }
            Thread.sleep(SLEEP_BETWEEN_RETRY_MS);
            conn = IOHelper.getConnection(request);
            sc = conn.getResponseCode();
        }

        if (sc == HttpURLConnection.HTTP_OK) {
            return IOHelper.readBytes(conn);
        }

        throw new Exception("Unexpected response to GetCoverage");
    }

}
