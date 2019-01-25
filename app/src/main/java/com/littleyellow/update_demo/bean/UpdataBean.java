package com.littleyellow.update_demo.bean;

import com.littleyellow.update.bean.Version;

/**
 * Created by 小黄 on 2019/1/22.
 */

public class UpdataBean implements Version {


    /**
     * resultcode :
     * resultdesc :
     * romver :
     * apkver : 1020101000
     * apkminver : 1010609999
     * flag : 1
     * reqip :
     * downloadurl : http://dd.myapp.com/16891/4D5B0B36C9E6EB1BBAA5428FBE5FE461.apk?fsname=com.teamnet.hula_2.1.1_1020101000.apk
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
