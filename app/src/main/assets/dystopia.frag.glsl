#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform samplerExternalOES camTexture;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

void main ()
{
    vec4 cameraColor = texture2D(camTexture, v_CamTexCoordinate);
    vec3 sepia = vec3(1, 1, 0.8);
//    vec3 sepia = vec3(1, 1, 0.8);
    gl_FragColor = vec4( vec3(dot(cameraColor.rgb, sepia)), cameraColor.a);
}