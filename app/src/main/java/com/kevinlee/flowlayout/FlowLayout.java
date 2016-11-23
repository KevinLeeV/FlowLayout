package com.kevinlee.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:FlowLayout
 * Description: 流式布局
 * Author:KevinLee
 * Date:2016/11/23 0023
 * Time:下午 7:48
 * Email:KevinLeeV@163.com
 */
public class FlowLayout extends ViewGroup {

    private static final boolean DEFAULT_ISMULTISELECT = false;// 默认单选
    private static final int DEFAULT_TEXT_SIZE = 14;//默认字体大小
    private static final int DEFAULT_TEXT_COLOR = 0XFF383838;// 默认字体颜色
    private static final int DEFAULT_TEXT_SELECTED_COLOR = 0XFFCD6600;// 默认字体颜色
    private static final int DEFAULT_PADDING = 6;// 默认内边距
    private int mTextSize = sp2px(DEFAULT_TEXT_SIZE);// 字体大小
    private int mTextColor = DEFAULT_TEXT_COLOR;// 字体颜色
    private int mTextSelectedColor = DEFAULT_TEXT_SELECTED_COLOR;// 被选中后的字体颜色
    private int padding = dp2px(DEFAULT_PADDING);// 内边距
    private boolean isMultiSelect = DEFAULT_ISMULTISELECT;// 是否多选
    private int backgroundNormal = R.drawable.flow_background_normal;// 多选时正常的背景
    private int backgroundSelected = R.drawable.flow_background_selected;// 多选时被选中的背景
    private int background = R.drawable.flow_background;// 单选时被选中的背景

    // 是否需要动画效果
    private boolean isNeedAnim = true;
    private OnClickListener mListener;
    // 在多选模式下,存储被点击的View的position
    private List<Integer> posList = new ArrayList<>();
    // 在多选模式下,存储被点击的文本
    private List<String> titleList = new ArrayList<>();
    // 在多选模式下,存储被点击的View
    private List<View> viewList = new ArrayList<>();

    // 在new出对象时调用
    public FlowLayout(Context context) {
        this(context, null);
    }

