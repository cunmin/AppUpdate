package com.littleyellow.update;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.littleyellow.update.bean.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * app版本更新接口
 */
public abstract class NetManager<T extends Version>{

    /**
     * @param callBack 回调
     */
    public abstract void getVersion(@NonNull Callback<T> callBack);


    /**
     * 下载
     * @param url      下载地址
     * @param path     文件保存路径
     * @param fileName 文件名称
     * @param callback 回调
     */
    public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull final FileCallback callback){
        new AsyncTask<String,Integer,Object>(){

            @Override
            protected void onPreExecute() {
                callback.onBefore();
            }

            @Override
            protected Object doInBackground(String... strings) {
                String url = strings[0];
                String path = strings[1];
                String fileName = strings[2];
                try {
                    URL urlClient = new URL(url);
                    HttpURLConnection hcont = (HttpURLConnection) urlClient.openConnection();
                    hcont.connect();
                    InputStream is = hcont.getInputStream();
                    int contentLength =hcont.getContentLength();
                    //写入文件
                    OutputStream os = new FileOutputStream(path+"/"+fileName);
                    int length;
                    int lengtsh = 0;
                    byte [] bytes = new byte[4096];
                    while ((length = is.read(bytes))!= -1){
                        os.write(bytes,0,length);
                        //获取当前进度值
                        lengtsh+=length;
                        publishProgress(lengtsh,contentLength);
                    }
                    //关闭流
                    is.close();
                    os.close();
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
                return new File(path,fileName);
            }

            @Override
            protected void onPostExecute(Object result) {
                if(result instanceof File){
                    File file = (File) result;
                    callback.onResponse(file);
                }else if(result instanceof Exception){
                    Exception e = (Exception) result;
                    if(null!=e) {
                        callback.onError(e.getMessage());
                    }
                }else{
                    callback.onError(null);
                }
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                callback.onProgress(values[0],values[1]);
            }
        }.execute(url,path,fileName);


    }

    /**
     * 下载回调
     */
    public interface FileCallback {
        /**
         * 进度
         *
         * @param progress 进度0.00 - 0.50  - 1.00
         * @param total    文件总大小 单位字节
         */
        void onProgress(long progress, long total);

        /**
         * 错误回调
         *
         * @param error 错误提示
         */
        void onError(String error);

        /**
         * 结果回调
         *
         * @param file 下载好的文件
         */
        void onResponse(File file);

        /**
         * 请求之前
         */
        void onBefore();
    }

    /**
     * 网络请求回调
     */
    public interface Callback<T> {
        /**
         * 结果回调
         *
         * @param result 结果
         */
        void onResponse(T result);

        /**
         * 错误回调
         *
         * @param error 错误提示
         */
        void onError(String error);
    }
}
