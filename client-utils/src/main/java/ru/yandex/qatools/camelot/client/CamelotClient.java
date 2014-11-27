package ru.yandex.qatools.camelot.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.client.beans.Camelot;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO add tests
/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class CamelotClient {

    private static final String ENDPOINTS_FILE_NAME = "endpoints.xml";

    private static final String ENDPOINTS_DIR = "/etc/camelot/";

    private static final String ENDPOINTS_RESOURCE = "camelot-endpoints.xml";

    private static final Object LOCK = new Object();

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelotClient.class);

    private static volatile Camelot camelotConfig = new Camelot();

    private static Unmarshaller unmarshaller = null;

    private Client client;

    static {
        if (tryToCreateUnmarshaller() && !tryToLoadEndpointsFromClassPath()) {
            tryToLoadEndpointsFromFile();
            initEndpointsFileWatcher();
        }
    }

    private static boolean tryToCreateUnmarshaller() {
        try {
            unmarshaller = JAXBContext.newInstance(Camelot.class).createUnmarshaller();
            return true;
        } catch (JAXBException e) {
            LOGGER.warn("Can't create unmarshaller for " + Camelot.class.getName(), e);
        }
        return false;
    }

    private static boolean tryToLoadEndpointsFromClassPath() {
        try (InputStream is = CamelotClient.class.getClassLoader().getResourceAsStream(ENDPOINTS_RESOURCE)) {
            if (is != null) {
                synchronized (LOCK) {
                    //noinspection unchecked
                    camelotConfig = (Camelot) unmarshaller.unmarshal(is);
                }
                return true;
            }
        } catch (JAXBException | IOException ignored) {
        }
        return false;
    }

    private static boolean tryToLoadEndpointsFromFile() {
        File file = new File(ENDPOINTS_DIR + ENDPOINTS_FILE_NAME);
        if (file.exists()) {
            try {
                synchronized (LOCK) {
                    //noinspection unchecked
                    camelotConfig = (Camelot) unmarshaller.unmarshal(file);
                    return true;
                }
            } catch (JAXBException ignored) {
            }
        }
        return false;
    }

    private static boolean initEndpointsFileWatcher() {
        final AtomicBoolean result = new AtomicBoolean(true);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Path endpoints = Paths.get(ENDPOINTS_DIR);
                try (WatchService service = FileSystems.getDefault().newWatchService()) {
                    endpoints.register(service,
                            StandardWatchEventKinds.ENTRY_MODIFY
                    );

                    LOGGER.info("Watching for changes in directory " + ENDPOINTS_DIR);
                    //noinspection all
                    while (true) {
                        try {
                            WatchKey key = service.take();
                            for (WatchEvent event : key.pollEvents()) {
                                Path path = (Path) event.context();
                                if (path.endsWith(ENDPOINTS_FILE_NAME)) {
                                    LOGGER.info("Endpoints file changed");
                                    tryToLoadEndpointsFromFile();
                                }
                            }
                            key.reset();
                        } catch (InterruptedException ignored) {
                        }
                    }

                } catch (IOException e) {
                    LOGGER.error("Can't create watch service for directory " + ENDPOINTS_DIR, e);
                    result.getAndSet(false);
                }
            }
        });
        executor.shutdown();
        return result.get();
    }

    private Client getClient() {
        if (client == null) {
            client = ClientBuilder.newClient(new ClientConfig()
                            .property(ClientProperties.CONNECT_TIMEOUT, 5000)
                            .property(ClientProperties.READ_TIMEOUT, 5000)
            );
        }
        return client;
    }

    protected final <T> T findEndpointAndRun(EndpointOperation<T> operation)
            throws CamelotClientException {
        List<Camelot.Endpoint> endpoints = new ArrayList<>(camelotConfig.getEndpoints());
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
