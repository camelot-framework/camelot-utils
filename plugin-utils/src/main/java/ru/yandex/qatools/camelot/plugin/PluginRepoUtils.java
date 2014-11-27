package ru.yandex.qatools.camelot.plugin;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.closure.Closure;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.PluginsInterop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.closure;
import static ch.lambdaj.Lambda.of;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
@SuppressWarnings("UnusedDeclaration")
public class PluginRepoUtils {

    private final PluginsInterop plugins;

    public PluginRepoUtils(PluginsInterop plugins) {
        this.plugins = plugins;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPluginStatesMap(Class pluginClass, boolean localKeysOnly) {
        return getPluginStatesMap(plugins.repo(pluginClass), localKeysOnly);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPluginStatesMap(String pluginId, boolean localKeysOnly) {
        return getPluginStatesMap(plugins.repo(pluginId), localKeysOnly);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getPluginStatesMap(AggregatorRepository<T> repo, boolean localKeysOnly) {
        Set<String> keys;
        if (localKeysOnly) {
            keys = repo.localKeys();
        } else {
            keys = repo.keys();
        }
        HashMap<String, T> result = new HashMap<>(keys.size());
        for (String key : keys) {
            result.put(key, repo.get(key));
        }
        return result;
    }

    public Object getPluginState(Class pluginClass, String correlationKey) {
        return plugins.repo(pluginClass).get(correlationKey);
    }

    public Object getPluginState(String pluginId, String correlationKey) {
        return plugins.repo(pluginId).get(correlationKey);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getPluginStatesList(Class pluginClass, boolean localKeysOnly) {
        return getPluginStatesList(plugins.repo(pluginClass), localKeysOnly);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getPluginStatesList(String pluginId, boolean localKeysOnly) {
        return getPluginStatesList(plugins.repo(pluginId), localKeysOnly);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getPluginStatesList(AggregatorRepository<T> repo, boolean localKeysOnly) {
        Closure closure = closure(); of(repo).get(Lambda.var(String.class));
        return (List<T>) closure.each((localKeysOnly ? repo.localKeys() : repo.keys()));
    }
}
