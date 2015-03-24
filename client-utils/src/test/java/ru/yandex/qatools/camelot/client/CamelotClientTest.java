package ru.yandex.qatools.camelot.client;

import org.junit.Test;
import ru.yandex.qatools.camelot.client.beans.Camelot;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CamelotClientTest {

    public static final List<Camelot.Endpoint> EMPTY_LIST
            = Collections.emptyList();

    private static final EndpointOperation<String> RETURN_URI
            = new EndpointOperation<String>() {
        @Override
        public String runOn(Client client, URI uri) {
            return uri.toString();
        }
    };

    @Test
    public void testClientWithEndpoint() throws Exception {
        CamelotClient client = mock(CamelotClient.class);
        String url = "http://endpoint.com";
        when(client.endpoints()).thenReturn(newEndpointsArray(url));
        assertThat(client.findEndpointAndRun(RETURN_URI), equalTo(url));
    }

    @Test(expected = CamelotClientException.class)
    public void testClientWithNoEndpoints() throws Exception {
        CamelotClient client = mock(CamelotClient.class);
        when(client.endpoints()).thenReturn(EMPTY_LIST);
        client.findEndpointAndRun(null);
    }

    private List<Camelot.Endpoint> newEndpointsArray(String url) {
        List<Camelot.Endpoint> result = new ArrayList<>();
        result.add(newEndpoint(url));
        return result;
    }

    private Camelot.Endpoint newEndpoint(String url) {
        Camelot.Endpoint endpointBean = new Camelot.Endpoint();
        endpointBean.setUrl(url);
        return endpointBean;
    }
}
