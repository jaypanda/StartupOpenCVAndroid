package com.example.startupopencvandroid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.Log;
import android.widget.SlidingDrawer;

class CameraView extends CameraViewBase {
	
	private int mFrameSize;
	private Bitmap mBitmap;
	private int[] mRGBA;
    private int[] mRGBA_overlay;
    private double frametime;

	
	
	ArrayList<SearchOverInterface> listeners = new ArrayList<SearchOverInterface>();


    public CameraView(Context context, int flag) {
        super(context);
    	
    }

	@Override
	protected void onPreviewStarted(int previewWidtd, int previewHeight) {
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		mFrameSize = previewWidtd * previewHeight;
		mRGBA = new int[mFrameSize];
		mRGBA_overlay = new int[mFrameSize];
		mBitmap = Bitmap.createBitmap(previewWidtd, previewHeight, Bitmap.Config.ARGB_8888);

	}

	@Override
	protected void onPreviewStopped() {
		if(mBitmap != null) {
//			mBitmap.recycle();
//			mBitmap = null;
		}
		mRGBA = null;
	
		
		
	}

    @Override
    protected Bitmap processFrame(byte[] data) {
        int[] rgba = mRGBA;
        int[] rgba_overlay = mRGBA_overlay;
        Bitmap bmp = mBitmap; 
        
  	    
       	double fps = 1000/(SystemClock.elapsedRealtime() - frametime);
       	frametime = SystemClock.elapsedRealtime();
       	Log.i("processFrame - fps: ", Double.toString(fps));
        FindFeatures(getFrameWidth(), getFrameHeight(), data, rgba);
        bmp.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
             
		for( SearchOverInterface listener:listeners){
			listener.onSearchingDone(true);
		}
        
        return bmp;
    }

    
	public void setSearchOverInterface( SearchOverInterface listener){
		listeners.add(listener);
	}
    
    public native void FindFeatures(int width, int height, byte yuv[], int[] rgba);


    
    static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("Startup");
    }


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		activityStopped = true;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		activityStopped = false;

	}
	
	

}
