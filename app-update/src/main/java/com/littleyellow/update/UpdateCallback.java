package com.littleyellow.update;

import android.content.Context;
import android.widget.Toast;

import com.littleyellow.update.bean.UpdateAppBean;

/**
 * 新版本检测回调
 */
public abstract class UpdateCallback<T> {

    /**
     * 根据自己情况解析结果
     * @return UpdateAppBean
     */
    protected abstract UpdateAppBean parseResult(T result) throws Exception;

    /**
     * 根据自己条件判断是否要升级
     * @return UpdateAppBean
     */
    protected  abstract boolean isUpdate(UpdateAppBean updateApp,int currentVersionCode);



    /**
     * 有新版本
     * @param updateApp        新版本信息
     * @param updateAppManager app更新管理器
     */
    protected void hasNewApp(UpdateAppBean updateApp, UpdateAppManager updateAppManager) {

    }

    /**
     * 没有新版本
     */
    protected void noNewApp() {
    }

    /**
     * 网路请求之后
     */
    protected void onAfter() {
    }

    /**
     * 网络请求之前
     */
    protected void onBefore() {
    }

    protected void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
