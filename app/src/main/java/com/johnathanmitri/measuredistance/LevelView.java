package com.johnathanmitri.measuredistance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.text.DecimalFormat;
import java.util.Arrays;


//This is for a level that displays the orientation of the device to help the user keep the device perfectly vertical.
public class LevelView extends View implements SensorEventListener
{
    private final ShapeDrawable levelLine;

    private final ShapeDrawable anchorLine;

    MeasureFragment hostFragment;

    //int levelLinePos = 200;
    int anchorLinePos;

    double smoothedTiltValue = 0;

    int width;
    int height;

    boolean paused = false;

    int blue = 0xff00BBF4;

    int color = Color.WHITE;
    int outlineColor = blue;


    Handler handler = new Handler();
    Runnable runnable;
    int orientationRefreshInterval = 10; // milliseconds

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] adjustedRotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    /*
    int color = Color.BLACK;
    int outlineColor = blue;
*/
    public LevelView(Context context, int width, int height)
    {
        super(context);

        this.width = width;
        this.height = height;

        levelLine = new ShapeDrawable(new RectShape());
        levelLine.getPaint().setColor(outlineColor);

        anchorLine = new ShapeDrawable(new RectShape());
        anchorLine.getPaint().setColor(color);

    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();


//        this.width = width;
//        this.height = height;
        //this.width = getWidth();
        //this.height = this.getLayoutParams().height;


        anchorLinePos = (int)(0.5 * height);

        anchorLine.setBounds(
                (int)(width*0.75),
                anchorLinePos - 1,
                width,
                anchorLinePos + 1);


        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        this.resume();



        //this.onResume();
    }


    public void updateOrientationAngles()
    {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);

        SensorManager.getOrientation(adjustedRotationMatrix, orientationAngles);

        //Tilt(orientationAngles[0], orientationAngles[1], orientationAngles[2]);
        /*if (smoothedTiltValue1 == null)
        {

        }*/

        smoothedTiltValue += (orientationAngles[1] - smoothedTiltValue) / 20f;
        //smoothedTiltValue1 = (orientationAngles[1]+ 19*smoothedTiltValue1)/20f;
        //smoothedTiltValue2 += (orientationAngles[2] - smoothedTiltValue2) / 3.0f;

        // -90 degrees is the vertical position.
        /*double[] oAngles = new double[3];
        oAngles[0] = Math.toDegrees(orientationAngles[0]);
        oAngles[1] = Math.toDegrees(orientationAngles[1]);
        oAngles[2] = Math.toDegrees(orientationAngles[2]);*/

        //orientationAngles[1] *= (orientationAngles[0] >= 0 ? -1 : 1);

        double diffFromVertical = Math.toDegrees(smoothedTiltValue);

        //diffFromVertical = 0;



        String output = "";
        //for (int i = 0; i < orientationAngles.length; i++)
        //{
        //    output += "["+ i + "]:  " + orientationAngles[i] + "  ";
        //}
        //Log.d("ANGLE 0", output);
        //Log.d("ANGLE ", "ANGLE: " + diffFromVertical);

        //the level view will have a range of -20 degrees to 20 degrees away from vertical
        double distanceMultiplier = diffFromVertical / 60;

        //max values for the multiplier to prevent the level from being outside the view.
        if (distanceMultiplier > 1)
            distanceMultiplier = 1;
        else if (distanceMultiplier < -1)
            distanceMultiplier = -1;

        distanceMultiplier+=1; //shift the range to [0,2]
        distanceMultiplier/=2; //range is now [0,1]

        int pos = (int)(distanceMultiplier * height);  //Distance from vertical.
        levelLine.setBounds(
                (int)(width*0.5),
                pos - 1,
                (int)(width * 0.75),
                pos + 1);

        this.invalidate();
    }



    public void resume()
    {
        paused = false;

        handler.postDelayed(runnable = new Runnable()
        {
            public void run()
            {
                if (!paused)
                {
                    handler.postDelayed(runnable, orientationRefreshInterval);
                    updateOrientationAngles();
                }
            }
        }, orientationRefreshInterval);


        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_UI, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void pause()
    {
        sensorManager.unregisterListener(this);
        paused = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onDraw(Canvas canvas) {

        levelLine.draw(canvas);
        anchorLine.draw(canvas);
    }


}

