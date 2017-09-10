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
    private Activity mActivity;
    private HttpManager mHttpManager;

    private NotificationCustom notificationCustom;


    private UpdateAppBean mUpdateApp;
    private String mTargetPath;
    private boolean isPost;
    private boolean mHideDialog;
    private boolean mShowIgnoreVersion;
    private boolean mDismissNotificationProgress;
    private boolean mOnlyWifi;

    private UpdateAppManager(Builder builder) {
        mActivity = builder.mActivity;
        mHttpManager = builder.mHttpManager;
        notificationCustom = builder.notificationCustom;
        mUpdateApp = builder.mUpdateApp;
        mTargetPath = builder.mTargetPath;
        isPost = builder.isPost;
        mHideDialog = builder.mHideDialog;
        mShowIgnoreVersion = builder.mShowIgnoreVersion;
        mDismissNotificationProgress = builder.mDismissNotificationProgress;
        mOnlyWifi = builder.mOnlyWifi;
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
            callback.toast(mActivity, "app正在更新");
            return;
        }

        mHttpManager.getVersion(new HttpManager.Callback<T>() {
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
        updateApp.setTargetPath(mTargetPath);
        updateApp.setHttpManager(mHttpManager);
        updateApp.setNotificationCustom(notificationCustom);
        DownloadService.bindService(mActivity.getApplicationContext(), new ServiceConnection() {
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
            UpdateAppBean mUpdateApp = callback.parseResult(result);
            int currentVersionCode = AppUpdateUtils.getVersionCode(mActivity);
            if (callback.isUpdate(mUpdateApp,currentVersionCode)) {
                //假如是静默下载，可能需要判断，
                //是否wifi,
                //是否已经下载，如果已经下载直接提示安装
                //没有则进行下载，监听下载完成，弹出安装对话框
                callback.hasNewApp(mUpdateApp, this);
            } else {
                callback.noNewApp();
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            callback.noNewApp();
        }
    }


    public static final class Builder {
        private Activity mActivity;
        private HttpManager mHttpManager;
        private NotificationCustom notificationCustom;
        private UpdateAppBean mUpdateApp;
        private String mTargetPath;
        private boolean isPost;
        private boolean mHideDialog;
        private boolean mShowIgnoreVersion;
        private boolean mDismissNotificationProgress;
        private boolean mOnlyWifi;

        private Builder() {
        }

        public Builder mActivity(Activity val) {
            mActivity = val;
            return this;
        }

        public Builder mHttpManager(HttpManager val) {
            mHttpManager = val;
            return this;
        }

        public Builder notificationCustom(NotificationCustom val) {
            notificationCustom = val;
            return this;
        }

        public Builder mUpdateApp(UpdateAppBean val) {
            mUpdateApp = val;
            return this;
        }

        public Builder mTargetPath(String val) {
            mTargetPath = val;
            return this;
        }

        public Builder isPost(boolean val) {
            isPost = val;
            return this;
        }

        public Builder mHideDialog(boolean val) {
            mHideDialog = val;
            return this;
        }

        public Builder mShowIgnoreVersion(boolean val) {
            mShowIgnoreVersion = val;
            return this;
        }

        public Builder mDismissNotificationProgress(boolean val) {
            mDismissNotificationProgress = val;
            return this;
        }

        public Builder mOnlyWifi(boolean val) {
            mOnlyWifi = val;
            return this;
        }

        public UpdateAppManager build() {
            //校验
            if (mActivity == null || mHttpManager == null|| TextUtils.isEmpty(mTargetPath)) {
                throw new NullPointerException("必需参数不能为空");
            }
            if(notificationCustom == null){
                notificationCustom = new NotificationStyle();
            }
            return new UpdateAppManager(this);
        }
    }

    public Activity getActivity() {
        return mActivity;
    }

    public static void addDownloadCallback(String tag,DownloadService.DownloadCallback callback) {
        if(null!=callback){
            DownloadService.mDownloadCallbacks.put(tag,callback);
        }
    }
}
