package com.nanchen.stepbarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: nanchen
 * Email: liushilin520@foxmail.com
 * Date: 2018-05-24  15:14
 */
public class StepBarView extends View {
    private static final String TAG = "StepBarView";

    private static final int DEFAULT_TV_COLOR = Color.parseColor("#313131"); // 默认的文本颜色
    private static final int DEFAULT_TV_SIZE = 15; // 默认的文字大小
    private static final int DEFAULT_PADDING = 8;  // 默认的步骤和文字之间的间距
    private static final int DEFAULT_CIRCLE_COLOR_CHECKED = Color.parseColor("#0892d8");
    private static final int DEFAULT_CIRCLE_COLOR_UNCHECKED = Color.parseColor("#f2f2f2");
    private static final int DEFAULT_NUMBER_COLOR_CHECKED = Color.parseColor("#f2f2f2");
    private static final int DEFAULT_NUMBER_COLOR_UNCHECKED = Color.parseColor("#313131");
    private static final int DEFAULT_LINE_COLOR = Color.parseColor("#cccccc");
    private static final int DEFAULT_EDGE_LINE_WIDTH = 30; // 默认前后两条线长度为 30dp
    private static final int DEFAULT_EDGE_CENTER_WIDTH = 50; // 默认前后两条线长度为 60dp
    private static final int DEFAULT_CIRCLE_RADIUS = 15; // 默认前后两条线长度为 15dp


    private Paint mPaint; // 画笔
    private int mCircleColorChecked; // 选中的圆的颜色
    private int mCircleColorUnchecked;// 未选中的圆的颜色
    private int mNumberColorChecked; // 选中的圆中的数字颜色
    private int mNumberColorUnchecked; // 未选中的圆中的数字颜色
    private float mCircleRadius; // 圆半径
    private List<String> mTopNames = new ArrayList<>(); // 圆圈内的标号
    private List<String> mBottomName = new ArrayList<>(); // 下面的文字


    private float mCircleTextPadding; // 下部文字和圆圈的间距
    private int mTvColor;  // 下面文字的颜色
    private float mTvSize;   // 文字大小
    private int mLineColor; // 直线的颜色
    private float mCenterLineWidth; // 中间线的长度
    private float mEdgeLineWidth;//左右两边线的宽度
    private int mCheckNum; // 当前步骤
    private Rect mRect;


    public StepBarView(Context context) {
        this(context, null);
    }

