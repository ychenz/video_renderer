#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform samplerExternalOES camTexture;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

void main ()
{
    vec4 cameraColor = texture2D(camTexture, v_CamTexCoordinate);
    vec3 lum = vec3(0.299, 0.587, 0.114);
    gl_FragColor = vec4( vec3(dot(cameraColor.rgb, lum)), cameraColor.a);
}