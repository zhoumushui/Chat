package com.tchip.chat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.tchip.chat.model.OLMusicDownloadProgressListener;
import com.tchip.chat.model.OLMusicFileDownloader;
import com.tchip.chat.model.OLMusicPlayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class OLMusicPlayActivity extends Activity {
	private static final int PROCESSING = 1;
	private static final int FAILURE = -1;

	private EditText pathText; // url地址
	private TextView resultView;
	private Button downloadButton;
	private Button stopButton;
	private ProgressBar progressBar;
	private Button playBtn;
	private OLMusicPlayer player;
	private SeekBar musicProgress;

	private Handler handler = new UIHandler();

	private String downloadUrl;

	private RelativeLayout layoutTop;

	private final class UIHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PROCESSING: // 更新进度
				progressBar.setProgress(msg.getData().getInt("size"));
				float num = (float) progressBar.getProgress()
						/ (float) progressBar.getMax();
				int result = (int) (num * 100); // 计算进度
				resultView.setText(result + "%");
				if (progressBar.getProgress() == progressBar.getMax()) { // 下载完成
					Toast.makeText(getApplicationContext(), "下载完成",
							Toast.LENGTH_LONG).show();
				}
				break;

			case FAILURE: // 下载失败
				Toast.makeText(getApplicationContext(), R.string.error,
						Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		setContentView(R.layout.activity_ol_music_play);


		pathText = (EditText) findViewById(R.id.path);
		resultView = (TextView) findViewById(R.id.resultView);
		downloadButton = (Button) findViewById(R.id.downloadbutton);
		stopButton = (Button) findViewById(R.id.stopbutton);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		MyOnClickListener listener = new MyOnClickListener();
		
		layoutTop = (RelativeLayout) findViewById(R.id.layoutTop);
		layoutTop.setOnClickListener(listener);
		
		downloadButton.setOnClickListener(listener);
		stopButton.setOnClickListener(listener);
		playBtn = (Button) findViewById(R.id.btn_online_play);
		playBtn.setOnClickListener(listener);
		musicProgress = (SeekBar) findViewById(R.id.music_progress);
		player = new OLMusicPlayer(musicProgress);
		musicProgress.setOnSeekBarChangeListener(new SeekBarChangeEvent());

		// 接收搜索内容
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			downloadUrl = extras.getString("downloadUrl");
			new Thread(new Runnable() {

				@Override
				public void run() {
					player.playUrl(downloadUrl);
				}
			}).start();
		}
	}

	class MyOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.layoutTop:
				finish();
				break;
				
			case R.id.downloadbutton: // 开始下载
				// http://abv.cn/music/光辉岁月.mp3，可以换成其他文件下载的链接
				String path = downloadUrl;// pathText.getText().toString();
				String filename = path.substring(path.lastIndexOf('/') + 1);

				try {
					// URL编码（这里是为了将中文进行URL编码）
					filename = URLEncoder.encode(filename, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				path = path.substring(0, path.lastIndexOf("/") + 1) + filename;
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// File savDir =
					// Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
					// 保存路径
					File savDir = Environment.getExternalStorageDirectory();
					download(path, savDir);
				} else {
					Toast.makeText(getApplicationContext(), "没有SD卡",
							Toast.LENGTH_LONG).show();
				}
				downloadButton.setEnabled(false);
				stopButton.setEnabled(true);
				break;
			case R.id.stopbutton: // 暂停下载
				exit();
				Toast.makeText(getApplicationContext(),
						"Now thread is Stopping!!", Toast.LENGTH_LONG).show();
				downloadButton.setEnabled(true);
				stopButton.setEnabled(false);
				break;
			case R.id.btn_online_play:
				new Thread(new Runnable() {

					@Override
					public void run() {
						player.playUrl(downloadUrl);
					}
				}).start();
				break;

			default:
				break;
			}

		}
		
		/*
		 * 由于用户的输入事件(点击button, 触摸屏幕....)是由主线程负责处理的，如果主线程处于工作状态，
		 * 此时用户产生的输入事件如果没能在5秒内得到处理，系统就会报“应用无响应”错误。
		 * 所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件，
		 * 导致“应用无响应”错误的出现。耗时的工作应该在子线程里执行。
		 */
		private DownloadTask task;

		private void exit() {
			if (task != null)
				task.exit();
		}

		private void download(String path, File savDir) {
			task = new DownloadTask(path, savDir);
			new Thread(task).start();
		}

		/**
		 * 
		 * UI控件画面的重绘(更新)是由主线程负责处理的，如果在子线程中更新UI控件的值，更新后的值不会重绘到屏幕上
		 * 一定要在主线程里更新UI控件的值，这样才能在屏幕上显示出来，不能在子线程中更新UI控件的值
		 * 
		 */
		private final class DownloadTask implements Runnable {
			private String path;
			private File saveDir;
			private OLMusicFileDownloader loader;

			public DownloadTask(String path, File saveDir) {
				this.path = path;
				this.saveDir = saveDir;
			}

			/**
			 * 退出下载
			 */
			public void exit() {
				if (loader != null)
					loader.exit();
			}

			OLMusicDownloadProgressListener downloadProgressListener = new OLMusicDownloadProgressListener() {
				@Override
				public void onDownloadSize(int size) {
					Message msg = new Message();
					msg.what = PROCESSING;
					msg.getData().putInt("size", size);
					handler.sendMessage(msg);
				}
			};

			public void run() {
				try {
					// 实例化一个文件下载器
					loader = new OLMusicFileDownloader(getApplicationContext(),
							path, saveDir, 3);
					// 设置进度条最大值
					progressBar.setMax(loader.getFileSize());
					loader.download(downloadProgressListener);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(handler.obtainMessage(FAILURE)); // 发送一条空消息对象
				}
			}
		}

	}

	class SeekBarChangeEvent implements OnSeekBarChangeListener {
		int progress;

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// 原本是(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
			this.progress = progress * player.mediaPlayer.getDuration()
					/ seekBar.getMax();
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
			player.mediaPlayer.seekTo(progress);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (player != null) {
			player.stop();
			player = null;
		}
	}

}
