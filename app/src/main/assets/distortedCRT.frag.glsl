#extension GL_OES_EGL_image_external : require

precision mediump float;
uniform samplerExternalOES camTexture;

varying vec2 v_CamTexCoordinate;
varying vec2 v_TexCoordinate;

uniform vec3  iResolution;
uniform vec4  iDate;        // (year, month, day, time in seconds)
uniform float	iGlobalTime;

// taken from https://www.shadertoy.com/view/MdlGRB
#define speed 2.

void main ()
{
        // todo add texture to channel 1
    	float dispersion = .01;
    	float distortion = .04;
    	float noisestrength = .2;
    	float bendscale = 1.5;

    	vec2 uv = v_CamTexCoordinate.xy / iResolution.xy;
    	vec2 disp = uv - vec2(.5, .5);
    	disp *= sqrt(length(disp));
    	uv += disp * bendscale;
    	uv = (uv + .5)/2.0;
    	vec2 uvr = uv * (1.0 - dispersion) + vec2(dispersion, dispersion)/2.0;
    	vec2 uvg = uv * 1.0;
    	vec2 uvb = uv * (1.0 + dispersion) - vec2(dispersion, dispersion)/2.0;

    	vec3 offset = texture2D(iChannel1, vec2(0, uv.y + iGlobalTime * 255.0)).xyz;

    	float r = mix(texture2D(camTexture, vec2(1.0 - uvr.x, uvr.y) + offset.x * distortion).xyz,
    				   offset, noisestrength).x;
    	float g = mix(texture2D(camTexture, vec2(1.0 - uvg.x, uvg.y) + offset.x * distortion).xyz,
    				   offset, noisestrength).y;
    	float b = mix(texture2D(camTexture, vec2(1.0 - uvb.x, uvb.y) + offset.x * distortion).xyz,
    				   offset, noisestrength).z;

    	if (uv.x > 0.0 && uv.x < 1.0 && uv.y > 0.0 && uv.y < 1.0) {
    		float stripes = sin(uv.y * 300.0 + iGlobalTime * 10.0);
    		vec3 col = vec3(r, g, b);
    		col = mix(col, vec3(.8), stripes / 20.0);
    		gl_FragColor = vec4(col, 1.0);
    	} else {
    		gl_FragColor = vec4(0, 0, 0, 1);
    	}
}