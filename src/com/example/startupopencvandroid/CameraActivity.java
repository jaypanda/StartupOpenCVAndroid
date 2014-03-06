package com.example.startupopencvandroid;


import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CameraActivity extends Activity implements SearchOverInterface{
    private static final String TAG = "Sample::Activity";
    private CameraView cameraView;
	private int obj;

	public CameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());        
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        int flagS;
        if(savedInstanceState == null){
			Bundle extras = getIntent().getExtras();
			if( extras == null ){
				flagS = 0;;							
			}else{
				flagS = extras.getInt("flagSource");
			}			
		}else{
			flagS = (Integer)savedInstanceState.getSerializable("flagSource");
		}  
        Log.i("Sample3Native",Integer.toString(flagS));
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        cameraView = new CameraView(this, flagS);
        cameraView.setSearchOverInterface(this);
        cameraView.activityStopped = false;      
                
       addContentView(cameraView, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
       
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent event){
	    if(event.getAction() == MotionEvent.ACTION_DOWN) {
	        // Execute your Runnable after 5000 milliseconds = 5 seconds.
	        cameraView.mBooleanIsPressed = true;
	        
	    }

	    if(event.getAction() == MotionEvent.ACTION_UP) {
	        if(cameraView.mBooleanIsPressed) {
	            cameraView.mBooleanIsPressed = false;

	        }
	    }
		return super.onTouchEvent(event);

	}
	
	@Override
	public void onSearchingDone(boolean success) {
		// TODO Auto-generated method stub
		Log.i("MainActivity","OnDataLoaded");
		if( success == true){
		
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		cameraView.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		cameraView.onResume();
	}
	
	
}
