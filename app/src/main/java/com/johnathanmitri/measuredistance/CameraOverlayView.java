package com.johnathanmitri.measuredistance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CameraOverlayView extends View
{
    private final ShapeDrawable circle1;
    private final ShapeDrawable circle2;
    private final ShapeDrawable line1;
    private final ShapeDrawable line2;

    FirstFragment hostFragment;

    int topLinePos = 200;
    int botLinePos = 400;

    int minSeperation = 20;

    int trackOffsetFromSide = 100;

    int circleWidth = 76;
    int circleHeight = 76;

    int lineThickness = 4;

    int width;
    int height;

    public CameraOverlayView(Context context, FirstFragment hostFragment,  int width, int height)
    {
        super(context);

        this.hostFragment = hostFragment;

        this.width = width;
        this.height = height;

        topLinePos = (int)(0.25 * height);
        botLinePos = (int)(0.75 * height);

        circle1 = new ShapeDrawable(new OvalShape());
        circle1.getPaint().setColor(0xff74AC23);
        circle2 = new ShapeDrawable(new OvalShape());
        circle2.getPaint().setColor(0xff74AC23);

        line1 = new ShapeDrawable(new RectShape());
        line1.getPaint().setColor(0xff74AC23);
        line2 = new ShapeDrawable(new RectShape());
        line2.getPaint().setColor(0xff74AC23);

        setBoundsByCenter(1);
        setBoundsByCenter(2);

        hostFragment.objectResized(topLinePos, botLinePos);
    }

    private void setBoundsByCenter(int lineNum)
    {
        if (lineNum == 1) {
            circle1.setBounds(
                    trackOffsetFromSide - (circleWidth / 2),
                    topLinePos - (circleHeight / 2),
                    trackOffsetFromSide + (circleWidth / 2),
                    topLinePos + (circleWidth / 2));
            line1.setBounds(
                    trackOffsetFromSide,
                    topLinePos - (lineThickness / 2),
                    width-trackOffsetFromSide,
                    topLinePos + (lineThickness/2));
        }
        else {
            circle2.setBounds(
                    width - trackOffsetFromSide - (circleWidth / 2),
                    botLinePos - (circleHeight / 2),
                    width - trackOffsetFromSide + (circleWidth / 2),
                    botLinePos + (circleWidth / 2));

            line2.setBounds(
                    trackOffsetFromSide,
                    botLinePos - (lineThickness / 2),
                    width-trackOffsetFromSide,
                    botLinePos + (lineThickness/2));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //if(event.getAction() == MotionEvent.ACTION_DOWN)
        //{
            /*int x = (int)event.getX();
            int y = (int)event.getY();
            int width = 50;
            int height = 50;

            drawable = new ShapeDrawable(new OvalShape());
            // If the color isn't set, the shape uses black as the default.
            drawable.getPaint().setColor(0xff74AC23);
            // If the bounds aren't set, the shape can't be drawn.
            drawable.setBounds(x, y, x + width, y + height);
        //}

        */

        int x = (int)event.getX();
        int y = (int)event.getY();


        if (x < 2*trackOffsetFromSide)  //left grabbed
        {
            if (y < botLinePos - minSeperation) {
                topLinePos = y;
                setBoundsByCenter(1);
            }
        }
        else if (x > width - 2*trackOffsetFromSide)  //right grabbed
        {
            if (y > topLinePos + minSeperation)
            {
                botLinePos = y;
                setBoundsByCenter(2);
            }
        }

        Log.d("DRAWING VIEW CLICKED", "DRAWING VIEW CLICKED");

        hostFragment.objectResized(topLinePos, botLinePos);

        this.invalidate();

        return true;
    }

    protected void onDraw(Canvas canvas) {
        circle1.draw(canvas);
        line1.draw(canvas);

        circle2.draw(canvas);
        line2.draw(canvas);
    }
}

