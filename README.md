# app-update
该库只是简单将升级逻辑代码抽离出来。单一原则，只考虑升级逻辑，没有动态申请权限,所以在调用项目代码前要把读写存储权限申请好。 因为网络框架、升级接口，提示升级UI每个项目都不一样的，为了减小库的大小^_^,所以要开发要自己实现接口如demo。
## Setup

要使用这个库 `minSdkVersion`  >= 14.  (compileSdkVersion>=26通知栏兼容8.0系统)

```gradle
allprojects {
    repositories {
        ...
        jcenter()//一般android studio新建项目都会自动加这行引进这个仓库的
    }
}

dependencies {
    implementation 'com.littleyellow:app-update:1.0.3'
}
```

## Usage
- 1.版本信息，一般在直接在接收信息的对象对应的类，实现Version接口，实现的方法里返回相应的信息即可
```
public class UpdataBean implements Version {
    /**
     * resultcode :
     * resultdesc :
     * apkver : 1020101000
     * apkminver : 1010609999
     * flag : 1
     * downloadurl : xxxxxxxxxxxxxx
     * description : 1、全新2.0UI界面，让你耳目一新；
     2、全新播放引擎，打开音频直播更快一步；
     3、新增视频功能，能看视频，更能看直播；
     4、别想了，更新吧。
     * showversion : 2.1.1
     * showfilesize : 22.6M
     */

    private String apkver;
    private String apkminver;
    private String downloadurl;
    private String description;
    private String showversion;
    private String showfilesize;
    private String md5;

    @Override
    public String getUpdateLog() {
        return description;
    }

    @Override
    public String getVersionName() {
        return showversion;
    }

    @Override
    public String getVersionNo() {
        return apkver;
    }

    @Override
    public String getUrl() {
        return downloadurl;
    }

    @Override
    public String getSize() {
        return showfilesize;
    }

    @Override
    public boolean hasUpdate(int installVersionCode) {
        try {
            return installVersionCode<Long.parseLong(apkver);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isForce(int installVersionCode) {
        try {
            return installVersionCode<Long.valueOf(apkminver);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getMd5() {
        return md5;
    }
}

```
hasUpdate、isForce方法根据自家后台的升级逻辑返回，也可重写下面说到的方法，getMd5方法是检验安装包md5，不用检验返回null/""即可

网络实现，继承NetManager 主要返回版本信息，UpdataBean类是上面创建的类，类名任意`是实现Version接口的类`。
```
public class XXX extends NetManager<UpdataBean> {
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
}    
```

- 2.升级检查
```
        UpdateAppManager.newBuilder()
                        .activity(MainActivity.this)
                        .httpManager(new XXX())
                        .targetPath(path)
                        .build()
                        .checkVersion(new CheckCallback(){
                        @Override
                        public void hasUpdate(final Version updateApp, final UpdateAppManager updateAppManager,
                                              boolean isForce,final File  apkFile) {
                            final boolean hasDownload = updateAppManager.isDownloaded(updateApp);//null!=apkFile&&apkFile.exists();
                            new MaterialDialog.Builder(updateAppManager.getActivity())
                                    .title("有新版本啦~"+(hasDownload?"(已下载)":""))
                                    .content(updateApp.getUpdateLog())
                                    .positiveText(hasDownload?"安装":"更新")
                                    .cancelable(!isForce)
                                    .canceledOnTouchOutside(!isForce)
                                    .negativeText(isForce?"退出":"暂不")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            if(hasDownload){
                                                updateAppManager.installApp(apkFile);
                                                return;
                                            }
                                            updateAppManager.download(updateApp,
                                                    new DownloadDialog(updateAppManager.getActivity()),
                                                    false);
                                        }
                                                })
                                                .show();
                                    }

                                    @Override
                                    public void noNewApp(UpdateAppManager updateAppManager) {
                                        toast(updateAppManager.getActivity(),"没有版本更新！");
                                    }
                        
                        });
```

点击下载调用
```
updateAppManager.download(version,downloadCallback,false);
```
静默下载可直接调用，其实也就调用上面方法
```
updateAppManager.silentDownload(version,downloadCallback);
```
是否已经下载完成(可能之前静默下载)
```
updateAppManager.isDownloaded(version)
```
手动跳去安装
```
updateAppManager.installApp(apkFile)
```
(一般用不到,有特殊需求可用)可添加全局的下载监听,包括静默下载  
```
UpdateAppManager.addDownloadCallback(String tag,DownloadCallback callback);
```

显示升级包括下载的样式要自己实现，每个项目肯定也不一样的，无法封装，可参考上面的做法。

- 定制（无特殊需求可跳过） 
1.若要用项目下载框架下载apk安装包可重写NetManager里面的download方法，可参考NetManager的默认下载方法（HttpURLConnection下载）。
2.若通知定义下载通知栏样式，可新建类实现INotification接口，UpdateAppManager的构造器的notificationCustom()设置进去。实现INotification里面的方法可参孝默认通知栏样式DefaultNotification 或demo里的ArrowNotification样式。

# License

```
Copyright (C) 2019, 小黄
  
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at 
 
       http://www.apache.org/licenses/LICENSE-2.0 

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
