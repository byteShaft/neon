package nonameyetsoft.com.torch;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;

import java.io.IOException;
import java.util.Arrays;

public class Flashlight {

    private String[] whiteListedDevices = {"dlx", "mako", "ghost", "g2"};
    private boolean isRunning = false;
    public static boolean isBusy = false;

    private Camera camera;
    private Camera.Parameters params;

    public Flashlight(Camera camera, Camera.Parameters params) {
        this.camera = camera;
        this.params = params;
    }

    public static boolean isAvailable(Context context) {
        boolean availability;
        PackageManager packageManager = context.getPackageManager();
        availability = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        return availability;
    }

    public boolean isOn() { return isRunning; }

    public void turnOn() {
        // We have a list of "known-to-work" devices where we don't
        // need any videoTexture hacks.
        if(Arrays.asList(whiteListedDevices).contains(Build.DEVICE)) {
            setCameraPreviewWithTorchOn();
        }
        // We don't "officially" support gingerbread devices but we don't
        // want them to be left off, so we implemented this code as a gamble
        // for such devices.
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
            setCameraPreviewWithTorchOn();
            // For all other devices, start videoTexture before attempting to
            // enable flash. <Known to be a bit slow>.
        } else {
            setVideoTexture();
            setCameraPreviewWithTorchOn();
        }
    }

    public void turnOff() {
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
        isRunning = false;
    }

    private void setCameraPreviewWithTorchOn() {
        camera.startPreview();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(params);
        isRunning = true;
    }

    @TargetApi(11)
    private void setVideoTexture() {
        // Flashlight does not work on many devices unless
        // surfaceTexture is set.
        SurfaceTexture mSurfaceTexture = new SurfaceTexture(0);
        try {
            camera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
