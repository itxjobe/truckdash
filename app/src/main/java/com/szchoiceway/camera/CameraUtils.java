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

    /** False when the vendor library is missing or fails to load; then no native method may be called. */
    public static final boolean AVAILABLE;

    static {
        boolean ok;
        try {
            System.loadLibrary("camera_utils");
            ok = true;
        } catch (Throwable t) { // UnsatisfiedLinkError is an Error, not an Exception
            android.util.Log.e("TruckDash", "video library unavailable; run scripts/dev.sh vendor-lib", t);
            ok = false;
        }
        AVAILABLE = ok;
    }
}
