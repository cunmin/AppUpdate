package com.littleyellow.update_demo;

import android.os.Build;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.littleyellow.update.NetManager;
import com.littleyellow.update_demo.bean.UpdataBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

/**
 * Created by Administrator on 2017/9/9 0009.
 */

public class AppUpdateNetManager extends NetManager<UpdataBean> {
    @Override
    public void getVersion(@NonNull final Callback<UpdataBean> callBack) {
        RequestParams params = new RequestParams("http://yt.online.atianqi.com:8210/yuetingol/getupload");
        JSONObject json = new JSONObject();
        try {
            json.put("clientid", "JHSG7328f");
            json.put("appcode", "ytfm");
            json.put("devicetype", "android");
            json.put("distributor", "ytfm");
            json.put("clientos", "android" + Build.VERSION.RELEASE);
            json.put("servicecode", "ytfm");
            json.put("clienttype", "PC");
            json.put("clientver",BuildConfig.VERSION_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.setAsJsonContent(true);
        params.setBodyContent(json.toString());
        params.setConnectTimeout(2000);
        x.http().post(params, new org.xutils.common.Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String json) {
                try {
                    UpdataBean resultBean = new Gson().fromJson(json, UpdataBean.class);
                    callBack.onResponse(resultBean);
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onError(e.getMessage());
                }
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
