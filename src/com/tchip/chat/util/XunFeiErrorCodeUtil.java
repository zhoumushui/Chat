package com.tchip.chat.util;

import com.iflytek.cloud.ErrorCode;

public class XunFeiErrorCodeUtil {

	/**
	 * 获取讯飞语音错误描述
	 * 
	 * @param errorCode
	 * @return
	 */
	public static String getErrorDescription(int errorCode) {
		switch (errorCode) {
		case ErrorCode.MSP_ERROR_TIME_OUT:
		case ErrorCode.ERROR_IVW_TIME_OUT:
		case ErrorCode.ERROR_SPEECH_TIMEOUT:
			return "超时";

		case ErrorCode.ERROR_NETWORK_TIMEOUT:
			return "网络超时";
			
		case ErrorCode.ERROR_NO_NETWORK:
			return "无网络链接";
			
		case ErrorCode.ERROR_NET_EXPECTION:
			return "网络异常";
			
		case ErrorCode.ERROR_NO_SPPECH:
			return "未说话";

		case ErrorCode.MSP_ERROR_FAIL:
			return "MSP_ERROR_FAIL";

		case ErrorCode.ERROR_IVW_SPEECH_TOO_SHORT:
			return "说话过短";

		default:
			return "出错了:" + errorCode;
		}
	}

}
