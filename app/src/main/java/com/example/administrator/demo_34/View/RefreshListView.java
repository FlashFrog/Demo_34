package com.example.administrator.demo_34.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.administrator.demo_34.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 说明：
 * 作者：AndroidDai
 * 邮箱：****163.com
 * Created by Administrator on 2016/5/18.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {
    private ImageView ivArrow;
    private ProgressBar pbRotate;
    private TextView tvState;
    private TextView tvTime;
    private View mHeaderView;   //头布局文件
    private int mHeaderViewHeight; //头布局文件的高
    private int downY;  //手机按下Y的坐标
    private final int PULL_REFRESH = 0;//下拉刷新的状态
    private final int RELEASE_REFRESH = 1;//松开刷新的状态
    private final int REFRESHING = 2;//正在刷新的状态
    private int currentState = PULL_REFRESH;
    private RotateAnimation upAnimation, downAnimation;//定义两个动画
    private View mFooterView;
    private int mFooterViewHeight;
    private boolean isLoadingMore = false;//当前是否正在处于加载更多

    /**
     * xml布局中创建调用这个构造方法
     *
     * @param context
     * @param attrs
     */
    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /***
     * java代码中创建调用这个构造方法
     *
     * @param context
     */
    public RefreshListView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnScrollListener(this);
        initHeaderView();
        initRotateAnimation();
        initFooterView();
    }

    /**
     * 初始化下拉加载更多
     */
    private void initFooterView() {
        mFooterView = View.inflate(getContext(), R.layout.layout_footer, null);
        mFooterView.measure(0, 0);
        mFooterViewHeight = mFooterView.getHeight();
        mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
        addFooterView(mFooterView);
    }

    /**
     * 初始化HeaderView
     */
    private void initHeaderView() {
        mHeaderView = View.inflate(getContext(), R.layout.layout_header, null);
        ivArrow = (ImageView) mHeaderView.findViewById(R.id.iv_arrow);
        pbRotate = (ProgressBar) mHeaderView.findViewById(R.id.pb_rotate);
        tvState = (TextView) mHeaderView.findViewById(R.id.tv_state);
        tvTime = (TextView) mHeaderView.findViewById(R.id.tv_time);
        mHeaderView.measure(0, 0);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
        addHeaderView(mHeaderView);
    }

    /**
     * 初始化旋转动画
     */
    private void initRotateAnimation() {
        upAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(300);
        upAnimation.setFillAfter(true);
        downAnimation = new RotateAnimation(-180, -360,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(300);
        downAnimation.setFillAfter(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentState == REFRESHING) {
                    break;
                }
                int deltaY = (int) (ev.getY() - downY);
                int paddingTop = -mHeaderViewHeight + deltaY;
                if (paddingTop > -mHeaderViewHeight && getFirstVisiblePosition() == 0) {
                    mHeaderView.setPadding(0, paddingTop, 0, 0);
                    if (paddingTop >= 0 && currentState == PULL_REFRESH) {
                        //从下拉刷新进入松开刷新状态
                        currentState = RELEASE_REFRESH;
                        refreshHeaderView();
                    } else if (paddingTop < 0 && currentState == RELEASE_REFRESH) {
                        //进入下拉刷新状态
                        currentState = PULL_REFRESH;
                        refreshHeaderView();
                    }
                    return true;//拦截TouchMove，不让listview处理该次move事件,会造成listview无法滑动
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentState == PULL_REFRESH) {
                    mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
                } else if (currentState == RELEASE_REFRESH) {
                    mHeaderView.setPadding(0, 0, 0, 0);
                    currentState = REFRESHING;
                    refreshHeaderView();
                    if (mListener != null) {
                        mListener.onPullRefresh();
                    }
                }
                break;
        }
        return super.
                onTouchEvent(ev);
    }

    /**
     * 根据currentState来更新headerView
     */
    private void refreshHeaderView() {
        switch (currentState) {
            case PULL_REFRESH:
                //下拉刷新状态
                tvState.setText("下拉刷新");
                ivArrow.startAnimation(downAnimation);
                break;
            case RELEASE_REFRESH:
                //松开刷新状态
                tvState.setText("松开刷新");
                ivArrow.startAnimation(upAnimation);
                break;
            case REFRESHING:
                //正在刷新状态
                tvState.setText("正在刷新...");
                ivArrow.clearAnimation();//因为向上的旋转动画有可能没有执行完
                ivArrow.setVisibility(INVISIBLE);
                pbRotate.setVisibility(VISIBLE);

                break;

        }
    }

    /**
     * 完成刷新操作，重置状态,在你获取完数据并更新完adater之后，去在UI线程中调用该方法
     */
    public void completeRefresh() {
        if (isLoadingMore) {
            //重置footerView状态
            mFooterView.setPadding(0,-mFooterViewHeight,0,0);
            isLoadingMore = false;
        } else {
            //重置headerView状态
            mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
            currentState = PULL_REFRESH;
            tvState.setText("下拉刷新");
            ivArrow.setVisibility(VISIBLE);
            pbRotate.setVisibility(INVISIBLE);
            tvTime.setText("最后刷新:" + getCurrentTime());
        }
    }

    /**
     * 获取当前系统时间，并格式化
     *
     * @return
     */
    private String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    public OnRefreshListener mListener;

    public void setOnRefreshLisetener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    public interface OnRefreshListener {
        void onPullRefresh();

        void onLoadMore();
    }

    /**
     * SCROLL_STATE_IDLE:闲置状态，就是手指松开
     * SCROLL_STATE_TOUCH_SCROLL：手指触摸滑动，就是按着来滑动
     * SCROLL_STATE_FLING：快速滑动后松开
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && getLastVisiblePosition() == (getCount() - 1)
                && !isLoadingMore) {
            isLoadingMore = true;
            mFooterView.setPadding(0, 0, 0, 0);
            setSelection(getCount());//让listview最后一条显示出来
            if (mListener != null){
                mListener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

}
