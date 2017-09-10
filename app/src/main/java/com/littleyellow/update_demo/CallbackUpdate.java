package com.littleyellow.update_demo;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.littleyellow.update.UpdateAppManager;
import com.littleyellow.update.UpdateCallback;
import com.littleyellow.update.bean.UpdateAppBean;
import com.littleyellow.update.service.DownloadService;
import com.littleyellow.update_demo.bean.ResultBean;

import java.io.File;

/**
 * Created by Administrator on 2017/9/9 0009.
 */

public class CallbackUpdate extends UpdateCallback<ResultBean> {

    /**
     * 接口返回的数据统一转为UpdateAppBean实体
     * @param result
     * @return
     * @throws Exception
     */
    @Override
    protected UpdateAppBean parseResult(ResultBean result) throws Exception {
        UpdateAppBean updateAppBean = new UpdateAppBean();
        updateAppBean.setUpdate(result.getUpdate())
                .setNewVersion(result.getNew_version())
                .setApkFileUrl(result.getApk_file_url())
                .setTargetSize(result.getTarget_size())
                .setUpdateLog(result.getUpdate_log())
                .setConstraint(result.isConstraint())
                .setNewMd5(result.getNew_md5());
        return updateAppBean;
    }

    /**
     *
     * @param updateApp        新版本信息
     * @param updateAppManager app更新管理器
     */
    @Override
    protected void hasNewApp(final UpdateAppBean updateApp, final UpdateAppManager updateAppManager) {
        new MaterialDialog.Builder(updateAppManager.getActivity())
                .title("有新版本啦~")
                .content(updateApp.getUpdateLog())
                .positiveText("更新")
                .negativeText("暂不")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        updateAppManager.download(updateApp,new DownloadService.DownloadCallback() {

                            MaterialDialog downloadDialog;

                            @Override
                            public void onStart() {
                                downloadDialog = new MaterialDialog.Builder(updateAppManager.getActivity())
                                        .title("正在努力下载中")
                                        .content("请求稍后...")
                                        .contentGravity(GravityEnum.CENTER)
                                        .progress(false, 100)
                                        .show();
                            }

                            @Override
                            public void onProgress(float percent,long progress,long totalSize) {
                                downloadDialog.setProgress((int) percent);
                            }

                            @Override
                            public boolean onFinish(File file) {
                                downloadDialog.dismiss();
                                return true;
                            }

                            @Override
                            public void onError(String msg) {

                            }
                        });
                    }
                })
                .show();
    }

    /**
     * 根据自己条件判断是否要升级
     * @param updateApp
     * @param currentVersionCode
     * @return
     */
    @Override
    protected boolean isUpdate(UpdateAppBean updateApp, int currentVersionCode) {
        return !TextUtils.isEmpty(updateApp.getUpdate()) && "Yes".equals(updateApp.getUpdate());
    }
}
