package com.littleyellow.update_demo;

import android.content.Context;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.littleyellow.update.callback.DownloadCallback;

import java.io.File;

/**
 * Created by 小黄 on 2019/1/25.
 */

public class DownloadDialog implements DownloadCallback {

    private Context context;

    private MaterialDialog downloadDialog;

    public DownloadDialog(Context context) {
        this.context = context;
    }

    @Override
    public void onStart() {
        downloadDialog = new MaterialDialog.Builder(context)
                .title("正在努力下载中")
                .content("请求稍后...")
                .contentGravity(GravityEnum.CENTER)
                .progress(false, 100)
                .show();
    }

    @Override
    public void onProgress(float percent, long progress, long totalSize) {
        downloadDialog.setProgress((int) percent);
    }

    @Override
    public boolean onFinish(File file) {
        downloadDialog.dismiss();
        return true;
    }

    @Override
    public void onError(String msg) {
        downloadDialog.dismiss();
    }
}
