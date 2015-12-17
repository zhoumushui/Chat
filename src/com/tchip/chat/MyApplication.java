package com.tchip.chat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.iflytek.cloud.SpeechUtility;

public class MyApplication extends Application {


	@Override
	public void onCreate() {

		// 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
		// 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
		// 参数间使用“,”分隔。
		try {
			SpeechUtility
					.createUtility(this, "appid=" + Constant.XUNFEI_APP_ID);
		} catch (Exception e) {
			MyLog.e("[MyApplication]SpeechUtility.createUtility: Catch Exception!");
		}
		super.onCreate();

	}

}
