# TrackMate-Lacss

A plugin of a plugin. 

This software adds a new cell detector to [TrackMate](https://imagej.net/plugins/trackmate/index), a cell/object tracker plugin for [FIJI/ImageJ](https://imagej.net/software/fiji/). 

The detector uses a pretrained deep-learning model [Lacss](https://github.com/jiyuuchc/lacss) to find and segment cells.

### Installation

1. Install [Lacss](https://github.com/jiyuuchc/lacss) according to its [documentation](https://jiyuuchc.github.io/lacss/install/)
2. Install Trackmate-Lacss plugin via FIJI's the built-in updater, i.e. ```Help/Update/Manage update sites```.

| <img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_3.png" height="350"> | <img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_4.png" height="350"> |

### How does it work

Lacss runs as an [GRPC](https://grpc.io/) server listening at a TCP port (default: 50051). This simple Java plugin communicates with the server by sending/receiving messages encoded in [protobuf](https://protobuf.dev/).

### Usage
**Step 1:** Start the Lacss server

```
python -m lacss.deploy.remote_server --modelpath=<model_file>
```

*Run the command without argments to get a list of download URLs for model files*

```
python -m lacss.deploy.remote_server
```

**Step 2:** Start TrackMate in FIJI. At the detector selection page, select "Lacss detector" from the dropdown menu.

<img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_1.png" height="350">

**Step 3:** At the next configuration page, provide the correct server address.

<img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_2.png" height="350">
