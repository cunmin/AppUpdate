package com.littleyellow.update.bean;

/**
 * Created by 小黄 on 2019/1/22.
 */

public interface Version {

    /**
     * 版本名
     */
    String getVersionName();

    /**
     * 版本号
     */
    String getVersionNo();

    /**
     * 更新日志
     */
    String getUpdateLog();

    /**
     * 下载地址
     */
    String getUrl();

    /**
     * 文件大小
     */
    String getSize();

    /**
     * 是否强制更新
     */
    boolean hasUpdate(int installVersionCode);

    /**
     * 是否强制更新
     */
    boolean isForce(int installVersionCode);

    /**
     * 安装包md5检验用，不用检验返回null即可
     */
    String getMd5();
}
