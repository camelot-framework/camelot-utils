package ru.yandex.qatools.camelot.client;

import ru.yandex.qatools.properties.PropertyLoader;
import ru.yandex.qatools.properties.annotations.Property;
import ru.yandex.qatools.properties.annotations.Resource;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@Resource.Classpath("camelot.properties")
public class CamelotProperties {

    public static final CamelotProperties PROPERTIES = new CamelotProperties();

    @Property("camelot.client.connect.timeout")
    private int clientConnectTimeout = 30000;

    @Property("camelot.client.read.timeout")
    private int clientReadTimeout = 30000;

    @Property("camelot.client.endpoints.resource")
    private String endpointsResource = "camelot-endpoints.xml";

    @Property("camelot.client.endpoints.file")
    private String endpointsFile = "/etc/camelot/endpoints.xml";

    private CamelotProperties() {
        PropertyLoader.populate(this);
    }

    public int getClientConnectTimeout() {
        return clientConnectTimeout;
    }

    public int getClientReadTimeout() {
        return clientReadTimeout;
    }

    public String getEndpointsResource() {
        return endpointsResource;
    }

    public String getEndpointsFile() {
        return endpointsFile;
    }
}
