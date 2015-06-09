/**
 *  WengWeng
 *  Created by wenhao on Oct 18, 2014.
 *  Copyright (c) 2014年 mafengwo. All rights reserved.
 */
package com.example.camerademo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

public class SaveImageTask extends AsyncTask<Void, Void, Void> {

	private Bitmap bitmap;
	private String filePath;
	private Context context;
	private boolean saveStatus;
	public SaveImageTask(Context context, Bitmap bitmap, String filePath) {

		this.context = context;
		this.bitmap = bitmap;
		this.filePath = filePath;
	}

	@Override
	protected Void doInBackground(Void... params) {
		
		if(bitmap == null || filePath == null){
			saveStatus = false;
			return null;
		}
		saveStatus = storeCapturedImage(bitmap,
				filePath);
		scaneImageFile(this.context, filePath);
		return null;
	}
	
	public static boolean storeCapturedImage(Bitmap bm, String filePath) {
        if (bm == null || filePath == null)
            return false;
        OutputStream outputStream = null;
        try {
            File file = new File(filePath);
            File dir = file.getParentFile();
            if (!dir.exists())
                dir.mkdirs();
            outputStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 92, outputStream);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		if (!saveStatus) {
			Toast.makeText(context, "嗡嗡图片存储失败!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 
	 * 发送媒体文件扫描广播,更新相册
	 * 
	 * @param context
	 * @param path
	 */
	public static void scaneImageFile(Context context, String path) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(new File(path));
		intent.setData(uri);
		context.sendBroadcast(intent);
	}
}
