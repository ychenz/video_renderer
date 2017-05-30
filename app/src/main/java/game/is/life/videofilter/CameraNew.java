package game.is.life.videofilter;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;

/**
 * Created by yzhao on 5/25/17.
 */

public class CameraNew implements CameraSupport {
    private CameraDevice camera;
    private CameraManager manager;

    public CameraNew(final Context context) {
        this.manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public CameraSupport open(int cameraId) {
        try {
            String[] cameraIds = manager.getCameraIdList();
            manager.openCamera(cameraIds[cameraId], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    CameraNew.this.camera = camera;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    close();
                }
            }, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public int getOrientation(int cameraId) {
        try {
            String[] cameraIds = manager.getCameraIdList();
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIds[cameraId]);
            return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Unable to get camera orientation");
    }

    @Override
    public void close() {
        if (null != camera) {
            camera.close();
            camera = null;
        }
    }
}
