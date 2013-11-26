/**
 * 
 */
package com.evanrichter.visualizer;

import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;


/**
 * @author Evan Richter
 *
 * The Main thread which contains the game loop. The thread must have access to 
 * the surface view and holder to trigger events every game tick.
 */

public class MainThread extends Thread {
	
	private static final String TAG = MainThread.class.getSimpleName();

  // desired fps
    private final static int    MAX_FPS = 50;
    // maximum number of frames to be skipped
    private final static int    MAX_FRAME_SKIPS = 5;
    // the frame period
    private final static int    FRAME_PERIOD = 1000 / MAX_FPS;
 
	
	// Surface holder that can access the physical surface
	private SurfaceHolder surfaceHolder;
	// The actual view that handles inputs
	// and draws to the surface
	private DrawingPanel dPanel;

	// flag to hold game state 
	private boolean running;
	public void setRunning(boolean running) 
	{
		this.running = running;
	}

	public MainThread(SurfaceHolder surfaceHolder, DrawingPanel dPanel) 
	{
		super();
		this.surfaceHolder = surfaceHolder;
		this.dPanel = dPanel;
	}

	@Override
	public void run() 
	{
		Canvas canvas;
		Log.d(TAG, "Starting game loop");
		
		
        long beginTime;     // the time when the cycle begun
        long timeDiff;      // the time it took for the cycle to execute
        int sleepTime;      // ms to sleep (<0 if we're behind)
        int framesSkipped;  // number of frames being skipped
        sleepTime = 0;
 

		
		while (running) 
		{
			canvas = null;
			// try locking the canvas for exclusive pixel editing
			// in the surface
			try 
			{
				canvas = this.surfaceHolder.lockCanvas();
				synchronized (surfaceHolder) 
				{

					beginTime = System.currentTimeMillis();                    
					framesSkipped = 0;  // resetting the frames skipped
				
					// update game state 
					// render state to the screen
					// draws the canvas on the panel
					if (canvas != null) 
					{
						this.dPanel.update();
						this.dPanel.onDraw(canvas);	
					}
					
					// calculate how long did the cycle take
					timeDiff = System.currentTimeMillis() - beginTime;
					// calculate sleep time
					sleepTime = (int)(FRAME_PERIOD - timeDiff);
					 if (sleepTime > 0) {
	                     // if sleepTime > 0 we're OK
	                     try {
	                         // send the thread to sleep for a short period
	                         // very useful for battery saving
	                         Thread.sleep(sleepTime);
	                         } 
	                     catch (InterruptedException e) {}
	                     }
	  
	                     while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
	                         // we need to catch up
	                         this.dPanel.update(); // update without rendering
	                         sleepTime += FRAME_PERIOD;  // add frame period to check if in next frame
	                         framesSkipped++;
	                     }
	  
	                     if (framesSkipped > 0) {
	                         //Log.d(TAG, "Skipped:" + framesSkipped);
	                     }

				}
			}
			finally 
			{
				// in case of an exception the surface is not left in 
				// an inconsistent state
				if (canvas != null) 
				{
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}	// end finally
			dPanel.getData();
		}
	}
	

}
