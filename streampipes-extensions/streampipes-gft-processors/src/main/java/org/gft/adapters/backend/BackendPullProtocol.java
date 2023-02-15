package org.gft.adapters.backend;

import org.apache.streampipes.connect.SendToPipeline;
import org.apache.streampipes.connect.adapter.model.generic.Protocol;
import org.apache.streampipes.connect.api.IAdapterPipeline;
import org.apache.streampipes.connect.api.IFormat;
import org.apache.streampipes.connect.api.IParser;
import org.apache.streampipes.connect.api.exception.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.*;

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

    abstract InputStream getDataFromEndpoint() throws ParseException, java.text.ParseException;
}