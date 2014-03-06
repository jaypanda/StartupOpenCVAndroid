package com.example.startupopencvandroid;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Movie;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class CameraViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "Sample::SurfaceView";

    private Camera              mCamera;
    private SurfaceHolder       mHolder;
    public int                  mFrameWidth;
    public int                  mFrameHeight;
    private byte[]              mFrame;
    private boolean             mThreadRun;
    private byte[]              mBuffer;
    public boolean activityStopped;
    
    public boolean mBooleanIsPressed;


    public CameraViewBase(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
        activityStopped = false;
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }

    @SuppressLint("NewApi")
	public void setPreview() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mCamera.setPreviewTexture( new SurfaceTexture(10) );
        else
        	mCamera.setPreviewDisplay(null);
	}

    @SuppressLint("NewApi")
	public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceCreated");
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            mFrameWidth = width;
            mFrameHeight = height;

            // selecting optimal camera preview size
            {
                int  minDiff = Integer.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - height) < minDiff) {
                        mFrameWidth = size.width;
                        mFrameHeight = size.height;
                        minDiff = Math.abs(size.height - height);
                    }
                }
            }

            params.setPreviewSize(getFrameWidth(), getFrameHeight());
//            Size optPicture = getOptimalPictureSize();
//            params.setPictureSize(optPicture.width, optPicture.height);

            List<String> FocusModes = params.getSupportedFocusModes();
            if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            {
            	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            if( params.isAutoExposureLockSupported() ){
            	params.setAutoExposureLock(true);
            }

            mCamera.setParameters(params);

            
            /* Now allocate the buffer */
            params = mCamera.getParameters();
            int size = params.getPreviewSize().width * params.getPreviewSize().height;
            size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
            mBuffer = new byte[size];
            /* The buffer where the current frame will be coppied */
            mFrame = new byte [size];
            mCamera.addCallbackBuffer(mBuffer);

            /* Notify that the preview is about to be started and deliver preview size */
            onPreviewStarted(params.getPreviewSize().width, params.getPreviewSize().height);
            
			try {
				setPreview();
			} catch (IOException e) {
				Log.e(TAG, "mCamera.setPreviewDisplay/setPreviewTexture fails: " + e);
			}



            /* Now we can start a preview */
            mCamera.startPreview();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mCamera = Camera.open();

        mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                synchronized (CameraViewBase.this) {
                    System.arraycopy(data, 0, mFrame, 0, data.length);
                    CameraViewBase.this.notify(); 
                }
                camera.addCallbackBuffer(mBuffer);
            }
        });
                    
        (new Thread(this)).start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mThreadRun = false;
        if (mCamera != null) {
            synchronized (this) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
        onPreviewStopped();
    }

    private Size getOptimalPictureSize() {
        if (mCamera == null)
            return null;

        List<Size> cameraSizes = mCamera.getParameters()
                .getSupportedPictureSizes();
        Size optimalSize = mCamera.new Size(0, 0);
        double previewRatio = (double) getFrameWidth()/getFrameHeight();

        for (Size size : cameraSizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - previewRatio) > 0.01f)
                continue;
            if (size.height > optimalSize.height) {
                optimalSize = size;
            }
        }

        if (optimalSize.height == 0) {
            for (Size size : cameraSizes) {
                if (size.height > optimalSize.height) {
                    optimalSize = size;
                }
            }
        }
        optimalSize.height = 768;
        optimalSize.width = 1024;
        return optimalSize;
    }
   

    
    protected abstract void onPause();
    protected abstract void onResume();
    /* The bitmap returned by this method shall be owned by the child and released in onPreviewStopped() */
    protected abstract Bitmap processFrame(byte[] data);

    /**
     * This method is called when the preview process is beeing started. It is called before the first frame delivered and processFrame is called
     * It is called with the width and height parameters of the preview process. It can be used to prepare the data needed during the frame processing.
     * @param previewWidth - the width of the preview frames that will be delivered via processFrame
     * @param previewHeight - the height of the preview frames that will be delivered via processFrame
     */
    protected abstract void onPreviewStarted(int previewWidtd, int previewHeight);

    /**
     * This method is called when preview is stopped. When this method is called the preview stopped and all the processing of frames already completed.
     * If the Bitmap object returned via processFrame is cached - it is a good time to recycle it.
     * Any other resourcses used during the preview can be released.
     */
    protected abstract void onPreviewStopped();

    public void run() {
        mThreadRun = true;
        Log.i(TAG, "Starting processing thread");
        while (mThreadRun) {
          while( !activityStopped ){

        	Bitmap bmp = null ;
            String res;

            synchronized (this) {
                try {
                    this.wait();
                    
//                    Log.i("SampleViewBase-run",Integer.toString(mFrame.length));
                    bmp = processFrame(mFrame);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (bmp != null) {

                	Canvas canvas = mHolder.lockCanvas();
                	if (canvas != null) {
                		canvas.drawBitmap(bmp, (canvas.getWidth() - getFrameWidth()) / 2, (canvas.getHeight() - getFrameHeight()) / 2, null);
                		mHolder.unlockCanvasAndPost(canvas);
                	}
                }
            }
          }
        }      
    }
}