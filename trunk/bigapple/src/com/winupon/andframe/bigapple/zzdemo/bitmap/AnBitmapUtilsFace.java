/* 
 * @(#)AnBitmapUtilsDemo.java    Created on 2013-9-5
 * Copyright (c) 2013 ZDSoft Networks, Inc. All rights reserved.
 * $Id$
 */
package com.winupon.andframe.bigapple.zzdemo.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.winupon.andframe.bigapple.bitmap.AfterClearCacheListener;
import com.winupon.andframe.bigapple.bitmap.AnBitmapUtils;
import com.winupon.andframe.bigapple.bitmap.BitmapDisplayConfig;
import com.winupon.andframe.bigapple.utils.ContextUtils;

/**
 * 从网络加载图片demo,封装单例的AnBitmapUtils实例，类似于一个门面模式，应用程序在使用时，可以屏蔽底层代码<br>
 * 
 * 注意：<br>
 * 1、使用者在使用时请保持单例，这样内存缓存设置的最大阀值才能被限制住<br>
 * 2、创建AnBitmapUtils实例所用的Context请使用Application对象，不要用Activity对象，防止Activity内存泄露<br>
 * 
 * @author xuan
 * @version $Revision: 1.0 $, $Date: 2013-9-5 下午6:49:46 $
 */
public abstract class AnBitmapUtilsFace {
    private static AnBitmapUtils instance;

    /**
     * 注意：请在程序一开始的时候就初始化
     * 
     * @param application
     */
    public static void init(Context application) {
        if (null == instance) {
            instance = new AnBitmapUtils(application);

            // 设置内存最大缓存，可设置其他更多参数
            instance.getGlobalConfig().setMemoryCacheSize(5 * 1024 * 1024);
            instance.getGlobalConfig().setDiskCachePath(ContextUtils.getSdCardPath() + "/xuan/cache");

            // 设置全局默认显示参数，可设置其他更多参数
            instance.getDefaultDisplayConfig().setShowOriginal(false);
        }
    }

    /**
     * 获取单例
     * 
     * @return
     */
    public static AnBitmapUtils getInstance() {
        if (null == instance) {
            throw new RuntimeException("请先初始化AnBitmapUtils实例，方法：在程序启动的时候调用init方法！");
        }

        return instance;
    }

    /**
     * 显示图片
     * 
     * @param context
     * @param imageView
     */
    public static void display(ImageView imageView, String uri) {
        getInstance().display(imageView, uri);
    }

    /**
     * 显示图片
     * 
     * @param imageView
     * @param uri
     * @param displayConfig
     *            显示方式参数配制
     */
    public static void display(ImageView imageView, String uri, BitmapDisplayConfig displayConfig) {
        getInstance().display(imageView, uri, displayConfig);
    }

    /**
     * 可灵活配制一些自定义的显示参数
     * 
     * @param context
     * @param imageView
     * @param uri
     * @param loading
     * @param fail
     */
    public static void display(ImageView imageView, String uri, Bitmap bitmapLoading, Bitmap bitmapFail) {
        BitmapDisplayConfig displayConfig = new BitmapDisplayConfig();
        displayConfig.setLoadingBitmap(bitmapLoading);// 加载中的图片显示
        displayConfig.setLoadFailedBitmap(bitmapFail);// 加载失败的图片显示
        display(imageView, uri, displayConfig);
    }

    // ///////////////////////////////////////////常用清理缓存部分///////////////////////////////////////////////////////////
    /**
     * 清理所有缓存，包括内存和磁盘
     */
    public static void clearCache() {
        getInstance().clearCache();
    }

    /**
     * 清理所有的缓存，包括内存和磁盘的，可设置自己的回调
     * 
     * @param afterClearCacheListener
     *            缓存清理后的回调
     */
    public static void clearCache(AfterClearCacheListener afterClearCacheListener) {
        getInstance().clearCache(afterClearCacheListener);
    }

    /**
     * 清理指定的缓存目标，包括内存的和磁盘的
     * 
     * @param cacheKey
     */
    public static void clearCache(String cacheKey) {
        getInstance().clearCache(cacheKey);
    }

    /**
     * 清理指定的缓存目标，包括内存的和磁盘的
     * 
     * @param cacheKey
     * @param afterClearCacheListener
     */
    public static void clearCache(String cacheKey, AfterClearCacheListener afterClearCacheListener) {
        getInstance().clearCache(cacheKey, afterClearCacheListener);
    }

}
