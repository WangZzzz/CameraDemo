package com.example.camerademo;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraUtils;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;
import com.example.wengcamera.R;

public class TestCameraFragment extends CameraFragment implements
		OnClickListener, OnSeekBarChangeListener {
	public static final String INTENT_PARAM_USE_FFC = "intent_param_use_ffc";
	public static final String INTENT_PARAM_PICTURE_SIZE = "intent_param_picture_size";
	private boolean isUseFFC;
	private OnSaveImageListener saveImageListener;
	private SeekBar zoom;
	private int MaxProgress = 0;
	private Button change_progress;

	public interface OnSaveImageListener {

		public void onSaveBitmapImage(Bitmap bitmap);

		public void onSaveByteImage(byte[] data);
	}

	public static TestCameraFragment newInstance(boolean useFFC) {
		TestCameraFragment fragment = new TestCameraFragment();
		Bundle args = new Bundle();
		args.putBoolean(INTENT_PARAM_USE_FFC, useFFC);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);

		saveImageListener = (OnSaveImageListener) activity;
		Handler autoFocusHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				autoFocus();
				// Log.e("TestCameraFragment", "autoFocusHandler");
				// sendEmptyMessageDelayed(0, 500);
				Log.e("TestCameraFragment", "isAutoFocusAvailable()=="
						+ isAutoFocusAvailable());
			};
		};
		autoFocusHandler.sendEmptyMessageDelayed(0, 500);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		View cameraView = super.onCreateView(inflater, container,
				savedInstanceState);
		View results = inflater.inflate(R.layout.fragment_camera, container,
				false);
		results.setOnClickListener(this);
		((ViewGroup) results.findViewById(R.id.camera)).addView(cameraView);
		zoom = (SeekBar)results.findViewById(R.id.zoom);
		zoom.setKeepScreenOn(true);
		zoom.setVisibility(View.VISIBLE);
		
		change_progress = (Button) results.findViewById(R.id.change_progress);
/*		change_progress.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				int progress = zoom.getProgress() + 5;
				if(progress <= MaxProgress){
					zoom.setProgress(progress);
				}else{
					zoom.setProgress(3);
				}
				return false;
			}
		});*/
		change_progress.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub				
				int progress = zoom.getProgress() + 1;
				if(progress <= MaxProgress){
					zoom.setProgress(progress);
				}else{
					zoom.setProgress(0);
				}
				return false;
			}
		});

		return results;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		isUseFFC = getArguments().getBoolean(INTENT_PARAM_USE_FFC);

		SimpleCameraHost.Builder builder = new SimpleCameraHost.Builder(
				new WengCameraHost(getActivity()));

//		setHost(builder.build());
		setHost(builder.useFullBleedPreview(true).build());
	}

	class WengCameraHost extends SimpleCameraHost {

		private Camera.Size size;

		public WengCameraHost(Context _ctxt) {
			super(_ctxt);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected boolean useFrontFacingCamera() {
			// TODO Auto-generated method stub
			return isUseFFC;
		}
		
		/*
		 * 使用SingleShotMode
		 */
		@Override
		public boolean useSingleShotMode() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void saveImage(PictureTransaction xact, byte[] image) {
			// TODO Auto-generated method stub
			// super.saveImage(xact, image);
			saveImageListener.onSaveByteImage(image);
		}

		@Override
		public void saveImage(PictureTransaction xact, Bitmap bitmap) {
			// TODO Auto-generated method stub
			// super.saveImage(xact, bitmap);
			saveImageListener.onSaveBitmapImage(bitmap);
		}

		@Override
		public void onCameraFail(FailureReason reason) {
			// TODO Auto-generated method stub
			super.onCameraFail(reason);
			Toast.makeText(getActivity(),
					"Sorry, but you cannot use the camera now!",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public Parameters adjustPictureParameters(PictureTransaction xact,
				Parameters parameters) {
			return super.adjustPictureParameters(xact, parameters);
		}

		@Override
		public Parameters adjustPreviewParameters(Parameters parameters) {
			// TODO Auto-generated method stub
			Log.d(this.getClass().getName(), "adjustPreviewParameters");
			// TODO Auto-generated method stub
			if (doesZoomReallyWork() && parameters.getMaxZoom() > 0) {
				MaxProgress = parameters.getMaxZoom();
				zoom.setMax(MaxProgress);
				zoom.setOnSeekBarChangeListener(TestCameraFragment.this);
			}else {
				zoom.setEnabled(false);
			}
			return super.adjustPreviewParameters(parameters);
		}

		@Override
		public Size getPictureSize(PictureTransaction xact,
				Parameters parameters) {
			// TODO Auto-generated method stub

			Size result = super.getPictureSize(xact, parameters);
			if (size != null) {
				result.width = size.width;
				result.height = size.height;
			}
			return result;
		}

		@Override
		public Size getPreviewSize(int displayOrientation, int width,
				int height, Parameters parameters) {
			// TODO Auto-generated method stub

			size = CameraUtils.getOptimalPreviewSize(displayOrientation, width,
					height, parameters);
			return size;
		}

		/**
		 * 
		 * 未调用时，生成图片的像素为预览可用的最大尺寸
		 * 
		 * If your use of the camera is single-purpose,return STILL_ONLY (for
		 * photos)
		 */
		@Override
		public RecordingHint getRecordingHint() {
			// TODO Auto-generated method stub
			// return RecordingHint.STILL_ONLY;

			RecordingHint rHint = getHost().getDeviceProfile()
					.getDefaultRecordingHint();
			if (rHint == RecordingHint.NONE) {
				rHint = RecordingHint.VIDEO_ONLY;
			}
			return rHint;
		}

		// @Override
		// @TargetApi(16)
		public void onAutoFocus(boolean success, Camera camera) {
			// TODO Auto-generated method stub
			super.onAutoFocus(success, camera);
			if (success) {//自动对焦成功时，取消自动对焦
				// set parameters of camera  
				cancelAutoFocus();
			}
		}
	}

	/**
	 * 
	 * 提供给CameraActivity点击拍照时调用
	 * 
	 */
	public void takeWengPicture() {

		// PictureTransaction xact = new PictureTransaction(getHost());
		// takePicture(xact);

		takePicture(false, true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (isAutoFocusAvailable()) {
			autoFocus();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		Log.d(this.getClass().getName(), "onProgressChanged");
//		if (fromUser) {
		Log.d(this.getClass().getName(), "fromUser");
		zoom.setEnabled(false);
		//设置zoom级别
		zoomTo(zoom.getProgress()).onComplete(new Runnable() {
			@Override
			public void run() {
				zoom.setEnabled(true);
			}
		}).go();
//		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

}
