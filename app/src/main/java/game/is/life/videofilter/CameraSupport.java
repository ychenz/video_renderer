package game.is.life.videofilter;

/**
 * Created by yzhao on 5/25/17.
 */

public interface CameraSupport {
    /**
     * Support both camera API version
     */

    CameraSupport open(int cameraId);
    int getOrientation(int cameraId);
    void close();
}
