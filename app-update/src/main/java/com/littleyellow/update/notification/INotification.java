package com.littleyellow.update.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

/**
 * Created by 小黄 on 2017/9/10 0010.
 */

public interface INotification{

    void setUp(Context context, NotificationCompat.Builder builder);

    void stop(Context context,NotificationCompat.Builder builder,String contentText);

    void progress(Context context,NotificationCompat.Builder builder,float percent,long progress, long total);

    void complete(Context context,NotificationCompat.Builder builder,PendingIntent installAppIntent);
}
