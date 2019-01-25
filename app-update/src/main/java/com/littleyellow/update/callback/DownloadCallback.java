package com.littleyellow.update.callback;

import java.io.File;

/**
 * Created by 小黄 on 2019/1/25.
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
