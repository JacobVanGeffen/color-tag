package com.lahacks.colormatch;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {

    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Camera cam;

    @SuppressWarnings("deprecation")
	public CameraPreview(Context context) {
        super(context);

        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		cam = Camera.open();
		cam.release();
    }

    public void setCamera(Camera camera) {
        if (cam == camera) { return; }
        
        cam.stopPreview();
        cam.release();
        
        cam = camera;
        
        if (cam != null) {
            //List<Size> localSizes = cam.getParameters().getSupportedPreviewSizes();
            requestLayout();
          
            try {
                cam.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
          
            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            cam.startPreview();
        }
    }
    
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Now that the size is known, set up the camera parameters and begin
	    // the preview.
	    Camera.Parameters parameters = cam.getParameters();
	    parameters.setPreviewSize(width, height);
	    requestLayout();
	    cam.setParameters(parameters);

	    // Important: Call startPreview() to start updating the preview surface.
	    // Preview must be started before you can take a picture.
	    cam.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {}

}
