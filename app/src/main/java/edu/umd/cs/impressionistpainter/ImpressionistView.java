package edu.umd.cs.impressionistpainter;

import android.content.Context;
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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;

/**
 * Created by jon on 3/20/2016.
 */

public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();
    private Paint _canvasPaint = new Paint(Paint.DITHER_FLAG);

    private int _alpha = 150;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Circle;
    private float _minBrushRadius = 15;
    private float _brushRadius = 40;
    private float _maxBrushRadius = 80;
    private VelocityTracker _velocity = VelocityTracker.obtain();

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
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
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

        //TODO
        //Basically, the way this works is to listen for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                _velocity.clear();
            case MotionEvent.ACTION_MOVE:
                // Make sure we have an image to draw from
                if(_imageView == null || _imageView.getDrawable() == null) break;
                _velocity.addMovement(motionEvent);

                float touchX = motionEvent.getX();
                float touchY = motionEvent.getY();
                Rect bitmapRect = getBitmapPositionInsideImageView(_imageView);

                Bitmap b = _imageView.getDrawingCache(true);

                try {
                    _paint.setColor(b.getPixel((int)touchX, (int)touchY));
                } catch (Exception e){
                    // do nothing
                }
                _brushType = BrushType.Splatter;
                switch(_brushType){
                    case Circle:
                        _offScreenCanvas.drawCircle(touchX, touchY, _brushRadius, _paint);
                        invalidate();
                        break;
                    case Square:
                        _offScreenCanvas.drawRect(
                                touchX - _brushRadius, touchY - _brushRadius,
                                touchX + _brushRadius, touchY + _brushRadius,
                                _paint);
                        invalidate();
                        break;
                    case Splatter:
                        int randXOffset = (int) ((Math.random()-.5) * 30);
                        int randYOffset = (int) ((Math.random()-.5) * 30);
                        int randBrushSize = (int) ((Math.random()) * 30) + 15;
                        _offScreenCanvas.drawCircle(touchX + randXOffset, touchY + randYOffset,
                                                    randBrushSize, _paint);
                        invalidate();
                        break;
                }


                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
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

    public Bitmap getBitmap() {
        return _offScreenBitmap;
    }
}

