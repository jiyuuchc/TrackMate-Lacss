# TrackMate-Lacss

A plugin of a plugin. 

This software adds a new cell detector to [TrackMate](https://imagej.net/plugins/trackmate/index), a cell/object tracker plugin for [ImageJ](https://imagej.net/). The detector uses a pretrained [deep-learning model](https://github.com/jiyuuchc/lacss) to detect and segment cells.

### Installation

1. Install [Lacss](https://github.com/jiyuuchc/lacss) according to its [documentation](https://jiyuuchc.github.io/lacss/install/)
2. Install plugin
   - For FIJI user: install/update via the built-in updater, i.e. ```Help/Update/Manage update sites```.
   - For vanila ImageJ user: Copy the [Jar file](https://github.com/jiyuuchc/TrackMate-Lacss/releases/) into ImageJ's plugin folder.

### How does it work

The plugin is a thin Java wrapper around the Lacss python module, which runs as a separate process. The Java code communicate with the Python process via either http (remote server) or [anonymous pipe](https://en.wikipedia.org/wiki/Anonymous_pipe) (local server), by sending/receiving messages encoded in [protobuf](https://protobuf.dev/).
