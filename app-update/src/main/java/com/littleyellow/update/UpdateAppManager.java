package com.littleyellow.update;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.littleyellow.update.bean.Version;
import com.littleyellow.update.callback.CheckCallback;
import com.littleyellow.update.callback.DownloadCallback;
import com.littleyellow.update.notification.DefaultNotification;
import com.littleyellow.update.notification.INotification;
import com.littleyellow.update.service.DownloadService;
import com.littleyellow.update.utils.AppUpdateUtils;
import com.littleyellow.update.utils.Md5Util;

import java.io.File;

/**
 * 版本更新管理器
 */
public class UpdateAppManager {

    private INotification notification;

    /**
     * 必需参数
     */
    private Activity activity;
    private NetManager netManager;
    private String targetPath;

    private UpdateAppManager(Builder builder) {
        activity = builder.activity;
        netManager = builder.netManager;
        notification = builder.notification;
        targetPath = builder.targetPath;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * 检查版本，如已经在下载了，直接返回
     * @param callback
     */
    public  void checkVersion(final CheckCallback callback){
        checkVersion(callback,null);
    }

    /**
     * 检查版本，如已经在下载了，通知栏有显示下载进度，并且第二个参数也可以监听下载进度
     * @param callback
     */
    public  void checkVersion(final CheckCallback callback, DownloadCallback downloadCallback) {
        if (callback == null) {
            return;
        }
        if (DownloadService.isRunning) {
            callback.toast(activity, "app正在更新");
            if(null!=downloadCallback){
                addDownloadCallback("againListen",downloadCallback);
                DownloadService.bindService(activity.getApplicationContext(), new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        ((DownloadService.DownloadBinder) service).refreshNotification();
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                });
            }
            return;
        }
        callback.onBefore();
        netManager.getVersion(new NetManager.Callback<Version>() {
            @Override
            public void onResponse(Version result) {
                callback.onAfter();
                if (result != null) {
                    processData(result, callback);
                }else{
                    callback.noNewApp(UpdateAppManager.this);
                }
            }

            @Override
            public void onError(String error) {
                callback.onAfter();
                callback.noNewApp(UpdateAppManager.this);
            }
        });
    }

    /**
     * 不带通知的后台下载
     */
    public void silentDownload(@Nullable final Version version,DownloadCallback downloadCallback){
        download(version,downloadCallback,true);
    }

    /**
     * 不带下载回调后台下载
     */
    public void download(@Nullable final Version version){
        download(version,null,false);
    }

    /**
     * 后台下载
     *
     * @param downloadCallback 后台下载回调
     */
    public void download(@Nullable final Version version, @Nullable final DownloadCallback downloadCallback,final boolean isSilentDownload) {
        if (version == null) {
            throw new NullPointerException("updateApp 不能为空");
        }
        DownloadService.bindService(activity.getApplicationContext(), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ((DownloadService.DownloadBinder) service).start(UpdateAppManager.this,version,downloadCallback,isSilentDownload);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        });
    }

    public void installApp(File apk){
        AppUpdateUtils.installApp(activity, apk);
    }

    /**
     * 解析
     *
     * @param result
     * @param callback
     */
    private void processData(Version result, @NonNull CheckCallback callback) {
        try {
            int currentVersionCode = AppUpdateUtils.getVersionCode(activity);
            File apk = new File(targetPath,result.getVersionName()+".apk");
            callback.updateLogic(this,result,currentVersionCode,apk);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            callback.noNewApp(this);
        }
    }


    public static final class Builder {
        private Activity activity;
        private NetManager netManager;
        private INotification notification;
        private String targetPath;

        private Builder() {
        }

        public Builder activity(Activity val) {
            activity = val;
            return this;
        }

        public Builder netManager(NetManager val) {
            netManager = val;
            return this;
        }

        public Builder notification(INotification val) {
            notification = val;
            return this;
        }

        public Builder targetPath(String val) {
            targetPath = val;
            return this;
        }

        public UpdateAppManager build() {
            //校验
            if (activity == null || netManager == null|| TextUtils.isEmpty(targetPath)) {
                throw new NullPointerException("必需参数不能为空");
            }
            if(notification == null){
                notification = new DefaultNotification();
            }
            return new UpdateAppManager(this);
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public static void addDownloadCallback(String tag,DownloadCallback callback) {
        if(null!=callback){
            callback.onStart();
            DownloadService.mDownloadCallbacks.put(tag,callback);
        }
    }

    public INotification getINotification() {
        return notification;
    }

    public NetManager getNetManager() {
        return netManager;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean isDownloaded(Version updateAppBean){
        if(!TextUtils.isEmpty(updateAppBean.getMd5())){
            File apk = new File(targetPath,updateAppBean.getVersionName()+".apk");
            return !TextUtils.isEmpty(updateAppBean.getMd5())
                    && apk.exists()
                    && Md5Util.getFileMD5(apk).equalsIgnoreCase(updateAppBean.getMd5());
        }else{
            File apk = new File(targetPath,updateAppBean.getVersionName()+".apk");
            return null!=apk&&apk.exists();
        }
    }
}
