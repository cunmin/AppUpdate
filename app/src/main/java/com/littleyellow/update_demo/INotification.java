package com.littleyellow.update_demo;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Parcel;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.littleyellow.update.notification.NotificationCustom;

/**
 * Created by 小黄 on 2017/9/17 0017.
 */

public class INotification implements NotificationCustom{

    RemoteViews rv;

    @Override
    public void setUp(Context context, NotificationCompat.Builder builder) {
        rv = new RemoteViews(context.getPackageName(),R.layout.notification_update_custom_layout);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContent(rv)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.FLAG_HIGH_PRIORITY);
    }

    @Override
    public void stop(Context context, NotificationCompat.Builder builder, String contentText) {
        rv.setTextViewText(R.id.tv,contentText);
        rv.setViewVisibility(R.id.progress, View.GONE);
    }

    @Override
    public void progress(Context context, NotificationCompat.Builder builder, float percent, long progress, long total) {
        rv.setTextViewText(R.id.tv,String.format("正在下载%.2f%c",percent,'%'));
        rv.setProgressBar(R.id.progress,(int)total,(int)progress,false);
    }

    @Override
    public void complete(Context context, NotificationCompat.Builder builder, PendingIntent installAppIntent) {
        rv.setTextViewText(R.id.tv,"下载完成，请点击安装");
        rv.setViewVisibility(R.id.progress, View.GONE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.rv, flags);
    }

    public INotification() {
    }

    protected INotification(Parcel in) {
        this.rv = in.readParcelable(RemoteViews.class.getClassLoader());
    }

    public static final Creator<INotification> CREATOR = new Creator<INotification>() {
        @Override
        public INotification createFromParcel(Parcel source) {
            return new INotification(source);
        }

        @Override
        public INotification[] newArray(int size) {
            return new INotification[size];
        }
    };
}
