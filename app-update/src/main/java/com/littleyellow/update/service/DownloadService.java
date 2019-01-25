package com.littleyellow.update.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.littleyellow.update.NetManager;
import com.littleyellow.update.UpdateAppManager;
import com.littleyellow.update.bean.Version;
import com.littleyellow.update.callback.DownloadCallback;
import com.littleyellow.update.notification.INotification;
import com.littleyellow.update.utils.AppUpdateUtils;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;


/**
 * 后台下载
 */
public class DownloadService extends Service {

    private final int NOTIFY_ID = 0;
//    private static final String TAG = DownloadService.class.getSimpleName();
    public static boolean isRunning = false;
    private NotificationManager mNotificationManager;
    private DownloadBinder binder = new DownloadBinder();
    private NotificationCompat.Builder mBuilder;
    private INotification mNotificationCustom;
    //WeakHashMap会在java虚拟机回收内存时,找到没被使用的key,将此条目移除,所以不需要手动remove()
    public static final Map<String, DownloadCallback> mDownloadCallbacks = new WeakHashMap<>();
    private boolean isSilentDownload = false;

    /**
     * 开启服务方法
     *
     * @param context
     */
    public static void refreshService(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction("refreshService");
        context.startService(intent);
    }

    public static void bindService(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, DownloadService.class);
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
        if (isSilentDownload) {
            return;
        }
        String channelId = "app_update";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "应用更新", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false); //是否在桌面icon右上角展示小红点
            channel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
            channel.enableVibration(false);
            channel.setSound(null, null);
            mNotificationManager.createNotificationChannel(channel);
        }
        mBuilder = new NotificationCompat.Builder(this,channelId);
        mBuilder.setOngoing(true)
                .setAutoCancel(true);
        mNotificationCustom.setUp(this,mBuilder);
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    /**
     * 下载模块
     */
    private void startDownload(UpdateAppManager updateAppManager,Version updateApp, final DownloadCallback callback) {
        String apkUrl = updateApp.getUrl();
        if (TextUtils.isEmpty(apkUrl)) {
            String contentText = "新版本下载路径错误";
            stop(contentText);
            return;
        }
        String appName = AppUpdateUtils.getApkName(updateApp);

        File appDir = new File(updateAppManager.getTargetPath());
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        String target = appDir + File.separator;

        updateAppManager.getNetManager().download(apkUrl, target, appName, new FileDownloadCallBack(callback));
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
        isSilentDownload = false;
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
        public void start(UpdateAppManager updateAppManager, Version updateApp, DownloadCallback callback, boolean isSilentDownload) {
            DownloadService.this.isSilentDownload = isSilentDownload;
            mNotificationCustom = updateAppManager.getNotificationCustom();
            //下载
            startDownload(updateAppManager,updateApp, callback);
        }

        public void refreshNotification(){
            isSilentDownload = false;
            setUpNotification();
        }
    }

    class FileDownloadCallBack implements NetManager.FileCallback {
        private final DownloadCallback mCallBack;
        float oldPercent = 0;
        Random random;

        public FileDownloadCallBack(@Nullable DownloadCallback callback) {
            this.mCallBack = callback;
            random = new Random();
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
            try {
                //做一下判断，防止自回调过于频繁，造成更新通知栏进度过于频繁，而出现卡顿的问题。
                float percent = progress* 100.0f/total;
                if (oldPercent+4+random.nextFloat() < percent) {
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
                    oldPercent = percent;
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
            File dest = new File(file.getParent(),file.getName().replace("_temp",""));
            file.renameTo(dest);
            Iterator j = mDownloadCallbacks.entrySet().iterator();
            while (j.hasNext()) {
                Map.Entry en = (Map.Entry) j.next();
                DownloadCallback callback = (DownloadCallback) en.getValue();
                if (callback != null) {
                    callback.onFinish(dest);
                }
            }
            if (mCallBack != null) {
                if (!mCallBack.onFinish(dest)) {
                    close();
                    return;
                }
            }

            if(isSilentDownload){
                close();
                return;
            }

            if (AppUpdateUtils.isAppOnForeground(DownloadService.this) || mBuilder == null) {
                //App前台运行
                mNotificationManager.cancel(NOTIFY_ID);
                AppUpdateUtils.installApp(DownloadService.this, dest);
            } else {
                //App后台运行
                //更新参数,注意flags要使用FLAG_UPDATE_CURRENT
                Intent installAppIntent = AppUpdateUtils.getInstallAppIntent(DownloadService.this, dest);
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
