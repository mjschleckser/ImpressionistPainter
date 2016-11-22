package edu.umd.cs.impressionistpainter;

/**
 * Created by jon on 3/23/2016.
 */
public enum BrushType {
    Circle,
    Splatter,
    SpeedBrush;

    public BrushType next(){
        if(ordinal() >= (values().length-1)) return values()[0];
        else return values()[ordinal()+1];
    }
}
