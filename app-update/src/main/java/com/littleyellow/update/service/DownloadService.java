package com.littleyellow.update.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.littleyellow.update.HttpManager;
import com.littleyellow.update.bean.UpdateAppBean;
import com.littleyellow.update.notification.NotificationCustom;
import com.littleyellow.update.utils.AppUpdateUtils;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * 后台下载
 */
public class DownloadService extends Service {

    private static final int NOTIFY_ID = 0;
    private static final String TAG = DownloadService.class.getSimpleName();
    public static boolean isRunning = false;
    private NotificationManager mNotificationManager;
    private DownloadBinder binder = new DownloadBinder();
    private NotificationCompat.Builder mBuilder;
    private NotificationCustom mNotificationCustom;
    //WeakHashMap会在java虚拟机回收内存时,找到没被使用的key,将此条目移除,所以不需要手动remove()
    public static final Map<String, DownloadCallback> mDownloadCallbacks = new WeakHashMap<>();
    //    /**
//     * 开启服务方法
//     *
//     * @param context
//     */
//    public static void startService(Context context) {
//        Intent intent = new Intent(context, DownloadService.class);
//        context.startService(intent);
//    }
    private boolean mDismissNotificationProgress = false;

    public static void bindService(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        isRunning = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 返回自定义的DownloadBinder实例
        return binder;
    }

    @Override
    public void onDestroy() {
        mNotificationManager = null;
        super.onDestroy();
    }

    /**
     * 创建通知
     */
    private void setUpNotification() {
        if (mDismissNotificationProgress) {
            return;
        }

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setOngoing(true)
                .setAutoCancel(true);
        mNotificationCustom.setUp(this,mBuilder);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    /**
     * 下载模块
     */
    private void startDownload(UpdateAppBean updateApp, final DownloadCallback callback) {

        mDismissNotificationProgress = updateApp.isDismissNotificationProgress();

        String apkUrl = updateApp.getApkFileUrl();
        if (TextUtils.isEmpty(apkUrl)) {
            String contentText = "新版本下载路径错误";
            stop(contentText);
            return;
        }
        String appName = AppUpdateUtils.getApkName(updateApp);

        File appDir = new File(updateApp.getTargetPath());
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        String target = appDir + File.separator + updateApp.getNewVersion();

        updateApp.getHttpManager().download(apkUrl, target, appName, new FileDownloadCallBack(callback));
    }

    private void stop(String contentText) {
        if (mBuilder != null) {
            mNotificationCustom.stop(this,mBuilder,contentText);
            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(NOTIFY_ID, notification);
        }
        close();
    }

    private void close() {
        stopSelf();
        isRunning = false;
    }

    /**
     * 进度条回调接口
     */
    public interface DownloadCallback {
        /**
         * 开始
         */
        void onStart();

        /**
         * 进度
         * @param percent  百分比
         * @param progress  进度 0.00 -1.00 ，总大小
         * @param totalSize 总大小 单位B
         */
        void onProgress(float percent,long progress, long totalSize);

        /**
         * 下载完了
         * @param file 下载的app
         * @return true ：下载完自动跳到安装界面，false：则不进行安装
         */
        boolean onFinish(File file);

        /**
         * 下载异常
         *
         * @param msg 异常信息
         */
        void onError(String msg);
    }

    /**
     * DownloadBinder中定义了一些实用的方法
     *
     * @author user
     */
    public class DownloadBinder extends Binder {
        /**
         * 开始下载
         *
         * @param updateApp 新app信息
         * @param callback  下载回调
         */
        public void start(UpdateAppBean updateApp, DownloadCallback callback) {
            mNotificationCustom = updateApp.getNotificationCustom();
            //下载
            startDownload(updateApp, callback);
        }


    }

    class FileDownloadCallBack implements HttpManager.FileCallback {
        private final DownloadCallback mCallBack;
        int oldRate = 0;

        public FileDownloadCallBack(@Nullable DownloadCallback callback) {
            super();
            this.mCallBack = callback;
        }

        @Override
        public void onBefore() {
            //初始化通知栏
            setUpNotification();
            if (null != mCallBack) {
                mCallBack.onStart();
            }
            Iterator j = mDownloadCallbacks.entrySet().iterator();
            while (j.hasNext()) {
                Map.Entry en = (Map.Entry) j.next();
                DownloadCallback callback = (DownloadCallback) en.getValue();
                if (null != callback) {
                    callback.onStart();
                }
            }
        }

        @Override
        public void onProgress(long progress, long total) {
            //做一下判断，防止自回调过于频繁，造成更新通知栏进度过于频繁，而出现卡顿的问题。
            try {
                int rate = Math.round(progress * 100);
                if (oldRate != rate) {
                    float percent = progress* 100.0f/total;
                    if (mCallBack != null&&total>0) {
                        mCallBack.onProgress(percent,progress, total);
                    }
                    if (mBuilder != null&&total>0) {
                        mNotificationCustom.progress(DownloadService.this,mBuilder,percent,progress,total);
                        Notification notification = mBuilder.build();
                        notification.flags = Notification.FLAG_AUTO_CANCEL;
                        mNotificationManager.notify(NOTIFY_ID, notification);
                    }

                    Iterator j = mDownloadCallbacks.entrySet().iterator();
                    while (j.hasNext()) {
                        Map.Entry en = (Map.Entry) j.next();
                        DownloadCallback callback = (DownloadCallback) en.getValue();
                        if (callback != null&&total>0) {
                            callback.onProgress(percent,progress, total);
                        }
                    }
                    //重新赋值
                    oldRate = rate;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String error) {
            Toast.makeText(DownloadService.this, "更新新版本出错，" + error, Toast.LENGTH_SHORT).show();
            //App前台运行
            if (mCallBack != null) {
                mCallBack.onError(error);
            }
            try {
                mNotificationManager.cancel(NOTIFY_ID);
                close();
                Iterator j = mDownloadCallbacks.entrySet().iterator();
                while (j.hasNext()) {
                    Map.Entry en = (Map.Entry) j.next();
                    DownloadCallback callback = (DownloadCallback) en.getValue();
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void onResponse(File file) {
            Iterator j = mDownloadCallbacks.entrySet().iterator();
            while (j.hasNext()) {
                Map.Entry en = (Map.Entry) j.next();
                DownloadCallback callback = (DownloadCallback) en.getValue();
                if (callback != null) {
                    callback.onFinish(file);
                }
            }
            if (mCallBack != null) {
                if (!mCallBack.onFinish(file)) {
                    close();
                    return;
                }
            }

            if (AppUpdateUtils.isAppOnForeground(DownloadService.this) || mBuilder == null) {
                //App前台运行
                mNotificationManager.cancel(NOTIFY_ID);
                AppUpdateUtils.installApp(DownloadService.this, file);
            } else {
                //App后台运行
                //更新参数,注意flags要使用FLAG_UPDATE_CURRENT
                Intent installAppIntent = AppUpdateUtils.getInstallAppIntent(DownloadService.this, file);
                PendingIntent contentIntent = PendingIntent.getActivity(DownloadService.this, 0, installAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(contentIntent)
                        .setDefaults((Notification.DEFAULT_ALL));
                mNotificationCustom.complete(DownloadService.this,mBuilder,contentIntent);
                Notification notification = mBuilder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(NOTIFY_ID, notification);
            }
            //下载完自杀
            close();
        }
    }


}
