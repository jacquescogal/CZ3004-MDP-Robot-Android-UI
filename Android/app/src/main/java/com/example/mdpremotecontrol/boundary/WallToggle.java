package com.example.mdpremotecontrol.boundary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.annotation.Nullable;

import com.example.mdpremotecontrol.R;

public class WallToggle extends androidx.appcompat.widget.AppCompatTextView  {

    private boolean checked=false;

    public WallToggle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ObstacleView,
                0, 0);
        try {
            setTextSize(getResources().getDimensionPixelSize(R.dimen.large_text_size));
            setText("Wall Lock");
            setTextColor(Color.BLACK);
            setGravity(Gravity.CENTER);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isChecked()) {
            setBackground(getResources().getDrawable(R.drawable.wall_lock_box_filled));
            setText("Wall:\nOn");
            setTextColor(getResources().getColor(R.color.white));
        }
        else {
            setBackground(getResources().getDrawable(R.drawable.wall_lock_box));
            setText("Wall:\nOff");
            setTextColor(getResources().getColor(R.color.black));
        }
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }

    public boolean isChecked(){
        return checked;
    }

}
