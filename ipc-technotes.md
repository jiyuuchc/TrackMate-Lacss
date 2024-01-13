## Trackmate-Lacss IPC technote

### Motivation

Deep learning (DL) algorithms excel in many image-processing tasks. However, to bring DL algorithm into ImageJ/Fiji, one typically needs to implement some type of inter-process communication (IPC). This is because DL code is typically written in Python, as Java's toolchain support for DL remains poor. In addition, DL algorithms can sometimes be resource-intensive and are best deployed on a separate, dedicated server with dedicated hardware (e.g. TPU/GPU), instead of the laptop of an end-user of ImageJ/Fiji.

Here we summarize the IPC implementation choices of (and lessons learned from) [Trackmate-Lacss](https://github.com/jiyuuchc/TrackMate-Lacss/), which we recently wrote to add a DL cell detector, namely, [Lacss](https://github.com/jiyuuchc/lacss) to the popular ImageJ plugin [Trackmate](https://imagej.net/plugins/trackmate/index).

### In a nutshell

Trackmate-Lacss has a front-end Java component (in fact, an ImageJ plugin ) that interacts with the end user, and a Python backend that does the GPU/CPU-accelerated computation of single-cell segmentation. The main considerations of IPC between these two are:

  1. **A messaging system** is used to serialize/de-serialize "data" into/from OS-independent byte streams, which are understood by both the Python and the Java processes. Trackmate-Lacss uses [protobuf](https://protobuf.dev/).
  2. **A wire mechanism** to transmit/receive the said byte stream. Currently, Trackmate-Lacss uses [anonymous pipe](https://en.wikipedia.org/wiki/Anonymous_pipe), which is simple and supported by pretty much every OS out there, although a plan is in place to add support for [gRPC](https://grpc.io/), which would allow deployment of the backend to a remote server.

### Protobuf

Developed by Google, protobuf is a mature messaging system with some notable advantages:

  - Supporting both forward and backward compatibility. It allows users to update the front-end and back-end independently without needing complete synchronization, which significantly improves robustness and the user experience.
  - Very lightweight and one of the fastest serialization libraries.
  - Is also the underlying messaging system of gRPC.
  - Already a dependency on Fiji (because most Google services, which some of the Fiji components rely on, are built on it).

The "data structure" of a protobuf message is defined using the so-called "protobuf language". For example, in Trackmate-Lacss, the message sent from the Java frontend to the Python backend is called "Input" and defined as:

```protobuf lacss.proto
enum DType {
    FLOAT32 = 0; // enforce float32 for now
}

message Image {
    uint64 height = 1;
    uint64 width = 2;
    uint64 channel = 3;
    DType dtype = 4;
    bytes data = 5;
}

message Settings {
    float min_cell_area = 1;
    bool remove_out_of_bound = 2;
    float scaling = 3;
    float nms_iou = 4;
    float detection_threshold = 5;
    float segmentation_threshold = 6;
    bool return_polygon = 7;
}

message Input {
    Settings settings = 1;
    Image image = 2;
}
```

It is fairly intuitive and easy to read/understand.

Conversely, the message sent back is a bunch of polygons representing segmentations of individual cells:

```protobuf lacss.proto
message Point {
    float x = 1;
    float y = 2;
}

message Polygon {
    float score = 1;
    repeated Point points = 2;
}

message PolygonResult {
    repeated Polygon polygons = 1;
}
```

To use these messages in Java, one uses the protobuf compiler (protoc) to compile these definitions into Java code, i.e.,

```bash
protoc --java_out=src/main/java/ lacss.proto
```

This creates a new Java class **LacssMsg**, which can be imported and used by other codes:

```java
LacssMsg.Settings settingMsg = LacssMsg.Settings.newBuilder()
    .setDetectionThreshold(getFloat(LacssDetectorFactory.KEY_DETECTION_THRESHOLD))
    .setMinCellArea(getFloat(LacssDetectorFactory.KEY_MIN_CELL_AREA))
    .setScaling(getFloat(LacssDetectorFactory.KEY_SCALING))
    .setNmsIou(getFloat(LacssDetectorFactory.KEY_NMS_IOU))
    .setSegmentationThreshold(getFloat(LacssDetectorFactory.KEY_SEGMENTATION_THRESHOLD))
    .setRemoveOutOfBound((boolean)settings.get(LacssDetectorFactory.KEY_REMOVE_OUT_OF_BOUNDS))
    .setReturnPolygon(true)
    .build();

LacssMsg.Image encoded_img = LacssMsg.Image.newBuilder()
    .setWidth(width)
    .setHeight(height)
    .setChannel(n_ch)
    .setData(ByteString.copyFrom(data.array()))
    .build();

LacssMsg.Input msg = LacssMsg.Input.newBuilder()
    .setImage(encoded_img)
    .setSettings(settings)
    .build();
```

Using the message in the Python code follows a similar pattern, except one should compile the protobuf language into Python code:

```bash
protoc --python_out=lacss/deploy/ lacss.proto
```

### Anonymous pipe

Anonymous pipe is the simplest and fastest mechanism for IPC. It is useful as a base implementation just to test and stabilize the message protocol.

Implementation in Java takes only a few lines of code:

```java

// start the python process 
ProcessBuilder pb = new ProcessBuilder("python", "-m", "lacss.deploy.server", modelPath);
pyServer = pb.start();

// redirect stdin and stdout
DataOutputStream p_out = new DataOutputStream(pyServer.getOutputStream());
DataInputStream p_in = new DataInputStream(pyServer.getInputStream());

// write message
p_out.writeInt(msg.getSerializedSize());
msg.writeTo(p_out);

// read message
int msg_size = p_in.readInt();
byte[] msg_buf = new byte[msg_size];
p_in.readFully(msg_buf);
LacssMsg.PolygonResult msg = LacssMsg.PolygonResult.parseFrom(msg_buf);
```

In Python:

```python
# read message
msg_size = sys.stdin.buffer.read(4)
msg_size = struct.unpack(">i", msg_size)[0]
msg = LacssMsg.Input()
msg.ParseFromString(sys.stdin.buffer.read(msg_size))

# write message
msg_size_bits = struct.pack(">i", msg.ByteSize())
sys.stdout.buffer.write(msg_size_bits)
sys.stdout.bufferst.write(msg.SerializeToString())
```

>  **_NOTE:_**  Be sure to use `sys.stdout.buffer` instead of `sys.stdout` to disable buffering.

### Endianness

A common headache in IPC is to ensure the two processes represent the data using the same endianness. This is typically handled by the protobuf library automatically, except when one uses the `byte` data type, in which case, care needs to be taken.

Fortunately, for Java it is simple: Java always uses big-endian no matter what operation system or hardware it is on. 

For Python, one needs to make sure to interpret data received from the Java process correctly. This is fairly simple if the algorithm is built on top of NumPy (as any sane person would):

```python
import numpy as np

# for java float type
data = msg.float_byte_data
np_data = np.frombuffer(data, dtype=">f4").astype("float32")

# for java double type
data = msg.double_byte_data
np_data = np.frombuffer(data, dtype=">f8").astype("float64")
```
