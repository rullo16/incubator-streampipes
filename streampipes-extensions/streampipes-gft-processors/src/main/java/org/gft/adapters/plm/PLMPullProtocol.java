package org.gft.adapters.plm;

import org.apache.streampipes.extensions.api.connect.IAdapterPipeline;
import org.apache.streampipes.extensions.api.connect.IFormat;
import org.apache.streampipes.extensions.api.connect.IParser;
import org.apache.streampipes.extensions.api.connect.exception.ParseException;
import org.apache.streampipes.extensions.management.connect.SendToPipeline;
import org.apache.streampipes.extensions.management.connect.adapter.model.generic.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.*;

public abstract class PLMPullProtocol extends Protocol {

    private ScheduledExecutorService scheduler;

    private final Logger logger = LoggerFactory.getLogger(PLMPullProtocol.class);

    private long interval;


    public PLMPullProtocol() {
    }

    public PLMPullProtocol(IParser parser, IFormat format, long interval) {
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
                    logger.warn("Could not receive data from Endpoint. Try again in " + interval + " seconds.");
                }
            } catch (ParseException e) {
                logger.error("Error while parsing: " + e.getMessage());
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }


        };

        scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(task, 1, interval, TimeUnit.SECONDS);
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