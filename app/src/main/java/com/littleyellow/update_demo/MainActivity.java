package com.littleyellow.update_demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.littleyellow.update.UpdateAppManager;
import com.littleyellow.update.callback.DownloadCallback;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView checkTv = (TextView) findViewById(R.id.check_tv);
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/UpdateApp";
        checkTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAppManager
                        .newBuilder()
                        .activity(MainActivity.this)
                        .netManager(new AppUpdateNetManager())
                        .targetPath(path)
                        .build()
                        .checkVersion(new UpdateCallback());
            }
        });

        final TextView addCallbackTv = (TextView) findViewById(R.id.add_callback_tv);
        addCallbackTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAppManager.addDownloadCallback("addTest",new DownloadCallback() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onProgress(float percent, long progress, long totalSize) {
                        String name = Thread.currentThread().getName();
                        addCallbackTv.setText(String.format("%.2f%c",percent,'%'));
                    }

                    @Override
                    public boolean onFinish(File file) {
                        addCallbackTv.setText("已完成");
                        return false;
                    }

                    @Override
                    public void onError(String msg) {

                    }
                });

            }
        });

        TextView custom_notification_checkTv = (TextView) findViewById(R.id.custom_notification_check_tv);
        custom_notification_checkTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAppManager
                        .newBuilder()
                        .activity(MainActivity.this)
                        .netManager(new AppUpdateNetManager())
                        .notification(new ArrowNotification())
                        .targetPath(path)
                        .build()
                        .checkVersion(new UpdateCallback());
            }
        });
    }
}
