package ru.yandex.qatools.camelot.client;

import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.beanloader.BeanLoader;
import ru.yandex.qatools.camelot.client.beans.Camelot;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static javax.ws.rs.client.ClientBuilder.newClient;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static ru.qatools.beanloader.BeanLoader.load;
import static ru.qatools.beanloader.BeanLoaderStrategies.file;
import static ru.qatools.beanloader.BeanLoaderStrategies.resource;
import static ru.yandex.qatools.camelot.client.CamelotProperties.PROPERTIES;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 */
public class CamelotClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelotClient.class);

    private final BeanLoader<Camelot> endpointsLoader = load(Camelot.class)
            .from(resource(PROPERTIES.getEndpointsResource()))
            .from(file(PROPERTIES.getEndpointsFile(), true));

    private Client client;

    protected List<Camelot.Endpoint> endpoints() {
        Camelot camelotConfig = endpointsLoader.getBean();
        if (camelotConfig != null) {
            return new ArrayList<>(camelotConfig.getEndpoints());
        }
        return Collections.emptyList();
    }

    private Client getClient() {
        if (client == null) {
            client = newClient(new ClientConfig()
                        .property(CONNECT_TIMEOUT, PROPERTIES.getClientConnectTimeout())
                        .property(READ_TIMEOUT, PROPERTIES.getClientReadTimeout()));
        }
        return client;
    }

    protected final <T> T findEndpointAndRun(EndpointOperation<T> operation)
            throws CamelotClientException {
        List<Camelot.Endpoint> endpoints = endpoints();
        while (endpoints.size() > 0) {
            int endpointIndex = new Random().nextInt(endpoints.size());
            Camelot.Endpoint endpoint = endpoints.get(endpointIndex);
            try {
                return operation.runOn(getClient(), new URI(endpoint.getUrl()));
            } catch (Exception e) {
                LOGGER.error(String.format("Can't send message to %s: %s",
                        endpoint.getUrl(), e.getMessage()));
                endpoints.remove(endpointIndex);
            }
        }
        throw new CamelotClientException("Unable to find a working endpoint!");
    }
}
