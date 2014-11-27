# Camelot Utils

This project contains some tools that may be helpful when building
[Camelot]-based applications.

### Client Utils

Contains a ```CamelotClient``` class that aims to help sending messages
to a corresponding camelot-powered service somewhere via HTTP.

One of the possible models for building a camelot application 
is to develop the nedded plugins (i.e aggregators, processors) 
and resources, launch a camelot instance with them somewhere 
on a server and then supply it with data in form of xml/json-messages
sent to one of the resources via HTTP.

Once messages arrive, the resource class can reject some of them based
on their parameters etc. and redirect the rest to the main camelot 
input queue, where from they will be consumed and handled by plugins.

```CamelotClient``` provides functionality for easy sending of these messages, 
you will only have to specify which ones.

For usage tutorial and tips please proceed to the [client-utils module docs].

### Plugin Utils

Contains a ```GraphiteReportProcessor``` and a ```PluginRepoUtils``` classes.
First is a pre-built processor that aims to simplify sending data from any
of yor plugins to a specified [Graphite] instance. Second is just an utility
class that provides a couple of useful methods for working with the camelot 
plugin states repository, like building a 'correlation keys to state objects' 
map et cetera.

For usage tutorial please proceed to the [plugin-utils module docs].

[camelot]: https://github.com/camelot-framework/camelot
[graphite]: http://graphite.wikidot.com/
[client-utils module docs]: https://github.com/camelot-framework/camelot-utils/blob/master/client-utils/README.md
[plugin-utils module docs]: https://github.com/camelot-framework/camelot-utils/blob/master/plugin-utils/README.md
