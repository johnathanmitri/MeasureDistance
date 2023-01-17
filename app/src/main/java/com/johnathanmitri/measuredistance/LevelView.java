package com.johnathanmitri.measuredistance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;

//This is for a level that displays the orientation of the device to help the user keep the device perfectly vertical.
public class LevelView extends View implements SensorEventListener
{
    private final ShapeDrawable levelLine;
    private final ShapeDrawable anchorLine;
    private final Paint textPaint;

    int anchorLinePos;
    int textXPos;
    int textYPos;

    double smoothedTiltValue = 0;

    int width;
    int height;

    int lineThickness = 4;
    int textSizeDp = 12;

    boolean paused = true;

    int blue = 0xff00BBF4;

    int color = Color.WHITE;
    int outlineColor = blue;

    Handler handler = new Handler();
    Runnable runnable;
    int orientationRefreshInterval = 10 * 1000; // microseconds

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] adjustedRotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    public LevelView(Context context, int width, int height)
    {
        super(context);

        this.width = width;
        this.height = height;

        levelLine = new ShapeDrawable(new RectShape());
        levelLine.getPaint().setColor(outlineColor);

        anchorLine = new ShapeDrawable(new RectShape());
        anchorLine.getPaint().setColor(color);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSizeDp, getResources().getDisplayMetrics())); //equivalent of 12dp
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        anchorLinePos = (int)(0.5 * height);

        anchorLine.setBounds(
                0,
                anchorLinePos - lineThickness,
                (int)(width*0.25),
                anchorLinePos + lineThickness);

        textXPos = (int)(width*0.125);
        textYPos = anchorLinePos - lineThickness-8;

        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        this.resume();
    }


    public void updateOrientationAngles()
    {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);

        SensorManager.getOrientation(adjustedRotationMatrix, orientationAngles);

        smoothedTiltValue += (orientationAngles[1] - smoothedTiltValue) / 25f; //smooth out the values so it doesnt look so erratic

        double diffFromVertical = Math.toDegrees(smoothedTiltValue);

        //the level view will have a range of -60 degrees to 60 degrees away from vertical
        double distanceMultiplier = diffFromVertical / 60;

        //max values for the multiplier to prevent the level from being outside the view.
        if (distanceMultiplier > 1)
            distanceMultiplier = 1;
        else if (distanceMultiplier < -1)
            distanceMultiplier = -1;

        distanceMultiplier+=1; //shift the range to [0,2]
        distanceMultiplier/=2; //range is now [0,1]

        int pos = (int)(distanceMultiplier * height);  // This gives the Y position in the view

        levelLine.setBounds(
                (int)(width*0.25),
                pos - lineThickness,
                (int)(width * 0.5),
                pos + lineThickness);

        this.invalidate();
    }

    public void resume()
    {
        if (!paused)
            return;

        paused = false;

        // register listener
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    orientationRefreshInterval);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    orientationRefreshInterval);
        }
    }

    public void pause()
    {
        // stop getting updated values
        sensorManager.unregisterListener(this);
        paused = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // every time the system gives us new sensor values, we read them into the arrays
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }
        updateOrientationAngles();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onDraw(Canvas canvas)
    {
        levelLine.draw(canvas);
        anchorLine.draw(canvas);
        // a little label that displays above the anchored line
        canvas.drawText("Level", textXPos, textYPos, textPaint);
    }
}

