# app-update
布局切换库，加载、空白、错误，可自定义布局类型。 
## Setup

要使用这个库 `minSdkVersion`  >= 14,compileSdkVersion>=26(通知栏兼容8.0系统)

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
     * romver :
     * apkver : 1020101000
     * apkminver : 1010609999
     * flag : 1
     * reqip :
     * downloadurl : xxxxxxxxxxxxxx
     * servertime : 0
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

网络实现
```

```

在Application的onCreate()方法里调用下面方法设置全局资源，也可在其他地方设置不过要注意内部类内存泄露问题
```
LoadViewHelper.DefaultLayoutListener(new SettingCallBack());
```

- 2.viewGroup`有且只有一个子控件，即内容控件`
```
LoadViewHelper loadViewHelper = LoadViewHelper.newBuilder()
//                .emptyLayoutRes(R.layout.empty_view)
//                .errorLayoutRes(R.layout.error_view)
//                .errorLayoutRes(R.layout.loading_view)
//               .addCustomLayout(LOGIN_LAYOUT,R.layout.login_view)    //这里添加自定义布局，显示时要对应设置对的LOGIN_LAYOUT状态
//               .stateChangeListener(new ShadeStateChangeListener())
                 .build()
                 .attach(viewGroup);
```


显示内容
```
loadViewHelper.showContent();
```
显示加载
```
loadViewHelper.showLoading();
```
显示空白
```
loadViewHelper.showEmpty();
```
显示错误
```
loadViewHelper.showError();
```
显示自定义布局
```
loadViewHelper.showCustom(int state);
```

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
