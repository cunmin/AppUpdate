package com.littleyellow.update_demo;

import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.littleyellow.update.UpdateAppManager;
import com.littleyellow.update.bean.Version;
import com.littleyellow.update.callback.CheckCallback;

import java.io.File;

/**
 * Created by Administrator on 2017/9/9 0009.
 */

public class UpdateCallback extends CheckCallback{

    /**
     *
     * @param updateApp        新版本信息
     * @param updateAppManager app更新管理器
     */
    @Override
    public void hasUpdate(final Version updateApp, final UpdateAppManager updateAppManager,boolean isForce,final File apkFile) {
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
}
