# TrackMate-Lacss

A plugin of a plugin. 

This software adds a new cell detector to [TrackMate](https://imagej.net/plugins/trackmate/index), a cell/object tracker plugin for [ImageJ](https://imagej.net/). The detector uses a pretrained [deep-learning model](https://github.com/jiyuuchc/lacss) to detect and segment cells.

### Installation

1. Install [Lacss](https://github.com/jiyuuchc/lacss) according to its [documentation](https://jiyuuchc.github.io/lacss/install/)
2. Install Trackmate-Lacss plugin via FIJI's the built-in updater, i.e. ```Help/Update/Manage update sites```.

| <img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_3.png" height="350"> | <img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_4.png" height="350"> |

### How does it work

Lacss runs as a GRPC server at a TCP port (default: 50051). This small plugin communicates with the server by sending/receiving messages encoded in [protobuf](https://protobuf.dev/).

Step 1: Start the Lacss server

```
python -m lacss.deploy.remote_server <model_file>
```

You can find the URLs of several pre-trained model files by running:
```
python -m lacss.deploy.remote_server --help
```

Step 2: In FIJI, start TrackMate from Menu: Plugins/Tracking/TrackMate

Step 3: At the detector selection page, select "Lacss detector" from the dropdown menu.

Step 4: A the next configuration page, provide the correct server address.

| <img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_1.png" height="350"> | <img src="https://github.com/jiyuuchc/Trackmate-Lacss/raw/main/.github/images/trackmate_img_2.png" height="350"> |
