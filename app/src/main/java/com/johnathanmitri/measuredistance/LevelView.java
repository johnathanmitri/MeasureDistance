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
import android.view.View;

import java.text.DecimalFormat;


//This is for a level that displays the orientation of the device to help the user keep the device perfectly vertical.
public class LevelView extends View implements SensorEventListener
{
    private final ShapeDrawable levelLine;

    private final ShapeDrawable anchorLine;

    MeasureFragment hostFragment;

    //int levelLinePos = 200;
    int anchorLinePos;



    int width;
    int height;



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

        anchorLinePos = (int)(0.5 * height);

        levelLine = new ShapeDrawable(new RectShape());
        levelLine.getPaint().setColor(outlineColor);

        anchorLine = new ShapeDrawable(new RectShape());
        anchorLine.getPaint().setColor(color);

        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        this.onResume();

        handler.postDelayed(runnable = new Runnable()
        {
            public void run()
            {
                handler.postDelayed(runnable, orientationRefreshInterval);

                updateOrientationAngles();
            }
        }, orientationRefreshInterval);


        //this.onResume();
    }

    public void updateOrientationAngles()
    {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        // -90 degrees is the vertical position.
        double[] oAngles = new double[3];
        oAngles[0] = Math.toDegrees(orientationAngles[0]);
        oAngles[1] = Math.toDegrees(orientationAngles[1]);
        oAngles[2] = Math.toDegrees(orientationAngles[2]);

        double diffFromVertical = Math.toDegrees(orientationAngles[1]) - (-90);

        //if (orientationAngles)

        //Log.d("ANGLE ", Arrays.toString(orientationAngles));
        DecimalFormat df = new DecimalFormat("0.00");
        String output = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Arrays.stream(oAngles).forEach(e -> output+=(df.format(e) + " ") );
        }

        //Log.d("ANGLE", );

        //the level view will have a range of -30 degrees to 30 degrees away from vertical
        double distanceMultiplier = diffFromVertical / 70;

        //max values for the multiplier to prevent the level from being outside the view.
        if (distanceMultiplier > 1)
            distanceMultiplier = 1;
        else if (distanceMultiplier < -1)
            distanceMultiplier = -1;

        distanceMultiplier+=1; //shift the range to [0,2]
        distanceMultiplier/=2; //range is now [0,1]

        int pos = (int)(distanceMultiplier * height);  //Distance from vertical.
        levelLine.setBounds(
                0,
                pos - 1,
                (int)(width * 0.75),
                pos + 1);

        this.invalidate();
    }

    public void onResume()
    {
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

    public void onPause()
    {
        sensorManager.unregisterListener(this);
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

