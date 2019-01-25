package com.littleyellow.update.callback;

import android.content.Context;
import android.widget.Toast;

import com.littleyellow.update.UpdateAppManager;
import com.littleyellow.update.bean.Version;

import java.io.File;

/**
 * 新版本检测回调
 */
public abstract class CheckCallback {

    /**
     * 有新版本
     * @param updateApp        新版本信息
     * @param updateAppManager app更新管理器
     */
    public abstract void hasUpdate(Version updateApp, UpdateAppManager updateAppManager, boolean isForce, File apkFile);

    /**
     * 没有新版本
     */
    public abstract void noNewApp(UpdateAppManager updateAppManager);

    /**
     * 网路请求之后
     */
    public void onAfter() {
    }

    /**
     * 网络请求之前
     */
    public void onBefore() {
    }

    public void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void updateLogic(UpdateAppManager updateAppManager,Version version,int installVersionCode,File apkFile){
        if(version.isForce(installVersionCode)){
            hasUpdate(version,updateAppManager,true,apkFile);
        } else if (version.hasUpdate(installVersionCode)) {
            //假如是静默下载，可能需要判断，
            //是否wifi,
            //是否已经下载，如果已经下载直接提示安装
            //没有则进行下载，监听下载完成，弹出安装对话框
            hasUpdate(version, updateAppManager,false,apkFile);
        } else {
            noNewApp(updateAppManager);
        }
    }

}
