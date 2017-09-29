package com.littleyellow.update_demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.littleyellow.update.UpdateAppManager;
import com.littleyellow.update.service.DownloadService;

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
                        .httpManager(new AppUpdateHttpManager())
                        .targetPath(path)
                        .build()
                        .checkVersion(new CallbackUpdate());
            }
        });

        final TextView addCallbackTv = (TextView) findViewById(R.id.add_callback_tv);
        addCallbackTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAppManager.addDownloadCallback("addTest",new DownloadService.DownloadCallback() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onProgress(float percent, long progress, long totalSize) {
                        addCallbackTv.setText(String.format("%.2f%c",percent,'%'));
                    }

                    @Override
                    public boolean onFinish(File file) {
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
                        .httpManager(new AppUpdateHttpManager())
                        .notificationCustom(new INotification())
                        .targetPath(path)
                        .build()
                        .checkVersion(new CallbackUpdate());
            }
        });
    }
}
