#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform samplerExternalOES camTexture;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

void main ()
{
    vec4 cameraColor = texture2D(camTexture, v_CamTexCoordinate);
//    vec3 sepia = vec3(1, 1, 0.8);
    gl_FragColor = vec4( 1.0-cameraColor.r,1.0-cameraColor.g,
            1.0-cameraColor.b, cameraColor.a);
}