package ru.yandex.qatools.camelot.plugin;

import com.codahale.metrics.graphite.Graphite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.ConfigValue;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.api.annotations.Processor;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Ilya Sadykov smecsia@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@Filter(instanceOf = GraphiteValue.class)
public class GraphiteReportProcessor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ConfigValue("graphite.host")
    private String host;

    @ConfigValue("graphite.port")
    private int port;

    @Processor
    public void process(GraphiteValue event) {
        if (isBlank(host)) {
            return;
        }

        logger.info(format("Sending %s to graphite at %s:%d", event, host, port));
        try {
            Graphite graphite = new Graphite(new InetSocketAddress(host, port));
            graphite.connect();
            graphite.send(event.getName(), event.getValue(), event.getTimestamp());
            graphite.close();
        } catch (IOException e) {
            logger.error("Failed to stream value to graphite:", e);
        }
    }
}
