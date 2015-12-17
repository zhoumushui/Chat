package com.tchip.chat;

import android.os.Environment;

public interface Constant {
	/**
	 * Debug：打印Log
	 */
	public static final boolean isDebug = true;

	/**
	 * 日志Tag
	 */
	public static final String TAG = "ZMS";

	/**
	 * SharedPreferences名称
	 */
	public static final class MySP {
		/** 名称 **/
		public static final String NAME = "Chat";

	}

	/**
	 * 广播
	 */
	public static final class Broadcast {

	}


	public static final class Module {
		
	}

	/**
	 * 路径
	 */
	public static final class Path {
		/**
		 * SDcard Path
		 */
		public static final String SD_CARD = Environment
				.getExternalStorageDirectory().getPath();

		/**
		 * 字体目录
		 */
		public static final String FONT = "fonts/";

	}

	/**
	 * 讯飞语音SDK
	 */
	public static final String XUNFEI_APP_ID = "5531bef5";

}
