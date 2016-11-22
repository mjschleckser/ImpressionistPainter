package edu.umd.cs.impressionistpainter;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Created by jon on 3/20/2016.
 */

public class ImpressionistView extends View {

    private ImpressionistImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();
    private Paint _canvasPaint = new Paint();

    private int _alpha = 150;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Circle;
    private float _brushRadius = 40;
    private VelocityTracker _velocity;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }
    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }
    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle){
        this.setDrawingCacheEnabled(true);  // for saving

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(10);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){
        Bitmap bitmap = getDrawingCache();
        clearPainting();
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    public void setImageView(ImpressionistImageView imageView){
        _imageView = imageView;
    }

    public String saveToInternalStorage(){
        return MediaStore.Images.Media.insertImage(
                getContext().getContentResolver(),
                _offScreenBitmap ,
                "An Impressionist Painting" ,
                "An impressionist painting created by my Impressionist app.");
    }

    public BrushType getBrushType() {
        return _brushType;
    }
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        if(_offScreenCanvas != null){
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.FILL);
            _offScreenCanvas.drawRect(0,0, this.getWidth(), this.getHeight(), p);
        }
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _canvasPaint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (_velocity == null) {
                    _velocity = VelocityTracker.obtain();
                } else {
                    _velocity.clear();
                }

                _imageView.showBrush();
                break;
            case MotionEvent.ACTION_MOVE:
                // Make sure we have an image to draw from
                if(_imageView == null || _imageView.getDrawable() == null) break;
                _velocity.addMovement(motionEvent);

                float touchX = motionEvent.getX();
                float touchY = motionEvent.getY();

                Rect bitmapRect = getBitmapPositionInsideImageView(_imageView);

                // Handle out-of-bounds color requests
                touchY = Math.max(touchY, bitmapRect.top);
                touchY = Math.min(touchY, (bitmapRect.top + bitmapRect.height()));
                touchX = Math.max(touchX, bitmapRect.left);
                touchX = Math.min(touchX, (bitmapRect.left + bitmapRect.width()));

                _imageView.setBrushPos(touchX, touchY);

                Bitmap b = _imageView.getDrawingCache(true);
                int color = _paint.getColor();
                try {
                    color = b.getPixel((int)touchX, (int)touchY);
                } catch (Exception e){
                    Log.e("getPixel Exception" , e.getStackTrace().toString());
                }
                _paint.setColor(color);

                switch(_brushType){
                    case Circle:
                        _offScreenCanvas.drawCircle(touchX, touchY, _brushRadius, _paint);
                        break;
                    case Splatter:
                        int randXOffset = (int) ((Math.random()-.5) * 40);
                        int randYOffset = (int) ((Math.random()-.5) * 40);
                        int randBrushSize = (int) ((Math.random()) * 45) + 15;
                        _offScreenCanvas.drawCircle(touchX + randXOffset, touchY + randYOffset,
                                                    randBrushSize, _paint);
                        break;
                    case SpeedBrush:
                        _velocity.computeCurrentVelocity(1000);
                        int velBrushSize = (int) (Math.abs(_velocity.getXVelocity()) + Math.abs(_velocity.getYVelocity()))/25;
                        velBrushSize = Math.min(150, velBrushSize);
                        velBrushSize = Math.max(20, velBrushSize);
                        _offScreenCanvas.drawCircle(touchX, touchY, velBrushSize, _paint);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                _imageView.hideBrush();
                break;
        }
        return true;
    }


    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();
        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }
        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

