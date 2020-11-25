package org.pragmaticindustries.cockpit.plc.pool2;

import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.metadata.PlcConnectionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Wrapper around a PlcConnection which interacts with the {@link CachedDriverManager}.
 *
 * @author julian
 * Created by julian on 24.02.20
 */
public class CachedPlcConnection implements PlcConnection {

    private static final Logger logger = LoggerFactory.getLogger(CachedPlcConnection.class);

    private final CachedDriverManager parent;
    private final PlcConnection activeConnection;
    private volatile boolean closed = false;

    public CachedPlcConnection(CachedDriverManager parent, PlcConnection activeConnection) {
        this.parent = parent;
        this.activeConnection = activeConnection;
    }

    @Override
    public void connect() throws PlcConnectionException {
        // Do nothing
        logger.warn(".connect() is called on a Cached Connection. This has no effect but should not happen.");
    }

    @Override
    public boolean isConnected() {
        if (closed) {
            return false;
        } else {
            return activeConnection.isConnected();
        }
    }

    /**
     * Executes the Request.
     */
    public CompletableFuture<? extends PlcReadResponse> execute(PlcReadRequest request) {
        logger.trace("Trying to executing Request {}", request);
        if (closed) {
            throw new IllegalStateException("Trying to execute a Request on a closed Connection!");
        }
        try {
            logger.trace("Executing Request {}", request);
            final CompletableFuture<? extends PlcReadResponse> responseFuture = request.execute();
            // The following code handles the case, that a read fails (which is handled async and thus not really connected
            // to the connection, yet
            // Thus, we register our own listener who gets the notification and reports the connection as broken
            return responseFuture.handleAsync(new BiFunction<PlcReadResponse, Throwable, PlcReadResponse>() {
                @Override
                public PlcReadResponse apply(PlcReadResponse plcReadResponse, Throwable throwable) {
                    if (throwable != null) {
                        // Do something here...
                        logger.warn("Request finished with exception. Reporting Connection as Broken", throwable);
                        closeConnectionExceptionally(null);
                    }
                    return plcReadResponse;
                }
            });
        } catch (Exception e) {
            return closeConnectionExceptionally(e);
        }
    }

    private CompletableFuture<? extends PlcReadResponse> closeConnectionExceptionally(Exception e) {
        // First, close this connection and allow no further operations on it!
        this.closed = true;
        // Return the Connection as invalid
        parent.handleBrokenConnection();
        // Throw Exception
        throw new PlcRuntimeException("Unable to finish Request!", e);
    }

    PlcConnection getActiveConnection() {
        return this.activeConnection;
    }

    @Override
    public synchronized void close() throws Exception {
        logger.debug("Closing cached connection and returning borrowed connection to pool.");
        // First, close this connection and allow no further operations on it!
        this.closed = true;
        // Return the Connection
        parent.returnConnection(activeConnection);
    }

    @Override
    public PlcConnectionMetadata getMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Void> ping() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlcReadRequest.Builder readRequestBuilder() {
        if (closed) {
            throw new IllegalStateException("Trying to build a Request on a closed Connection!");
        }
        return new CachedReadRequestBuilder(this, this.getActiveConnection().readRequestBuilder());
    }

    @Override
    public PlcWriteRequest.Builder writeRequestBuilder() {
        return null;
    }

    @Override
    public PlcSubscriptionRequest.Builder subscriptionRequestBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlcUnsubscriptionRequest.Builder unsubscriptionRequestBuilder() {
        throw new UnsupportedOperationException();
    }
}