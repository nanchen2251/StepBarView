package com.nanchen.stepbarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2018-05-25  14:03
 */
public class StepView extends View {

    private static final String TAG = "StepView";
    private static final int DEFAULT_COLOR_BG_CHECKED = Color.parseColor("#0892d8");
    private static final int DEFAULT_COLOR_BG_UNCHECKED = Color.parseColor("#CCCCCC");
    private static final int DEFAULT_COLOR_TV_CHECKED = Color.parseColor("#ffffff");
    private static final int DEFAULT_COLOR_TV_UNCHECKED = Color.parseColor("#313131");
    private static final int DEFAULT_STEP_HEIGHT = 40;
    private static final int DEFAULT_PADDING = 3; // 默认间距


    private Paint mPaint;  // 画笔
    private int mColorBgChecked; // 已经进行了的步骤的颜色
    private int mColorBgUnchecked; // 未进行步骤的颜色
    private int mColorTvChecked; // 进行步骤的文本颜色
    private int mColorTvUnchecked; // 未进行步骤的文本颜色
    private List<String> mStepNames = new ArrayList<>();// 用于存放步骤 Title
    private float mStepWidth;
    private float mStepHeight;
    private float mTipWidth;
    private Path mPath;
    private int mCheckedNum;
    private int mPadding;
    private int mStepNum;


    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StepView);
        mColorBgChecked = array.getColor(R.styleable.StepView_bg_color_checked, DEFAULT_COLOR_BG_CHECKED);
        mColorBgUnchecked = array.getColor(R.styleable.StepView_bg_color_unchecked, DEFAULT_COLOR_BG_UNCHECKED);
        mColorTvChecked = array.getColor(R.styleable.StepView_tv_color_checked, DEFAULT_COLOR_TV_CHECKED);
        mColorTvUnchecked = array.getColor(R.styleable.StepView_bg_color_unchecked, DEFAULT_COLOR_TV_UNCHECKED);
        mPadding = (int) array.getDimension(R.styleable.StepView_step_view_padding, dp2px(DEFAULT_PADDING));
        mStepHeight = array.getDimension(R.styleable.StepView_step_height, dp2px(DEFAULT_STEP_HEIGHT));
        mCheckedNum = array.getInteger(R.styleable.StepView_check_num, 0);
        int namesId = array.getResourceId(R.styleable.StepView_step_names, 0);
        if (namesId != 0) {
            mStepNames.addAll(Arrays.asList(getResources().getStringArray(namesId)));
            mStepNum = mStepNames.size();
        }
        array.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(sp2px(15));
        mPaint.setStrokeWidth(3);
        Log.e(TAG, "init: " + sp2px(15));
        mPath = new Path();
    }

    private float sp2px(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    private float dp2px(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getWidth(widthMeasureSpec), getHeight(heightMeasureSpec));
    }

    private int getWidth(int widthMeasureSpec) {
        int result = 0;
        int size = MeasureSpec.getSize(widthMeasureSpec);
        // 不管 Mode 为什么 都默认为 EXACTLY
        mStepWidth = (size - getPaddingLeft() - getPaddingRight() - mPadding * (mStepNum - 1)) / mStepNum;
        mTipWidth = mStepWidth / 6; // 无论高度用什么模式，都采用宽度的六分之一
        return size;
    }

    private int getHeight(int heightMeasureSpec) {
        int result = 0;
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {
            // wrap_content
            result = (int) mStepHeight + getPaddingTop() + getPaddingBottom();
        } else {
            // 定死或者 match_parent
            result = height;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.translate(getPaddingLeft(), getPaddingTop()); // 先把坐标中心移动到绘制的左上角


        for (int i = 0; i < mStepNum; i++) {
            mPath.reset();
            mPath.moveTo(mStepWidth * i + mPadding * i, 0);

            mPath.lineTo(mStepWidth * (i + 1) + mPadding * i, 0); // 绘制第一条横线
            if (i != mStepNum - 1)
                // 绘制右边的尖尖
                mPath.lineTo((mStepWidth) * (i + 1) + mTipWidth + mPadding * i, mStepHeight / 2);
            // 绘制右下角
            mPath.lineTo(mStepWidth * (i + 1) + mPadding * i, mStepHeight);
            // 绘制左下角
            mPath.lineTo(mStepWidth * i + mPadding * i, mStepHeight);
            if (i != 0) {
                // 不是第一个还需要画尖尖
                mPath.lineTo(mTipWidth + (mStepWidth + mPadding) * i, mStepHeight / 2);
            }
            mPath.close();

            int bgColor = mColorBgUnchecked;
            int tvColor = mColorTvUnchecked;

            if (mCheckedNum >= i) {// 已经完成的步骤，切换颜色
                bgColor = mColorBgChecked;
                tvColor = mColorTvChecked;
            }

            mPaint.setColor(bgColor);
            mPaint.setStyle(Style.FILL);
            canvas.drawPath(mPath, mPaint);

            // 绘制文字
            mPaint.setStyle(Style.FILL);
            FontMetrics metrics = mPaint.getFontMetrics();
            int tvWidth = (int) mPaint.measureText(mStepNames.get(i));
            int tvHeight = (int) (metrics.bottom - metrics.top);
            mPaint.setColor(tvColor);
            float x = 0;
            float y = mStepHeight / 2 + tvHeight / 2 - metrics.bottom;
            if (i == 0) { // 第一个
                x = mStepWidth / 2 - tvWidth / 2;
            } else if (i == mStepNum - 1) {
                // 最后一个
                x = (mStepWidth + mPadding) * i + (mStepWidth + mTipWidth) / 2 - tvWidth / 2;
            } else {
                // 中间的
                x = (mStepWidth + mPadding) * i + mTipWidth + mStepWidth / 2 - tvWidth / 2;
            }
            canvas.drawText(mStepNames.get(i), x, y, mPaint);
        }

    }
}
