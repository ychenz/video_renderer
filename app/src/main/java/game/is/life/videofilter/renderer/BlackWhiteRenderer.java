package game.is.life.videofilter.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;

import com.androidexperiments.shadercam.gl.CameraRenderer;

/**
 * Created by yzhao on 5/31/17.
 */

public class BlackWhiteRenderer extends CameraRenderer {

    public BlackWhiteRenderer(Context context, SurfaceTexture previewSurface, int width, int height)
    {
        super(context, previewSurface, width, height, "blackwhite.frag.glsl", "base.vert.glsl");

        //other setup if need be done here
    }

    /**
     * we override {@link #setUniformsAndAttribs()} and make sure to call the super so we can add
     * our own uniforms to our shaders here. CameraRenderer handles the rest for us automatically
     */
    @Override
    protected void setUniformsAndAttribs()
    {
        super.setUniformsAndAttribs();
    }

    @Override
    public String toString() {
        return "Grey scale";
    }
}
