/* 
 * @(#)ProgressDialogUtils.java    Created on 2012-11-16
 * Copyright (c) 2012 ZDSoft Networks, Inc. All rights reserved.
 * $Id: ProgressDialogUtils.java 34344 2013-01-21 07:40:03Z xuan $
 */
package com.winupon.andframe.bigapple.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

/**
 * 简化一些加载提示框的操作，可能会有一点点的内存溢出问题，即context的引用，这里类就先不要用了，在并发使用时可能有问题
 * 
 * @author xuan
 * @version $Revision: 34344 $, $Date: 2013-01-21 15:40:03 +0800 (周一, 21 一月 2013) $
 */
@Deprecated
public class ProgressDialogUtils {
    private final static ProgressDialogUtils instance = new ProgressDialogUtils();
    private Context context;
    private ProgressDialog progressDialog;

    private ProgressDialogUtils() {
    }

    public static ProgressDialogUtils instance(Context context) {
        if (instance.context != context) {
            instance.context = context;
            instance.progressDialog = new ProgressDialog(context);
        }

        return instance;
    }

    /**
     * 显示
     * 
     * @param title
     */
    public void show(String title) {
        ProgressDialogUtils.show(title, progressDialog);
    }

    /**
     * 显示
     * 
     * @param title
     * @param cancelable
     */
    public void show(String title, boolean cancelable) {
        progressDialog.setTitle(title);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    /**
     * 显示
     * 
     * @param title
     */
    public static void show(String title, ProgressDialog progressDialog) {
        progressDialog.setTitle(title);
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    /**
     * 隐藏
     */
    public void dismiss() {
        ProgressDialogUtils.dismiss(progressDialog);
    }

    /**
     * 隐藏
     */
    public void dismiss(Handler handler) {
        ProgressDialogUtils.dismiss(handler, progressDialog);
    }

    /**
     * 隐藏
     * 
     * @param handler
     */
    public static void dismiss(Handler handler, final ProgressDialog progressDialog) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }

    /**
     * 隐藏
     */
    public static void dismiss(ProgressDialog progressDialog) {
        progressDialog.dismiss();
    }

}
