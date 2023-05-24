package org.gft.adapters.backend;

import org.apache.streampipes.extensions.api.connect.IAdapterPipeline;
import org.apache.streampipes.extensions.api.connect.IFormat;
import org.apache.streampipes.extensions.api.connect.IParser;
import org.apache.streampipes.extensions.api.connect.exception.ParseException;
import org.apache.streampipes.extensions.management.connect.SendToPipeline;
import org.apache.streampipes.extensions.management.connect.adapter.model.generic.Protocol;
import org.apache.streampipes.model.connect.grounding.ProtocolDescription;
import org.apache.streampipes.model.connect.guess.GuessSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

// This is a Streampipes built in Class used to manage the waiting time between two consecutive poll
public abstract class BackendPullProtocol extends Protocol {

    private ScheduledExecutorService scheduler;
    private final Logger logger = LoggerFactory.getLogger(BackendPullProtocol.class);

    private long interval;

    public BackendPullProtocol() {
    }

    public BackendPullProtocol(IParser parser, IFormat format, long interval) {
        super(parser, format);
        this.interval = interval;
    }

    @Override
    public void run(IAdapterPipeline adapterPipeline) {
        final Runnable errorThread = () -> executeProtocolLogic(adapterPipeline);

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(errorThread, 0, TimeUnit.MILLISECONDS);

    }

    private void executeProtocolLogic(IAdapterPipeline adapterPipeline) {
        final Runnable task = () -> {

            format.reset();
            SendToPipeline stk = new SendToPipeline(format, adapterPipeline);
            try {
                InputStream data = getDataFromEndpoint();
                if(data != null) {
                    parser.parse(data, stk);
                } else {
                    logger.warn("Could not receive data from Endpoint. Try again in " + this.interval + " seconds.");
                }
            } catch (ParseException e) {
                logger.error("Error while parsing: " + e.getMessage());
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }

        };

        scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(task, 1, this.interval, TimeUnit.SECONDS);
        try {
            handle.get();
        } catch (ExecutionException | InterruptedException e ) {
            logger.error("Error", e);
        }
    }

    @Override
    public void stop() {
        scheduler.shutdownNow();
    }

    // get constructor parameters
    public abstract Protocol getInstance(ProtocolDescription protocolDescription, IParser parser, IFormat format);

    // retrieve the schema of the payload
    public abstract GuessSchema getGuessSchema() throws ParseException;

    public abstract List<Map<String, Object>> getNElements(int n) throws ParseException;

    abstract InputStream getDataFromEndpoint() throws ParseException, java.text.ParseException;

    public abstract String getId();
}