package com.app.ben.notetaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class LinedEditText extends android.support.v7.widget.AppCompatEditText {
    private final Rect mRect;
    private final Paint mPaint;

    public LinedEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.parseColor("#B71C1C"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int curHeight = 0;
        Rect r = mRect;
        int baseline = getLineBounds(0, r);
        for (curHeight = baseline + 1; curHeight < height;
             curHeight += getLineHeight())
        {
            canvas.drawLine(r.left, curHeight, r.right, curHeight, mPaint);
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        invalidate();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