    public StepBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.StepBarView);
        mTvColor = array.getColor(R.styleable.StepBarView_step_tv_color, DEFAULT_TV_COLOR);
        mTvSize = array.getDimension(R.styleable.StepBarView_step_tv_size, sp2px(DEFAULT_TV_SIZE));
        mCircleTextPadding = array.getDimension(R.styleable.StepBarView_step_padding, dp2px(DEFAULT_PADDING));
        mCircleColorChecked = array.getColor(R.styleable.StepBarView_step_circle_color_checked, DEFAULT_CIRCLE_COLOR_CHECKED);
        mCircleColorUnchecked = array.getColor(R.styleable.StepBarView_step_circle_color_unchecked, DEFAULT_CIRCLE_COLOR_UNCHECKED);
        mNumberColorChecked = array.getColor(R.styleable.StepBarView_step_number_color_checked, DEFAULT_NUMBER_COLOR_CHECKED);
        mNumberColorUnchecked = array.getColor(R.styleable.StepBarView_step_number_color_unchecked, DEFAULT_NUMBER_COLOR_UNCHECKED);
        mLineColor = array.getColor(R.styleable.StepBarView_step_line_color, DEFAULT_LINE_COLOR);
        mEdgeLineWidth = array.getDimension(R.styleable.StepBarView_step_edge_line_width, dp2px(DEFAULT_EDGE_LINE_WIDTH));
        mCenterLineWidth = array.getDimension(R.styleable.StepBarView_step_center_line_width, dp2px(DEFAULT_EDGE_CENTER_WIDTH));
        mCircleRadius = array.getDimension(R.styleable.StepBarView_step_circle_radius, dp2px(DEFAULT_CIRCLE_RADIUS));
        mCheckNum = array.getInteger(R.styleable.StepBarView_step_check_number, 0);
        int bottomNameId = array.getResourceId(R.styleable.StepBarView_step_bottom_name, 0);
        if (bottomNameId != 0) {
            // 如果已经注入
            mBottomName = Arrays.asList(getResources().getStringArray(bottomNameId));
            // 并且默认 上部为 1 2 3 ...
            for (int i = 0; i < mBottomName.size(); i++) {
                mTopNames.add((i + 1) + "");
            }
        }
        int topNameId = array.getResourceId(R.styleable.StepBarView_step_top_name, 0);
        if (topNameId != 0) {
            mTopNames = Arrays.asList(getResources().getStringArray(topNameId));
        }
        array.recycle();
        init();
    }

    private void init() {

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
        mPaint.setStyle(Style.STROKE); // 设置为线条模式
        mPaint.setTextSize(mTvSize);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getReallyWidth(widthMeasureSpec), getReallyHeight(heightMeasureSpec));
    }

    /**
     * 获取真实的宽度
     */
    private int getReallyWidth(int widthMeasureSpec) {
        int result = 0;
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {// 至多，用于 wrap_content
            int reallyWidth = (int) (getPaddingLeft() + getPaddingRight() + mEdgeLineWidth * 2
                    + mCircleRadius * 2 * mTopNames.size() + mCenterLineWidth * (mTopNames.size() - 1));
            result = Math.min(reallyWidth, size);
        } else {
            result = size;
            // 中间线长度 = (总长度 - 左间距 - 右间距 - 前后两根线 - 圆直径 * 圆数量)/(圆数量 - 1)
            mCenterLineWidth = (result - getPaddingLeft() - getPaddingRight()
                    - mEdgeLineWidth * 2 - mCircleRadius * 2 * mTopNames.size()) / (mTopNames.size() - 1);
            mEdgeLineWidth = mCenterLineWidth * 3 / 5;
        }
        return result;
    }

    /**
     * 获取真实的高度
     */
    private int getReallyHeight(int heightMeasureSpec) {
        int result = 0;
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.AT_MOST) {// 至多，用于 wrap_content
            int reallyHeight = (int) (getPaddingBottom() + getPaddingTop() + mCircleRadius * 2
                    + mCircleTextPadding + (mPaint.getFontMetrics().bottom - mPaint.getFontMetrics().top));
            result = Math.min(reallyHeight, size);
        } else { //
            result = size;
        }
        return result;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startX = getPaddingLeft();
        float endX = getMeasuredWidth() - getPaddingRight();
        float cy = getPaddingTop() + mCircleRadius;
        mPaint.setColor(mLineColor);
        // 首先画线
        canvas.drawLine(startX, cy, endX, cy, mPaint);
        // 然后开始画圆和里面的文字
        for (int i = 0; i < mTopNames.size(); i++) {
            float cx = getPaddingLeft() + mEdgeLineWidth + mCircleRadius + i * (mCenterLineWidth + mCircleRadius * 2); // 圆形横坐标
            mPaint.getTextBounds(mTopNames.get(i), 0, mTopNames.get(i).length(), mRect);
            int numberWidth = mRect.width();
            int numberHeight = mRect.height();

            mPaint.getTextBounds(mBottomName.get(i), 0, mBottomName.get(i).length(), mRect);
            int textWidth = mRect.width();
            int textHeight = mRect.height();


            if (mCheckNum == i) { // 当前步骤，实心圆
                mPaint.setColor(mCircleColorChecked);
                mPaint.setStyle(Style.FILL);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                // 描边
                mPaint.setColor(mCircleColorChecked);
                mPaint.setStyle(Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                // 画数字
                mPaint.setColor(mNumberColorChecked);
                mPaint.setStyle(Style.FILL);
                canvas.drawText(mTopNames.get(i), cx - numberWidth / 2, cy + numberHeight / 2, mPaint);
            } else { // 在绘制非当前步骤
                mPaint.setColor(mCircleColorUnchecked);
                mPaint.setStrokeWidth(2);
                mPaint.setStyle(Style.FILL);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                // 描边
                mPaint.setColor(mNumberColorUnchecked);
                mPaint.setStyle(Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
                // 画数字
                mPaint.setColor(mNumberColorUnchecked);
                mPaint.setStyle(Style.FILL);
                canvas.drawText(mTopNames.get(i), cx - numberWidth / 2, cy + numberHeight / 2, mPaint);
            }

            // 绘制下面的文字
            float y = getPaddingTop() + mCircleRadius * 2 + mCircleTextPadding + textHeight;
            mPaint.setColor(mTvColor);
            mPaint.setStyle(Style.FILL);
            canvas.drawText(mBottomName.get(i), cx - textWidth / 2, y, mPaint);
        }
    }
}