    // 在使用布局文件时调用
    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // 在有自定义属性时调用
    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取自定义属性值
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlowLayout, defStyleAttr, 0);
        mTextSize = typedArray.getInt(R.styleable.FlowLayout_flow_textSize, mTextSize);// 自定义字体大小，默认值是初始化时设置的
        mTextColor = typedArray.getInt(R.styleable.FlowLayout_flow_textColor, mTextColor);// 自定义字体颜色，默认值是初始化时设置的
        mTextSelectedColor = typedArray.getInt(R.styleable.FlowLayout_flow_textSelectedColor, mTextSelectedColor);// 自定义字体被选中颜色，默认值是初始化时设置的
        padding = typedArray.getInt(R.styleable.FlowLayout_flow_padding, padding);// 自定义内边距，默认值是初始化时设置的
        isMultiSelect = typedArray.getBoolean(R.styleable.FlowLayout_flow_isMultiSelect, isMultiSelect);// 自定义是否多选，默认值是初始化时设置的
        if (isMultiSelect) {
            // 自定义多选模式下，正常背景，默认值是初始化时设置的
            backgroundNormal = typedArray.getResourceId(R.styleable.FlowLayout_flow_background_normal, R.drawable.flow_background_normal);
            // 自定义多选模式下，被选中背景，默认值是初始化时设置的
            backgroundSelected = typedArray.getResourceId(R.styleable.FlowLayout_flow_background_selected, R.drawable.flow_background_selected);
        } else
            // 自定义单选模式背景，默认值是初始化时设置的
            background = typedArray.getResourceId(R.styleable.FlowLayout_flow_background, R.drawable.flow_background);
        // 一定要释放
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取测量宽度的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        // 获取宽度的测量值
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // 获取高度的测量模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 获取高度的测量值
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //最终需要测量的ViewGroup的宽高
        int width = 0;
        int height = 0;
        //一行的宽度和高度
        int lineWidth = 0;
        int lineHeight = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            // 获取每一个子View,并测量子View的宽高
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
            // 获取子View 的LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
            // 获取子View的宽高
            int childWidth = childView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = childView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            // 判断已有的一行的宽度+当前子View的宽度是否大于父控件的宽度,则需要换行
            if (lineWidth + childWidth > widthSize - getPaddingLeft() - getPaddingRight()) {
                // 获取一行的最大宽度
                width = Math.max(lineWidth, width);
                // 重置一行的宽度为当前子View 的宽度
                lineWidth = childWidth;
                // 父控件的高度等于父控件原高度加上当前的行高度
                height += lineHeight;
                // 将行高度重置为子View的高度
                lineHeight = childHeight;
            } else {
                // 未换行时
                // 一行的宽度等于之前的宽度加上当前子View 的宽度
                lineWidth += childWidth;
                // 一行的高度等于之前的高度与当前子View的高度的最大值
                lineHeight = Math.max(lineHeight, childHeight);
            }
            // 最后一行时,我们还未考虑
            // 当循环到最后一个子View时
            if (i == getChildCount() - 1) {
                // 获取一行的最大宽度
                width = Math.max(lineWidth, width);
                // 父控件的高度等于父控件原高度加上当前的行高度
                height += lineHeight;
            }
        }

        setMeasuredDimension(
                widthMode == MeasureSpec.EXACTLY ? widthSize : width,
                heightMode == MeasureSpec.EXACTLY ? heightSize : height);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            allChildViewLayout();
        }
    }

    /**
     * 画出所有的子View
     */
    private void allChildViewLayout() {
        // 父控件的宽度
        int parentWidth = getMeasuredWidth();
        // 行宽度
        int lineWidth = 0;
        // 行高度
        int lineHeight = 0;
        // 父控件的左内边距
        int leftPadding = getPaddingLeft();
        // 父控件的右内边距
        int rightPadding = getPaddingRight();
        // 父控件的上内边距
        int topPadding = getPaddingTop();
        // 每个子View的左坐标
        int left;
        // 每个子View的上坐标
        int top = 0;
        // 循环取出所有的子View
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            // 获取子View 的LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
            // 获取子View的宽高
            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();

            // 判断已有的一行的宽度+当前子View的宽度是否大于父控件的宽度,则需要换行
            if (lineWidth + childWidth + lp.leftMargin + lp.rightMargin > parentWidth - leftPadding - rightPadding) {
                // 重置一行的宽度为当前子View 的宽度
                lineWidth = childWidth + lp.leftMargin + lp.rightMargin;
                // 设置left,top
                left = leftPadding + lp.leftMargin;
                top += lineHeight;
                // 重置行高
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
            } else {
                // 设置left
                left = leftPadding + lineWidth + lp.leftMargin;
                // 设置行宽度
                lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
                // 设置行高度
                lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            }
            int lc = left;
            int tc = top + lp.topMargin + topPadding;
            int rc = lc + childWidth;
            int bc = tc + childHeight;
            childView.layout(lc, tc, rc, bc);
        }
    }

    // 由于需求，我们只需要使用MarginLayoutParams
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 自定义一个点击事件接口
     */
    public interface OnClickListener {
        // 单选模式下
        void onClick(TextView view, int position);

        // 多选模式下
        void onClick(List<View> viewList, List<Integer> posList, List<String> textList);
    }

    /**
     * 设置点击事件接口
     *
     * @param listener
     */
    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 动态添加button
     *
     * @param titles 文本数组
     * @param margin 外边距
     */
    public void generateButton(String[] titles, int margin) {
        generateButton(titles, margin, margin, margin, margin);
    }

    /**
     * 动态添加button
     *
     * @param titles 文本集合
     * @param margin 外边距
     */
    public void generateButton(List<String> titles, int margin) {
        generateButton(titles, margin, margin, margin, margin);
    }

    /**
     * 动态添加button
     *
     * @param titles        文本数组
     * @param margin_left   左外边距
     * @param margin_top    上外边距
     * @param margin_right  右外边距
     * @param margin_bottom 下外边距
     */
    public void generateButton(String[] titles, int margin_left, int margin_top, int margin_right, int margin_bottom) {
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            setButtonWithTitle(margin_left, margin_top, margin_right, margin_bottom, title, i);
        }
    }

    /**
     * 动态添加button
     *
     * @param titles        文本集合
     * @param margin_left   左外边距
     * @param margin_top    上外边距
     * @param margin_right  右外边距
     * @param margin_bottom 下外边距
     */
    public void generateButton(List<String> titles, int margin_left, int margin_top, int margin_right, int margin_bottom) {
        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            setButtonWithTitle(margin_left, margin_top, margin_right, margin_bottom, title, i);
        }
    }

    /**
     * 设置button
     */
    private void setButtonWithTitle(int margin_left, int margin_top, int margin_right, int margin_bottom, final String title, final int position) {
        final TextView textView = new TextView(getContext());
        MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dp2px(margin_left);
        lp.topMargin = dp2px(margin_top);
        lp.rightMargin = dp2px(margin_right);
        lp.bottomMargin = dp2px(margin_bottom);
        textView.setLayoutParams(lp);
        textView.setPadding(padding, padding / 4, padding, padding / 4);
        textView.setText(title);
        textView.setTextSize(mTextSize);
        textView.setTextColor(mTextColor);
        textView.setBackgroundResource(isMultiSelect ? backgroundNormal : background);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNeedAnim)
                    v.startAnimation(getSelectedAnim());
                if (!isMultiSelect)
                    mListener.onClick((TextView) v, position);
                else {
                    boolean isExist = false;
                    for (int i = 0; i < posList.size(); i++) {
                        if (position == posList.get(i)) {
                            viewList.remove(i);
                            posList.remove(i);
                            titleList.remove(i);
                            textView.setBackgroundResource(backgroundNormal);
                            textView.setTextColor(mTextColor);
                            isExist = true;
                            break;
                        }
                    }
                    if (!isExist) {
                        viewList.add(textView);
                        posList.add(position);
                        titleList.add(title);
                        textView.setTextColor(mTextSelectedColor);
                        textView.setBackgroundResource(backgroundSelected);
                    }
                    mListener.onClick(viewList, posList, titleList);
                }
            }
        });
        addView(textView);
    }

    /**
     * 获取View被点击时的动画
     *
     * @return
     */
    private Animation getSelectedAnim() {
        ScaleAnimation anim = new ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(false);
        anim.setDuration(100);
        return anim;
    }

    /**
     * 设置是否多选
     * 须在调用generateButton()方法之前调用
     *
     * @param isMultiSelect
     */
    public void setIsMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }

    /**
     * 多选模式下设置正常状态的背景
     * 须在调用generateButton()方法之前调用
     *
     * @param resId
     */
    public void setBackgroundNormal(int resId) {
        backgroundNormal = resId;
    }

    /**
     * 多选模式下设置选中状态的背景
     * 须在调用generateButton()方法之前调用
     *
     * @param resId
     */
    public void setBackgroundSelected(int resId) {
        backgroundSelected = resId;
    }

    /**
     * 单选模式下设置背景
     * 须在调用generateButton()方法之前调用
     *
     * @param resId
     */
    public void setBackground(int resId) {
        background = resId;
    }

    /**
     * 设置字体大小
     * 须在调用generateButton()方法之前调用
     *
     * @param size
     */
    public void setTextSize(int size) {
        mTextSize = size;
    }

    /**
     * 设置字体颜色
     * 须在调用generateButton()方法之前调用
     *
     * @param color
     */
    public void setTextColor(int color) {
        mTextColor = color;
    }

    /**
     * 设置被选中后的字体颜色
     *
     * @param color
     */
    public void setTextSelectedColor(int color) {
        mTextSelectedColor = color;
    }

    /**
     * 设置是否需要动画效果
     *
     * @param isNeed
     */
    public void setNeedAnimation(boolean isNeed) {
        isNeedAnim = isNeed;
    }

    /**
     * 内边距
     *
     * @param padding
     */
    public void setPadding(int padding) {
        this.padding = dp2px(padding);
    }

    /**
     * dp转px
     */
    private int dp2px(int dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     */
    private int sp2px(int spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, getResources().getDisplayMetrics());
    }
}
