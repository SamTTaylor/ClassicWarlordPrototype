package samueltaylor.classicwarlordprototype.Shapes;

/**
 * Created by Sam on 04/08/2015.
 */
public class TextObject {

    public String text;
    public float x;
    public float y;
    public float[] color;

    public TextObject()
    {
        text = "default";
        x = 0f;
        y = 0f;
        color = new float[] {1f, 1f, 1f, 1.0f};
    }

    public TextObject(String txt, float fx, float fy)
    {
        text = txt;
        x = fx;
        y = fy;
        color = new float[] {1f, 1f, 1f, 1.0f};
    }

    public void setX(float f){x=f;}
    public void setY(float f){y=f;}
    public void setText(String s){text=s;}
}