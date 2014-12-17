package ru.yandex.qatools.camelot.client;

import ru.yandex.qatools.properties.PropertyLoader;
import ru.yandex.qatools.properties.annotations.Property;
import ru.yandex.qatools.properties.annotations.Resource;

/**
 * @author innokenty
 */
@Resource.Classpath("camelot.properties")
public class CamelotProperties {

    @Property("camelot.client.connect.timeout")
    private int clientConnectTimeout = 30000;

    @Property("camelot.client.read.timeout")
    private int clientReadTimeout = 30000;

    public CamelotProperties() {
        PropertyLoader.populate(this);
    }

    public int getClientConnectTimeout() {
        return clientConnectTimeout;
    }

    public int getClientReadTimeout() {
        return clientReadTimeout;
    }
}
