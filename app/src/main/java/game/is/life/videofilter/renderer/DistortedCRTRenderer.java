package game.is.life.videofilter.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.SystemClock;

import com.androidexperiments.shadercam.gl.CameraRenderer;

import java.util.Date;

/**
 * Created by yzhao on 5/31/17.
 */

public class DistortedCRTRenderer extends CameraRenderer {

    private int width;
    private int height;

    public DistortedCRTRenderer(Context context, SurfaceTexture texture, int width, int height) {
        super(context, texture, width, height, "distortedCRT.frag.glsl", "base.vert.glsl");
        this.height = height;
        this.width = width;
    }

    @Override
    protected void setUniformsAndAttribs() {
        //always call super so that the built-in fun stuff can be set first
        super.setUniformsAndAttribs();

        int resolutionHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "iResolution");
        GLES20.glUniform3f(resolutionHandle, this.width, this.height,
                (float)this.width / (float)this.height);

        Date date = new Date();
        int dateHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "iDate");
        GLES20.glUniform4f(dateHandle, date.getYear(), date.getMonth(), date.getDate(),
                date.getHours() * 24 * 60 + date.getMinutes() * 60 + date.getSeconds());

        int globalTimeHandle = GLES20.glGetUniformLocation(mCameraShaderProgram, "iGlobalTime");
        GLES20.glUniform1f(globalTimeHandle, SystemClock.currentThreadTimeMillis() / 100.0f);
    }

}
