package com.example.camerademo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.integer;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ZoomButton;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.example.camerademo.TestCameraFragment.OnSaveImageListener;
import com.example.wengcamera.R;

public class CameraActivity extends Activity implements OnSaveImageListener,
		OnClickListener{

	private static final int CAMERA_STATUS_BACK = 0;
	private static final int CAMERA_STATUS_FRONT = 1;
	/** 当前使用的摄像头
	 * 
	 * 默认使用的是后置摄像头
	 * */
	private int currentCameraStatus = CAMERA_STATUS_BACK;

	private TestCameraFragment std = null;
	private TestCameraFragment ffc = null;
	private TestCameraFragment current = null;
	/** 是否有两个摄像头*/
	private boolean hasTwoCameras = (Camera.getNumberOfCameras() > 1);

	private ImageView mFlashlightImageView;
	private ImageView mSwitchCameraImageView;
	private ImageView mTakePictureImageView;
	
	private int screenWidth;
	private int screenHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_camera);
		initCamera(currentCameraStatus);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mFlashlightImageView = (ImageView) findViewById(R.id.camera_pic_flashlight_image);
		mSwitchCameraImageView = (ImageView) findViewById(R.id.camera_pic_frontblack_image);
		mTakePictureImageView = (ImageView) findViewById(R.id.camera_pic_take_image);
		
		this.mTakePictureImageView.setOnClickListener(this);
		this.mFlashlightImageView.setOnClickListener(this);
		this.mSwitchCameraImageView.setOnClickListener(this);
		
		// 获取屏幕的尺寸
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.camera_pic_take_image:
		
			try {
				current.takeWengPicture(); // 拍照
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.camera_pic_flashlight_image:
			switchFlashMode();
			break;
		case R.id.camera_pic_frontblack_image:
			//切换两个camera，前置和后置摄像头
			if (hasTwoCameras) {
				currentCameraStatus = (currentCameraStatus == CAMERA_STATUS_BACK ? CAMERA_STATUS_FRONT
						: CAMERA_STATUS_BACK);
				initCamera(currentCameraStatus);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * FLash切换的顺序为：Auto-Off-On
	 * 
	 * 
	 * @param mode
	 */
	private void switchFlashMode() {
		if (current == null) {
			return;
		}
		String mode = current.getFlashMode();
		if (Camera.Parameters.FLASH_MODE_AUTO.equals(mode)) {
			current.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mFlashlightImageView
					.setImageResource(R.drawable.icon_camera_flashlight_disabled);
		} else if (Camera.Parameters.FLASH_MODE_OFF.equals(mode)) {
			current.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			mFlashlightImageView
					.setImageResource(R.drawable.icon_camera_flashlight);
		} else {
			current.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			mFlashlightImageView
					.setImageResource(R.drawable.icon_camera_flashlight_auto);
		}
	}

	/**
	 * 初始化摄像头
	 * 
	 * @param status
	 */
	private void initCamera(int status) {

		if (CAMERA_STATUS_BACK == status) {
			if (std == null) {
				std = TestCameraFragment.newInstance(false);
			}

			current = std;
		} else {
			if (ffc == null) {
				ffc = TestCameraFragment.newInstance(true);
			}

			current = ffc;
		}

		getFragmentManager().beginTransaction()
				.replace(R.id.camera_pic_container, current)
				.commitAllowingStateLoss();
	}

	@Override
	public void onSaveBitmapImage(Bitmap bitmap) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSaveByteImage(byte[] data) {
		// TODO Auto-generated method stub
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		if (options.outWidth <= 0 || options.outHeight <= 0) { // 图片损坏
			return;
		}

		// DLog.d("wenhao", "options = " +
		// options.outWidth+","+options.outHeight);

		Bitmap bitmap = null;
		//得到照片存储路径
		String filePath = generateTakePictureFilePath();
		options.inJustDecodeBounds = false;
		

		if (options.outWidth <= options.outHeight) {
			options.inSampleSize = (int) (options.outWidth / screenWidth);
		} else {
			options.inSampleSize = (int) (options.outHeight / screenHeight);
		}
		if (options.inSampleSize <= 0) {
			options.inSampleSize = 1;
		}

		Bitmap tmpBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
				options);
		new SaveImageTask(this, tmpBitmap, filePath).execute();
		PreviewActivity.launch(this, tmpBitmap);
	}

	/*
	 * 	照片存储路径
	 */
	public static final String DCIM_PATH = "/DCIM/TestCamera/";

	public static String generateTakePictureFilePath() {
		String directory = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + DCIM_PATH;
		String filePath = directory + generatePictureFilename() + ".jpg";
		return filePath;
	}

	/*
	 * 照片的名称
	 */
	public static String generatePictureFilename() {
		long dateTake = System.currentTimeMillis();
		Date date = new Date(dateTake);
		SimpleDateFormat sdf = new SimpleDateFormat("'weng'_yyyyMMdd_HHmmss");
		String filename = sdf.format(date);
		return filename;
	}
}
