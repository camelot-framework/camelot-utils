package ru.yandex.qatools.camelot.client;

import javax.ws.rs.client.Client;
import java.net.URI;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface EndpointOperation<T> {

    T runOn(Client client, URI uri);
}
