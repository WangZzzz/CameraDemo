package com.example.camerademo;

import com.example.wengcamera.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class PreviewActivity extends Activity{
	private static Bitmap previewBitmap = null;
	public static void launch(Context context,Bitmap bitmap){
		previewBitmap = bitmap;
		Intent intent = new Intent(context,PreviewActivity.class);
		context.startActivity(intent);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
		ImageView previewImage =(ImageView)findViewById(R.id.preview_photo_image);
		previewImage.setImageBitmap(previewBitmap);
	}
}
