package com.littleyellow.update.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;

import com.littleyellow.update.R;
import com.littleyellow.update.utils.AppUpdateUtils;

/**
 * Created by 小黄 on 2017/9/10 0010.
 */

public class NotificationStyle implements NotificationCustom {

    /**
     * 创建通知
     */
    @Override
    public void setUp(Context context,NotificationCompat.Builder builder) {
        if (builder != null) {
            builder.setContentTitle("开始下载")
                    .setContentText("正在连接服务器")
                    .setSmallIcon(R.mipmap.lib_update_app_update_icon)
                    .setLargeIcon(AppUpdateUtils.drawableToBitmap(AppUpdateUtils.getAppIcon(context)))
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());
        }
    }

    @Override
    public void stop(Context context,NotificationCompat.Builder builder,String contentText) {
        if (builder != null) {
            builder.setContentTitle(AppUpdateUtils.getAppName(context)).setContentText(contentText);
        }
    }

    @Override
    public void progress(Context context,NotificationCompat.Builder builder,float percent,long progress, long total){
        if (builder != null) {
            builder.setContentTitle("正在下载：" + AppUpdateUtils.getAppName(context))
                    .setContentText(String.format("%.2f%c",percent,'%'))
                    .setProgress((int)total,(int)progress, false)
                    .setWhen(System.currentTimeMillis());
        }
    }

    @Override
    public void complete(Context context,NotificationCompat.Builder builder,PendingIntent installAppIntent){
        builder.setContentTitle(AppUpdateUtils.getAppName(context))
                .setContentText("下载完成，请点击安装")
                .setProgress(0, 0, false);
//                        .setAutoCancel(true)
    }

    /*========================================================================================================*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public NotificationStyle() {
    }

    protected NotificationStyle(Parcel in) {
    }

    public static final Creator<NotificationStyle> CREATOR = new Creator<NotificationStyle>() {
        @Override
        public NotificationStyle createFromParcel(Parcel source) {
            return new NotificationStyle(source);
        }

        @Override
        public NotificationStyle[] newArray(int size) {
            return new NotificationStyle[size];
        }
    };
}
