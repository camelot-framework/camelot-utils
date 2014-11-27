package ru.yandex.qatools.camelot.plugin;

import com.codahale.metrics.graphite.Graphite;
import mockit.Mock;
import mockit.MockUp;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.test.*;

import java.net.InetSocketAddress;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.camelot.plugin.GraphiteValue.gValue;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@RunWith(CamelotTestRunner.class)
@UseProperties("camelot.properties")
public class GraphiteReportProcessorTest {

    @Helper
    protected TestHelper helper;

    @PluginMock
    private GraphiteReportProcessor graphiteProcessor;

    @Before
    public void setUp() throws Exception {
        new MockUp<Graphite>() {
            @Mock void $init(InetSocketAddress address) {}
            @Mock void connect() {}
            @Mock void close() {}
            @Mock void send(String name, String value, long timestamp) {}
        };
    }

    @Test
    public void testProcess() throws Exception {
        GraphiteValue gValue = gValue("name", 1, 69);
        helper.sendTo(GraphiteReportProcessor.class, gValue);
        verify(graphiteProcessor, timeout(5000)).process(
                argThat(Matchers.<GraphiteValue>allOf(
                        hasProperty("name", equalTo(gValue.getName())),
                        hasProperty("value", equalTo(gValue.getValue())),
                        hasProperty("timestamp", equalTo(gValue.getTimestamp())))));
    }
}
