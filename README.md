# TrackMate-Lacss

A plugin of a plugin. 

This software adds a new cell detector for TraceMate, a cell/object tracker plugin for [Fiji](https://imagej.net/software/fiji/downloads). The detector uses a pretrained deep-learning model [LACSS](https://github.com/jiyuuchc/lacss) to detect and segment cells.


### FIJI Installation

1. Install [Lacss](https://github.com/jiyuuchc/lacss) according to it's [documentation](https://jiyuuchc.github.io/lacss/install/)
2. Copy the [Jar file](https://github.com/jiyuuchc/TrackMate-Lacss/releases/) into FIJI's plugin folder.


##### Installation

Lacss utilizes a deep-learning library named [JAX](https://github.com/google/jax). JAX, while operational with only a CPU limits the computation power. GPUs (NVIDIA) or TPU are recommended especially with large datasets. 

TPU Server-Client is not currently supported.

#### Nvidia GPUs 

Jax requires the prior installation of CUDA and CUDNN drivers for proper function. It is recommended to use `Command Prompt` followed by `nivida-smi` to locate the driver version and current CUDA driver version

Linux-users can likely skip to PIP installations as JAX provides wheels for Cuda and CUDNN installations. 

![Nvidia-smi](https://cdn.discordapp.com/attachments/1112582233463722014/1174251896001798144/image.png?ex=6566ea4c&is=6554754c&hm=358a17e849a9d7cb262b5f4af36ae6d76257695cfbb104ae04856b04105db18d&)

NVIDIA Drivers may need to be updated first may be best to check [here](https://www.nvidia.com/Download/index.aspx?lang=en-us) or automaticaly update with NIVIDA Geforce Experience 

### Linux Based OS

Linux-users can likely skip to PIP installations as JAX provides wheels for Cuda and CUDNN installations. 

Examples: 
```
pip install --upgrade pip
pip install --upgrade "jax[cuda11_pip]" -f https://storage.googleapis.com/jax-releases/jax_cuda_releases.html
pip install lacss
```

CUDA 12 compatible GPUs can utilize a different JAX Pip for installation:

```
pip install -U "jax[cuda12_pip]" -f https://storage.googleapis.com/jax-releases/jax_cuda_releases.html
```

There is a python script(lacss_testgpu.py) provided and button in the GUI to detect if GPU is detected.


### Windows

JAX only supports CPU for windows offically. A community project is used in order to use GPU on Windows

Manual installation of CUDA and CUDNN is required.  

[Cuda](https://developer.nvidia.com/cuda-downloads) must be installed first before [CUDNN](https://developer.nvidia.com/cudnn).

Follow the CUDNN Installation guide from the offical NVIDIA [docs](https://docs.nvidia.com/deeplearning/cudnn/install-guide/index.html)(Section 2.3) or follow this ![guide](https://stackoverflow.com/questions/31326015/how-to-verify-cudnn-installation)

Post CUDA and CUDNN install, the Jax/jaxlib can be via the [community](https://github.com/cloudhan/jax-windows-builder) supported windows-jax:

Everything below is recommended to be completed in a virtual environment.

If CUDA version is CUDA 11:
**Downgrading jax maybe be required (untested)**

Example:
```
pip install --upgrade pip
pip install jax[cuda111] -f https://whls.blob.core.windows.net/unstable/index.html --use-deprecated legacy-resolver
pip install jax -f https://storage.googleapis.com/jax-releases/jax_cuda_releases.html
pip install lacss
```
If CUDA version is CUDA 12:
**Only cuda121/jaxlib-0.4.11+cuda12.cudnn89-cp310-cp310-win_amd64.whl has been tested**
**Reccomend installing jaxlib via local file/wheel pip was not working correctly 11/15/23**
**Jax Downgrade is REQUIRED (11/15/23)**
Download [here](https://whls.blob.core.windows.net/unstable/index.html)

Example (This order of pip installs is recommend):

```
pip install --upgrade pip
pip install lacss
pip install jaxlib localpath
pip install jax==0.4.11 -f https://storage.googleapis.com/jax-releases/jax_cuda_releases.html
```

Verify GPU detection via the python script(lacss_testgpu.py) provided or use button in the GUI.

If there is an `ml_dtypes` error:
Try:
```
pip install ml_dtypes==0.2.0
```

## Post JAX/Jaxlib and Lacss Library installations

Currently the plugin is not avaliable for direct download via Fiji plugin manager. 

VSCode or simmilar is reccomended to compile the JAR to add into Fiji after cloning this repository 

```
git clone https://github.com/Nick-Kuang/TrackMate-Lacss.git
```

Fiji can be found [here](https://imagej.net/software/fiji/downloads) if not downloaded already.

After compling the JAR this file should be placed in:

```
.../Fiji/plugins/
```

After restarting FIJI the plugin should be found in the taskbar via Plugins -> Tracking -> Trackmate
