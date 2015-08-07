package samueltaylor.classicwarlordprototype.Shapes;

import android.opengl.GLES20;

public class shaderCodes {

    // Program variables
    public static int progBg;
    public static int progText;


    /* SHADER Image
     *
     * This shader is for rendering 2D images straight from a texture
     * No additional effects.
     *
     */
    public static final String vertexBG =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vec4(vPosition.x,vPosition.y,0,1);" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    public static final String fragBG =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                    "}";

    /* SHADER Text
     *
     * This shader is for rendering 2D text textures straight from a texture
     * Color and alpha blended.
     *
     */
    public static final String vertexText =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_Color;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec4 v_Color;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vec4(vPosition.x,vPosition.y,0,1);" +
                    "  v_texCoord = a_texCoord;" +
                    "  v_Color = a_Color;" +
                    "}";
    public static final String fragText =
            "precision mediump float;" +
                    "varying vec4 v_Color;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord ) * v_Color;" +
                    "  gl_FragColor.rgb *= v_Color.a;" +
                    "}";



    public static final String regionVertexShader =
            "uniform mat4 uMVPMatrix;" +
                    "uniform vec4 vColor;" +
                    "uniform vec4 playercolour;" +
                    "attribute vec4 vPosition;" +
                    "uniform vec3 vCentrePosition;" +
                    "varying vec4 color;" +
                    "uniform int usegradient;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vec4(vPosition.x,vPosition.y,0,1);" +
                    "  mediump float distanceFromReferencePoint = clamp(distance(vec2(vPosition.x,vPosition.y), vec2(vCentrePosition.x, vCentrePosition.y)), 0.0, 1.0)*2.0;" +
                    "  if((usegradient>1) && (vPosition.z>0.0) && (distanceFromReferencePoint<0.8))" +//Distance limiter manually stops the gradient going passed the destination colour
                    "    color = mix(playercolour, vColor, distanceFromReferencePoint);" +
                    "  else" +
                    "    color = vColor;"+//For Colour ID selection
                    "}";

    public static final String regionFragmentShader =
            "precision mediump float;" +
                    "varying vec4 color;" +
                    "void main() {" +
                    "  gl_FragColor = color;" +
                    "}";

    public static int loadShader(int type, String shaderCode){

        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
