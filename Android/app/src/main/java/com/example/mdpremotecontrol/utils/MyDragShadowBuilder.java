package com.example.mdpremotecontrol.utils;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

public class MyDragShadowBuilder extends View.DragShadowBuilder {

    private Point mScaleFactor;
    private int width;
    private int height;

    // Defines the constructor for myDragShadowBuilder
    public MyDragShadowBuilder(View v,int gridInterval) {

        // Stores the View parameter passed to myDragShadowBuilder.
        super(v);

        this.width = gridInterval;
        this.height = gridInterval;

    }

    // Defines a callback that sends the drag shadow dimensions and touch point back to the
    // system.
    @Override
    public void onProvideShadowMetrics (Point size, Point touch) {
        // Defines local variables
        int width;
        int height;

        // Sets the width of the shadow to half the width of the original View
        width = this.width;

        // Sets the height of the shadow to half the height of the original View
        height = this.height;

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
        size.set(width, height);

        // Sets size parameter to member that will be used for scaling shadow image.
        mScaleFactor = size;

        // Sets the touch point's position to be in the middle of the drag shadow
        touch.set(width / 2, height / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        // Draws the ColorDrawable in the Canvas passed in from the system.
        canvas.scale(mScaleFactor.x/(float)getView().getWidth(), mScaleFactor.y/(float)getView().getHeight());
        getView().draw(canvas);
    }

}