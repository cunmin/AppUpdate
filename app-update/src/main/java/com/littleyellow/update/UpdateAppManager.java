package com.littleyellow.update;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.littleyellow.update.bean.UpdateAppBean;
import com.littleyellow.update.notification.NotificationCustom;
import com.littleyellow.update.notification.NotificationStyle;
import com.littleyellow.update.service.DownloadService;
import com.littleyellow.update.utils.AppUpdateUtils;

/**
 * 版本更新管理器
 */
public class UpdateAppManager {
    /**
     * 必需参数
     */
    private Activity activity;
    private HttpManager httpManager;
    private NotificationCustom notificationCustom;


    private UpdateAppBean updateApp;
    private String targetPath;
    private boolean isPost;
    private boolean hideDialog;
    private boolean showIgnoreVersion;
    private boolean dismissNotificationProgress;
    private boolean onlyWifi;

    private UpdateAppManager(Builder builder) {
        activity = builder.activity;
        httpManager = builder.httpManager;
        notificationCustom = builder.notificationCustom;
        updateApp = builder.updateApp;
        targetPath = builder.targetPath;
        isPost = builder.isPost;
        hideDialog = builder.hideDialog;
        showIgnoreVersion = builder.showIgnoreVersion;
        dismissNotificationProgress = builder.dismissNotificationProgress;
        onlyWifi = builder.onlyWifi;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public <T> void checkVersion(final UpdateCallback<T> callback) {
        if (callback == null) {
            return;
        }
        if (DownloadService.isRunning ) {
            callback.onAfter();
            callback.toast(activity, "app正在更新");
            return;
        }

        httpManager.getVersion(new HttpManager.Callback<T>() {
            @Override
            public void onResponse(T result) {
                callback.onAfter();
                if (result != null) {
                    processData(result, callback);
                }
            }

            @Override
            public void onError(String error) {
                callback.onAfter();
                callback.noNewApp();
            }
        });
    }

    /**
     * 不带下载回调后台下载
     */
    public void download(@Nullable final UpdateAppBean updateApp){
        download(updateApp,null);
    }

    /**
     * 后台下载
     *
     * @param downloadCallback 后台下载回调
     */
    public void download(@Nullable final UpdateAppBean updateApp, @Nullable final DownloadService.DownloadCallback downloadCallback) {
        if (updateApp == null) {
            throw new NullPointerException("updateApp 不能为空");
        }
        updateApp.setTargetPath(targetPath);
        updateApp.setHttpManager(httpManager);
        updateApp.setNotificationCustom(notificationCustom);
        DownloadService.bindService(activity.getApplicationContext(), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ((DownloadService.DownloadBinder) service).start(updateApp, downloadCallback);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        });
    }


    /**
     * 解析
     *
     * @param result
     * @param callback
     */
    private <T> void processData(T result, @NonNull UpdateCallback callback) {
        try {
            UpdateAppBean updateApp = callback.parseResult(result);
            int currentVersionCode = AppUpdateUtils.getVersionCode(activity);
            if (callback.isUpdate(updateApp,currentVersionCode)) {
                //假如是静默下载，可能需要判断，
                //是否wifi,
                //是否已经下载，如果已经下载直接提示安装
                //没有则进行下载，监听下载完成，弹出安装对话框
                callback.hasNewApp(updateApp, this);
            } else {
                callback.noNewApp();
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            callback.noNewApp();
        }
    }


    public static final class Builder {
        private Activity activity;
        private HttpManager httpManager;
        private NotificationCustom notificationCustom;
        private UpdateAppBean updateApp;
        private String targetPath;
        private boolean isPost;
        private boolean hideDialog;
        private boolean showIgnoreVersion;
        private boolean dismissNotificationProgress;
        private boolean onlyWifi;

        private Builder() {
        }

        public Builder activity(Activity val) {
            activity = val;
            return this;
        }

        public Builder httpManager(HttpManager val) {
            httpManager = val;
            return this;
        }

        public Builder notificationCustom(NotificationCustom val) {
            notificationCustom = val;
            return this;
        }

        public Builder updateApp(UpdateAppBean val) {
            updateApp = val;
            return this;
        }

        public Builder targetPath(String val) {
            targetPath = val;
            return this;
        }

        public Builder isPost(boolean val) {
            isPost = val;
            return this;
        }

        public Builder hideDialog(boolean val) {
            hideDialog = val;
            return this;
        }

        public Builder showIgnoreVersion(boolean val) {
            showIgnoreVersion = val;
            return this;
        }

        public Builder dismissNotificationProgress(boolean val) {
            dismissNotificationProgress = val;
            return this;
        }

        public Builder onlyWifi(boolean val) {
            onlyWifi = val;
            return this;
        }

        public UpdateAppManager build() {
            //校验
            if (activity == null || httpManager == null|| TextUtils.isEmpty(targetPath)) {
                throw new NullPointerException("必需参数不能为空");
            }
            if(notificationCustom == null){
                notificationCustom = new NotificationStyle();
            }
            return new UpdateAppManager(this);
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public static void addDownloadCallback(String tag,DownloadService.DownloadCallback callback) {
        if(null!=callback){
            DownloadService.mDownloadCallbacks.put(tag,callback);
        }
    }
}
