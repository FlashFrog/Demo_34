package com.example.administrator.demo_34;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.administrator.demo_34.View.RefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RefreshListView mRefreshListView;
    private List<String> list = new ArrayList<String>();
    private MyBaseAdapter mAdapter;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mAdapter.notifyDataSetChanged();
            mRefreshListView.completeRefresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mRefreshListView = (RefreshListView) findViewById(R.id.refreshListView);
    }

    private void initData() {
        for (int i = 0; i < 15; i++) {
            list.add("list原来的数据-" + i);
        }
        mAdapter = new MyBaseAdapter();
        mRefreshListView.setAdapter(mAdapter);
        mRefreshListView.setOnRefreshLisetener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onPullRefresh() {
                requestDataFromServer(false);
            }

            @Override
            public void onLoadMore() {
                requestDataFromServer(true);
            }
        });
    }

    /**
     * 模拟向服务器请求数据
     */
    private void requestDataFromServer(final boolean isLoadMore) {
        new Thread() {
            public void run() {
                SystemClock.sleep(3000);//模拟请求服务器的一个时间长度
                if (isLoadMore){
                    list.add("加载更多的数据-1");
                    list.add("加载更多的数据-2");
                    list.add("加载更多的数据-3");
                }else {
                    list.add(0, "下拉刷新的数据");
                }
                //在UI线程更新UI
                mHandler.sendEmptyMessage(0);
            }

            ;
        }.start();
    }

    class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setPadding(30, 30, 30, 30);
            textView.setTextSize(18);
            textView.setText(list.get(position));
            return textView;
        }
    }

}
