package game.is.life.videofilter.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.androidexperiments.shadercam.gl.CameraRenderer;

/**
 * Created by yzhao on 5/31/17.
 */

public class InvertedRenderer extends CameraRenderer {

    public InvertedRenderer(Context context, SurfaceTexture previewSurface, int width, int height)
    {
        super(context, previewSurface, width, height, "inverted.frag.glsl", "base.vert.glsl");

        //other setup if need be done here
    }

    @Override
    public String toString() {
        return "Inverted";
    }
}
