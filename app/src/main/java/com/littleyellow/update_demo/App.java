package com.littleyellow.update_demo;

import android.app.Application;

import org.xutils.x;

/**
 * Created by Administrator on 2017/9/9 0009.
 */

public class App extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
    }
}
