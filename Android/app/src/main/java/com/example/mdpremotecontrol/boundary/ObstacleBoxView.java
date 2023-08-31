package com.example.mdpremotecontrol.boundary;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.utils.MyDragShadowBuilder;

public class ObstacleBoxView extends androidx.appcompat.widget.AppCompatTextView {

    public ObstacleBoxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ObstacleView,
                0, 0);
        try {
            setTextSize(getResources().getDimensionPixelSize(R.dimen.large_text_size));
            setText("Empty");
            setTextColor(Color.BLACK);
            setGravity(Gravity.CENTER);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}

