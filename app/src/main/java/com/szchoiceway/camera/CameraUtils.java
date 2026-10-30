package com.szchoiceway.camera;

import android.view.Surface;

/** JNI shim matching the vendor's libcamera_utils.so (class name must match its JNI symbols). */
public class CameraUtils {
    public static native void closeCamera();
    public static native void deleteSurface(int index);
    public static native int getFrameCount(int index);
    public static native int openCamera(int cameraID);
    public static native void setSensorOutputFmt(int fmt);
    static native void setDataCallBack(CameraCallBack callBack);
    public static native void setSurface(Surface surface, int index);

    static { System.loadLibrary("camera_utils"); }
}
