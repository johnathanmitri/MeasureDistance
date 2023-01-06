package com.johnathanmitri.measuredistance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CameraOverlayView extends View
{
    private final ShapeDrawable circle1;
    private final ShapeDrawable circle1Inner;
    private final ShapeDrawable circle2;
    private final ShapeDrawable circle2Inner;

    private final ShapeDrawable line1;
    private final ShapeDrawable line1Inner;
    private final ShapeDrawable line2;
    private final ShapeDrawable line2Inner;

    private final int TOP_LINE = 1;
    private final int BOT_LINE = 2;

    FirstFragment hostFragment;

    int topLinePos = 200;
    int botLinePos = 400;

    int topLinePointerId =-1;
    int botLinePointerId = -1;


    int trackOffsetFromSide = 100;

    int circleWidth = 76;
    int circleHeight = 76;

    int lineThickness = 1;  //inner line adds this much thickness to the single pixel in the center.
    int outerLineThickness = 2;  //outer line adds this much thickness

    int width;
    int height;

    int minSeparation = (circleHeight / 2) + outerLineThickness*2 + 4;

    int blue = 0xff00BBF4;


    int color = Color.WHITE;
    int outlineColor = blue;

    /*
    int color = Color.BLACK;
    int outlineColor = blue;
*/
    public CameraOverlayView(Context context, FirstFragment hostFragment,  int width, int height)
    {
        super(context);

        this.hostFragment = hostFragment;

        this.width = width;
        this.height = height;

        topLinePos = (int)(0.25 * height);
        botLinePos = (int)(0.75 * height);

        circle1 = new ShapeDrawable(new OvalShape());
        circle1.getPaint().setColor(outlineColor);

        circle1Inner = new ShapeDrawable(new OvalShape());
        circle1Inner.getPaint().setColor(color);

        circle2 = new ShapeDrawable(new OvalShape());
        circle2.getPaint().setColor(outlineColor);

        circle2Inner = new ShapeDrawable(new OvalShape());
        circle2Inner.getPaint().setColor(color);

        line1 = new ShapeDrawable(new RectShape());
        line1.getPaint().setColor(outlineColor);

        line1Inner = new ShapeDrawable(new RectShape());
        line1Inner.getPaint().setColor(color);

        line2 = new ShapeDrawable(new RectShape());
        line2.getPaint().setColor(outlineColor);

        line2Inner = new ShapeDrawable(new RectShape());
        line2Inner.getPaint().setColor(color);

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
            circle1Inner.setBounds(
                    trackOffsetFromSide - (circleWidth / 2) + outerLineThickness*2,
                    topLinePos - (circleHeight / 2) + outerLineThickness*2,
                    trackOffsetFromSide + (circleWidth / 2)- outerLineThickness*2,
                    topLinePos + (circleWidth / 2)- outerLineThickness*2);
            line1.setBounds(
                    trackOffsetFromSide,
                    topLinePos - lineThickness - outerLineThickness,
                    width-trackOffsetFromSide,
                    topLinePos + lineThickness + outerLineThickness);
            line1Inner.setBounds(
                    trackOffsetFromSide,
                    topLinePos - lineThickness,
                    width-trackOffsetFromSide-outerLineThickness,
                    topLinePos + lineThickness);
        }
        else {
            circle2.setBounds(
                    width - trackOffsetFromSide - (circleWidth / 2),
                    botLinePos - (circleHeight / 2),
                    width - trackOffsetFromSide + (circleWidth / 2),
                    botLinePos + (circleWidth / 2));
            circle2Inner.setBounds(
                    width - trackOffsetFromSide - (circleWidth / 2) + outerLineThickness*2,
                    botLinePos - (circleHeight / 2) + outerLineThickness*2,
                    width - trackOffsetFromSide + (circleWidth / 2)- outerLineThickness*2,
                    botLinePos + (circleWidth / 2)- outerLineThickness*2);
            line2.setBounds(
                    trackOffsetFromSide,
                    botLinePos - lineThickness - outerLineThickness,
                    width-trackOffsetFromSide,
                    botLinePos + lineThickness + outerLineThickness);
            line2Inner.setBounds(
                    trackOffsetFromSide+outerLineThickness,
                    botLinePos - lineThickness,
                    width-trackOffsetFromSide   ,
                    botLinePos + lineThickness);
        }
    }

    public void setLinePos(int line, int y)
    {
        if (line == TOP_LINE)
        {
            if (y > botLinePos - minSeparation)  //if top is being moved too low
                topLinePos = botLinePos - minSeparation;
            else if (y < 0)
                topLinePos = 0;
            else
                topLinePos = y;

            setBoundsByCenter(TOP_LINE);
        }
        else
        {
            if (y < topLinePos + minSeparation)  //if bot is being moved too high
                botLinePos = topLinePos + minSeparation;
            else if (y > height)
                botLinePos = height;
            else
                botLinePos = y;

            setBoundsByCenter(BOT_LINE);
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
        int action = event.getActionMasked();
// Get the index of the pointer associated with the action.
        int index = event.getActionIndex();

        //int xPos = -1;
        //int yPos = -1;

        int xDONTUSE = (int)event.getX(index);
        int yDONTUSE = (int)event.getY(index);


        //int index = (action & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;


        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
        {
            int id = event.getPointerId(index);
            Log.d("ACTION_DOWN", "index: " + index + "   id: " +  id);

            int x = (int)event.getX(index);
            if (x < 2*trackOffsetFromSide)  //left (top) grabbed
            {
                if (topLinePointerId == -1)
                {
                    topLinePointerId = id;
                    setLinePos(TOP_LINE,(int)event.getY(index));
                }
            }
            else if (x > width - 2*trackOffsetFromSide)  //right (bottom) grabbed
            {
                if (botLinePointerId == -1)
                {
                    botLinePointerId = id;
                    setLinePos(BOT_LINE,(int)event.getY(index));
                }
            }
        }
        else if (action == MotionEvent.ACTION_MOVE)
        {
            for (int i = 0; i < event.getPointerCount(); i++)
            {
                int id = event.getPointerId(i);
                if (topLinePointerId == id)
                {
                    setLinePos(TOP_LINE,(int)event.getY(i));
                }
                else if (botLinePointerId == id)
                {
                    setLinePos(BOT_LINE,(int)event.getY(i));
                }
            }

        }
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP)
        {
            int id = event.getPointerId(index);
            if (topLinePointerId == id)
            {
                topLinePointerId = -1;

            }
            else if (botLinePointerId == id)
            {
                botLinePointerId = -1;

            }
        }

            //Log.d("DEBUG_TAG","The action is " + actionToString(action));



/*        if (event.getPointerCount() > 1) {
            Log.d("DEBUG_TAG","Multitouch event");
            // The coordinates of the current screen contact, relative to
            // the responding View or Activity.
            xPos = (int)MotionEventCompat.getX(event, index);
            yPos = (int)MotionEventCompat.getY(event, index);

        } else {
            // Single touch event
            Log.d("DEBUG_TAG","Single touch event");
            xPos = (int)MotionEventCompat.getX(event, index);
            yPos = (int) MotionEventCompat.getY(event, index);
        }*/



        /*if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN)
        {
            Log.d("DRAWING VIEW DOWN", "POINTER ID: " + event.getPointerId(event.getActionIndex()));
        }*/

        /*
        int x = (int)event.getX();
        int y = (int)event.getY();


        if (x < 2*trackOffsetFromSide)  //left grabbed
        {
            if (y < botLinePos - minSeparation) {
                topLinePos = y;
                setBoundsByCenter(1);
            }
        }
        else if (x > width - 2*trackOffsetFromSide)  //right grabbed
        {
            if (y > topLinePos + minSeparation)
            {
                botLinePos = y;
                setBoundsByCenter(2);
            }
        }
*/
       // Log.d("DRAWING VIEW CLICKED", "DRAWING VIEW CLICKED");

        hostFragment.objectResized(topLinePos, botLinePos);

        this.invalidate();

        return true;
    }

    protected void onDraw(Canvas canvas) {
        circle1.draw(canvas);
        line1.draw(canvas);
        line1Inner.draw(canvas);
        circle1Inner.draw(canvas);

        circle2.draw(canvas);
        line2.draw(canvas);
        line2Inner.draw(canvas);
        circle2Inner.draw(canvas);
    }
}

