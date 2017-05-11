package com.toksaitov.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawingView extends View {

    private static final int POINTER_INITIAL_COLOR = Color.RED;
    private static final float POINTER_DISTANCE_THRESHOLD = 4;
    private static final float POINTER_STROKE_WIDTH = 100;

    private Bitmap savedBitmap;

    private Bitmap backBuffer;
    private Canvas backBufferCanvas;
    private Paint backBufferPaint;

    private float previousX, previousY;

    private Path path;
    private Paint pathPaint;

    public boolean shouldSave;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        savedBitmap = null;

        backBufferPaint = new Paint(Paint.DITHER_FLAG);

        previousX = -1.0f;
        previousY = -1.0f;

        path = new Path();
        pathPaint = new Paint(Paint.DITHER_FLAG);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setColor(POINTER_INITIAL_COLOR);
        pathPaint.setStrokeWidth(POINTER_STROKE_WIDTH);

        shouldSave = false;
    }

    public void loadBitmap(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        savedBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public boolean saveBitmap(File file) {
        boolean success = false;

        if (file == null) {
            return false;
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            backBuffer.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();

            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    success = false;
                }
            }
        }

        return success;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                onTouchStart(event.getX(), event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event.getX(), event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                onTouchEnd(event.getX(), event.getY());
                invalidate();
                break;
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            backBuffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            backBufferCanvas = new Canvas(backBuffer);
            if (savedBitmap != null) {
                backBufferCanvas.drawBitmap(savedBitmap, 0, 0, backBufferPaint);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(backBuffer, 0, 0, backBufferPaint);
        canvas.drawPath(path, pathPaint);
    }

    private void onTouchStart(float x, float y) {
        path.reset();
        path.moveTo(x, y);

        previousX = x;
        previousY = y;
    }

    private void onTouchMove(float x, float y) {
        float dx = Math.abs(x - previousX);
        float dy = Math.abs(y - previousY);
        float distanceSquared = (float) Math.sqrt(dx * dx + dy * dy);
        if (distanceSquared > POINTER_DISTANCE_THRESHOLD) {
            path.quadTo(
                previousX,
                previousY,
                (previousX + x) / 2.0f,
                (previousY + y) / 2.0f
            );

            previousX = x;
            previousY = y;
        }
    }

    private void onTouchEnd(float x, float y) {
        path.lineTo(x, y);
        backBufferCanvas.drawPath(path, pathPaint);
        path.reset();

        shouldSave = true;
    }

}
