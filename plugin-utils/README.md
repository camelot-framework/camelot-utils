# Camelot Plugin Utils

Contains a ```GraphiteReportProcessor``` and a ```PluginRepoUtils``` classes.
First is a pre-built processor that aims to simplify sending data from any
of yor plugins to a specified [Graphite] instance. Second is just an utility
class that provides a couple of useful methods for working with the camelot 
plugin states repository, like building a 'correlation keys to state objects' 
map et cetera.

To use ```plugins-utils``` add the following dependency to your pom

```xml
<dependency>
   <groupId>ru.yandex.qatools.camelot.utils</groupId>
   <artifactId>plugin-utils</artifactId>
   <version>${camelot-utils.version}</version>
</dependency>
```

### GraphiteReportProcessor

This plugin offers a simple solution for sending arbitrary values from your 
camelot application to a Graphite imstance. That's what you have to do:

1. specify properties ```graphite.host``` and ```graphite.port``` in your 
   ```camelot.properties``` file;
1. add ```ru.yandex.qatools.camelot.plugin.GraphiteReportProcessor```
to your ```camelot.xml```;
1. add an automatically-injected field ```@MainInput EventProducer input```
   to the plugin/resource you gonna send graphite values from;
1. send values like this:
   
   ```java
   import static ru.yandex.qatools.camelot.plugin.GraphiteValue.gValue;

   input.produce(gValue("metric.name", 100500, System.currentTimeMillis()));
   ```

1. Badum tsssss!

### PluginRepoUtils

An utility class that can be useful when working with the camelot plugin
states storage. For example when you're building a front-end interface for 
your camelot app, you may want to request all the states for one of the plugins
to draw them on the plugin's dashboard. This task can be easily solved with this
utility class just by following these steps:

1. Add the following method to any of your resource classes:
   
   ```java
   @GET
   @Path("/map/{pluginId}")
   @Produces({MediaType.APPLICATION_JSON})
   public Map<String, Object> getPluginStates(@PathParam("pluginId") String pluginId) {
       return new PluginRepoUtils(plugins).getPluginStatesMap(pluginId, false);
   }
   ```

2. Request all the plugin states in your front-end js code like that:
   
   ```js
   angular.module('my.camelot.application.plugins.MyPlugin', ['camelotUtil'])
       .controller('AppCtrl', ['$scope', '$http', 'subscribe', 'baseUrl', 'pluginId', 
               function ($scope, $http, subscribe, baseUrl, pluginId) {
       'use strict';

       $http.get(baseUrl + '/aero-monitoring/map/' + pluginId)
           .success(function (states) {
               // 'states' is a on object with correlation keys as keys 
               // and corresponding plugins states as values.
           });
   }]);
   ```

Other methods for obtaining a list of states or to get only locally stored states
are also available, for more info check the ```PluginRepoUtils``` class API. 
Note that requesting all hazelcast keys from different nodes significantly loads 
hazelcast processor. That's why it's recommended to use the provided ```subscribe```
function and get updates instead of reloading the whole states map/list by cron.

More info on how to write front-end widgets and dashboards for your camelot app
can be found on the [camelot wiki pages](https://github.com/camelot-framework/camelot/wiki/Plugins-widgets-and-dashboards).
