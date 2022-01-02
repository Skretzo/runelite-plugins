# Video Recorder
![Icon](icon.png)

## Info
Capture the in-game screen as a video.

The output video file is encoded with the [TechSmith Screen Capture Codec](https://www.techsmith.com/codecs.html) and stored in an AVI file container. To play the video you must either use [VLC media player](https://www.videolan.org/vlc/) or install the aforementioned [TSCC codec](https://www.techsmith.com/codecs.html). The latter will also allow you to edit the video in a video editing software. The output video file will use the current timestamp as its name and be saved to a folder named `videos` inside your `.runelite` folder. A button in the plugin panel with the text `Open output folder` will take you directly there.

![animation](https://user-images.githubusercontent.com/53493631/147863130-e7e46a3f-2c17-4b2e-8a1d-f8878aea6bcb.gif)

## Config options
- FPS
  - The framerate (frames/second) for the video.
- Keyframe interval
  - The interval (number of frames) between keyframes (versus delta/prediction frames)
    - A lower interval (e.g. 1) gives a bigger output file, but faster seeking (good for editing)
    - A moderate interval (e.g. 80) gives a good trade-off between output file size and seeking (recommended)
    - A higher interval (e.g. 50 FPS * 60 seconds = 3000) gives a smaller output file, but slower seeking (good for archiving)
- Compression level
  - The amount of compression for each video frame
    - 1: fast compression (bigger file size, faster seeking)
    - 6: default compression (recommended)
    - 9: high compression (smaller file size, slower seeking)
- Start video hotkey
  - The hotkey that will start the video recording. Alternatively use the start button in the plugin panel.
- Stop video hotkey
  - The hotkey that will stop the video recording. Alternatively use the stop button in the plugin panel.

## Acknowledgements
This plugin is using the [Monte Media Library](http://www.randelshofer.ch/monte/) to create video files, a project under the [Creative Commons BY 3.0 license](http://www.randelshofer.ch/monte/license.html#CCBY).
