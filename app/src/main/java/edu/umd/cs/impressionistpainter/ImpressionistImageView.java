package edu.umd.cs.impressionistpainter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Matt on 11/22/2016.
 */

public class ImpressionistImageView extends ImageView {

    boolean _showBrush;
    int _brushX, _brushY;
    Paint _paint;

    public ImpressionistImageView(Context context) {
        super(context);
        init();
    }
    public ImpressionistImageView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }
    public ImpressionistImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        _brushX = 0;
        _brushY = 0;
        _showBrush = false;

        _paint = new Paint();
        _paint.setColor(Color.GREEN);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeWidth(5);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(_showBrush) canvas.drawCircle(_brushX, _brushY, 15, _paint);
    }

    public void setBrushPos(float touchX, float touchY) {
        _brushX = (int)touchX;
        _brushY = (int)touchY;
        invalidate();
    }

    public void showBrush(){
        _showBrush = true;
        invalidate();
    }

    public void hideBrush(){
        _showBrush = false;
        invalidate();
    }

}
