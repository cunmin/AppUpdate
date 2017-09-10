package com.littleyellow.update_demo;

import android.support.annotation.NonNull;

import com.littleyellow.update.HttpManager;
import com.littleyellow.update_demo.bean.ResultBean;

import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * Created by Administrator on 2017/9/9 0009.
 */

public class AppUpdateHttpManager implements HttpManager<ResultBean> {
    @Override
    public void getVersion(@NonNull final Callback<ResultBean> callBack) {
        RequestParams params = new RequestParams("https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/json/json.txt?appKey=ab55ce55Ac4bcP408cPb8c1Aaeac179c5f6f&version=0.1.0");
        params.setConnectTimeout(500);
        x.http().get(params, new org.xutils.common.Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String json) {
                //这里模拟网络框架返回实体ResultBean
                ResultBean resultBean = new ResultBean();
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    resultBean.setUpdate(jsonObject.optString("update"));
                    resultBean.setNew_version(jsonObject.optString("new_version"));
                    resultBean.setApk_file_url(jsonObject.optString("apk_file_url"));
                    resultBean.setTarget_size(jsonObject.optString("target_size"));
                    resultBean.setUpdate_log(jsonObject.optString("update_log"));
                    resultBean.setConstraint(jsonObject.optBoolean("constraint"));
                    resultBean.setNew_md5(jsonObject.optString("new_md5"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                callBack.onResponse(resultBean);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                callBack.onError(ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }

        });
    }

    @Override
    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull final FileCallback callback) {
        RequestParams params = new RequestParams(url);
        //自定义保存路径，Environment.getExternalStorageDirectory()：SD卡的根目录
        params.setSaveFilePath(path+"/"+fileName);

        x.http().get(params, new org.xutils.common.Callback.ProgressCallback<File>() {

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                callback.onBefore();
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                if(isDownloading) {
                    callback.onProgress(current, total);
                }
            }

            @Override
            public void onSuccess(File result) {
                callback.onResponse(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                callback.onError(ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }
        });
    }
}
