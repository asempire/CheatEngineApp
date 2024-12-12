# CheatEngineApp

A very simple demo to how applications like CheatEngine, GameKiller, GameHacker... allow rooted android devices to cheat in games via memory manipulation.

## Installation

The structure of the project is basedon android studio so openning it as a project in android studio and building it should be a plug and play.
Building the app with `apktool` should also work but Ididn't test it.

## Compiling native binaries

If you wish to edit the natie C binaries and recompile them, make sure that you have NDK build tools that could be downloaded from [here](https://developer.android.com/ndk/downloads)
Once downloaded navigate to the jni directory and execute the ndk-build in the directory:

```shell
cd CheatEngineApp
cd native_bins/jni
$PATH_OF_NDK/ndk/android-ndk-<version>/ndk-build
```

# Demo
