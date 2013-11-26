package com.evanrichter.visualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

class DrawingPanel extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder surfaceHolder;
	private MainThread _thread;
	private Canvas canvas;
	private Paint p = new Paint();
	private GrabAudio grabAudio = null;
	private boolean visible = false;
	private boolean dataExists = false;
	private boolean frequency = true;
	private int displayType = 4;
	private int resolution = 512;
	private int fadeAmt = 100;
	private int cToggle = 0;
	private int cToggle2 = 0;
	private int flashVal = 0;

	private double ax = -100, ay = -100;
	private int y = 0;
	private int[] data;
	private int[] tempData;
	private int[] lagData = new int[50];
	private int[][] locData = new int[50][2];
	private int waveAmt = 1;
	private int[][] waveData = new int[waveAmt][resolution];
	private int[] boxArray = {0,0,0,0,0,0,0,0};
	private double tempAvg = 0;

	public DrawingPanel(Context context, int vType, int cMode, int cMode2, boolean freq) {
		super(context);
		frequency = freq;
		displayType = vType;
		cToggle = cMode;
		cToggle2 = cMode2;
		getHolder().addCallback(this);
		surfaceHolder = getHolder();

	}

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void onDraw(Canvas c) {

		// Background, initial alpha value (fadeAmt) determines fading of previous render,
		// cToggle determines color scheme
		// cToggle 2 determines color of wave, if 1, means wave is red and background is changed
		// to a cream color to accentuate the wave
		
		//try drawARGB(a,r,g,b); to fill screen
		if (cToggle == 0) {
			//p.setColor(Color.argb(fadeAmt, 0, 4, 20));
			c.drawARGB(fadeAmt,0,4,20);
		}
		if (cToggle == 0 && cToggle2 == 1) {
			//p.setColor(Color.argb(fadeAmt, 20, 4, 0));
			c.drawARGB(fadeAmt,20,4,0);
		}
		if (cToggle == 1) {
			//p.setColor(Color.argb(255, 230, 255, 255));
			c.drawARGB(255,230,255,255);
		}
		if (cToggle == 1 && cToggle2 == 1) {
			//p.setColor(Color.argb(255, 255, 231, 200));
			c.drawARGB(255,255,231,200);
		}
		//c.drawRect(0, 0, c.getWidth(), c.getHeight(), p);
		
		// Normal display of frequencies
		if (displayType == 0) { 
			fadeAmt = 200;		
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					double offset = 1 + ((double) i / (double) (resolution / 4));
					c.drawRect(
							((c.getWidth() / 2) + (int) (2 * data[i] * offset)),
							c.getHeight()
									- (int) (i * c.getHeight() / resolution),
							((c.getWidth() / 2) - (int) (2 * data[i] * offset)),
							c.getHeight()
									- (int) (i * c.getHeight() / resolution + c
											.getHeight() / resolution), p);
				}
			}
		}
		// bubble display of frequencies
		if (displayType == 1) {
			fadeAmt = 200;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					double offset = 1 + ((double) i / (double) 50);
					c.drawCircle(c.getWidth() / 2,
							c.getHeight()
									- (int) (i * c.getHeight() / resolution),
							(int) (offset * data[i]), p);
				}
			}
		}
		// pixely raindropy effect
		if (displayType == 2) {
			fadeAmt = 50;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					double offset = 1 + ((double) i / (double) (resolution / 32));
					c.drawRect(
							((c.getWidth() / 2) + (int) (2 * data[i] * offset + offset)),
							c.getHeight() - (int) (i * c.getHeight() / resolution),
							((c.getWidth() / 2) + (int) (2 * data[i] * offset + offset) - 10),
							c.getHeight()- (int) (i * c.getHeight() / resolution
											+ c.getHeight() / resolution + 1),
							p);
				}
			}
		}
		// Rising
		if (displayType == 3) {
			fadeAmt = 100;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					double offset = 1 + ((double) i / (double) (resolution)/5);
					if (cToggle2 == 0) {
						p.setColor(Color.argb((int) Math.abs(offset * 5 * data[i]), (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb((int) Math.abs(offset * 5 * data[i]), 255, (i / 2), 0));
					}
					c.drawRect( 0, c.getHeight() - (int) (i * c.getHeight() / resolution),
								c.getWidth(), c.getHeight() - (int) ((i+1) * c.getHeight() / resolution),
							p);
				}
			}
		}

		// old school display of frequencies
		if (displayType == 4) {
			fadeAmt = 255;
			double width = c.getHeight() / 8;
			if (visible) {
				for (int n = 0; n < 8; n++) {

					int h = 0;
					for (int i = n * (resolution / 8); i < ((n * resolution / 8) + resolution / 8); i++) {
						if (Math.abs(data[i]) > h) {
							h = Math.abs(data[i]);
						}
					}
					if (n == 0) {
						h = h / 4;
					}

					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, 0, 255, 255 - 32 * n));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, 255 - 32 * n, 0));
					}
					
					h = h * 12;
					
					if (h > c.getWidth() - 10){
						h = c.getWidth() - 10;
					}

					c.drawRect(c.getWidth() - h, c.getHeight()
							- (int) (n * (width) + width - 1), (c.getWidth()),
							c.getHeight() - (int) ((n * width)), p);
					
					//White blocks on top

					if (boxArray[n] < h){
						boxArray[n] = h;
					}
					int m = boxArray[n];
					
					c.drawRect((c.getWidth() - 10) - m, c.getHeight() - (int) (n * (width) + width - 1), 
								c.getWidth() - m, c.getHeight() - (int) ((n * width)), p);
					
					if (boxArray[n] > 0){
						boxArray[n] -= 6;
					}
				}
			}
			tempData = data;
		}
		
		//Wavelength view of sound
		if (displayType == 5) {
			fadeAmt = 255;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					c.drawRect(
							((c.getWidth() / 2) + (int) (2 * data[i])),
							c.getHeight()
									- (int) (i * c.getHeight() / resolution),
							((c.getWidth() / 2) - (int) (2 * data[i])),
							c.getHeight()
									- (int) (i * c.getHeight() / resolution + c
											.getHeight() / resolution), p);
				}
			}
		}
		
		//Wavelength Knot
		if (displayType == 6) {
			fadeAmt = 255;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					
					int i2 = 0;
					if (i < 511){
						i2 = i + 1;
					
						double xCo = Math.cos(i * (1.0/4.0));
						double yCo = Math.sin(i * (1.0/4.0));
						double xCo2 = Math.cos(i2 * (1.0/4.0));
						double yCo2 = Math.sin(i2 * (1.0/4.0));
						
						c.drawLine((int)((c.getWidth() / 2) + (2 * xCo * data[i])), 
								   (int)((c.getHeight() / 2) + (2 * yCo * data[i])), 
								   (int)((c.getWidth() / 2) + (2 * xCo2 * data[i2])), 
								   (int)((c.getHeight() / 2) + (2 * yCo2 * data[i2])), p);
					}
				}
			}
		}
		
		//Starburst
		if (displayType == 7) {
			fadeAmt = 255;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					
					int i2 = 0;
					if (i < 255){i2 = i + 1;}
					
					double xCo = Math.cos(i * (360.0/256.0));
					double yCo = Math.sin(i * (360.0/256.0));
					double xCo2 = Math.cos(i * (360.0/256.0));
					double yCo2 = Math.sin(i * (360.0/256.0));
					
					c.drawLine((int)((c.getWidth() / 2) + (2 * xCo * data[i])), 
							   (int)((c.getHeight() / 2) + (2 * yCo * data[i])), 
							   (int)((c.getWidth() / 2) + (2 * xCo2 * data[i2])), 
							   (int)((c.getHeight() / 2) + (2 * yCo2 * data[i2])), p);
					
					//flashVal = Math.abs(data[i]);
				}
			}
		}
		
		//Flower
		if (displayType == 8) {
			fadeAmt = 255;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					
					int i2 = 0;
					if (i < 511){
						i2 = i + 1;
						
						double pos = i * (360.0/512.0);
						double pos2 = i2 * (360.0/512.0);
						
						double xCo = Math.cos(Math.toRadians(pos));
						double yCo = Math.sin(Math.toRadians(pos));
						double xCo2 = Math.cos(Math.toRadians(pos2));
						double yCo2 = Math.sin(Math.toRadians(pos2));
						
						c.drawLine((int)((c.getWidth() / 2) + (4 * xCo * Math.abs(data[i]))), 
								   (int)((c.getHeight() / 2) + (4 * yCo * Math.abs(data[i]))), 
								   (int)((c.getWidth() / 2) + (4 * xCo2 * Math.abs(data[i2]))), 
								   (int)((c.getHeight() / 2) + (4 * yCo2 * Math.abs(data[i2]))), p);
					}
				}
			}
		}	
		//Ring
		if (displayType == 9) {
			fadeAmt = 255;
			if (visible) {
				for (int i = 0; i < resolution+2; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (i / 2), 0));
					}
					
					int i2 = 0;
					
					if (i < 509){
						i2 = i + 1;
					}
					
					double pos = (i * (360.0/(resolution+2))) + 90;
					double pos2 = (i2 * (360.0/(resolution+2))) + 90;
					
					double xCo = Math.cos(Math.toRadians(pos));
					double yCo = Math.sin(Math.toRadians(pos));
					double xCo2 = Math.cos(Math.toRadians(pos2));
					double yCo2 = Math.sin(Math.toRadians(pos2));
					
					if (i < resolution){
					
						c.drawLine((int)((c.getWidth() / 2) + (xCo * (150 + data[i]))), 
								   (int)((c.getHeight() / 2) + (yCo * (150 + data[i]))), 
								   (int)((c.getWidth() / 2) + (xCo2 * (150 + data[i2]))), 
								   (int)((c.getHeight() / 2) + (yCo2 * (150 + data[i2]))), p);
						
						}
					}
			}
		}	
		
		//Plaid
		if (displayType == 10) {
			fadeAmt = 255;
			if (visible) {
				
				int[] tempLagData = new int[50];
				for (int i = 0; i < 50; i++) {
					tempLagData[i] = lagData[i];
				}
				lagData[0] = data[0];
				for (int i = 1; i < 50; i++) {
					lagData[i] = tempLagData[i-1];
				}
				
				//System.out.println(lagData[0] != lagData[50]);

				for (int i = 0; i < 50; i++) {
					
					double m = 255/50;
					
					if (cToggle2 == 0) {
						p.setColor(Color.argb((2 * Math.abs(lagData[i])), (int)(i * m), 255, 255 - (int)(i * m)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb((2 * Math.abs(lagData[i])), 255, (int)(i * m), 0));
					}
					
					double n = i * (((c.getHeight())/2)/(double)50);
					
					c.drawLine(0, (int) (((c.getHeight())/2) + 10 + n), c.getWidth(), (int) (((c.getHeight())/2) + 10 + n), p);
					c.drawLine(0, (int) (((c.getHeight())/2) - 10 - n), c.getWidth(), (int) (((c.getHeight())/2) - 10 - n), p);
					c.drawLine((int) (((c.getWidth())/2) + 10 + n), 0, (int) (((c.getWidth())/2) + 10 + n), c.getHeight(), p);
					c.drawLine((int) (((c.getWidth())/2) - 10 - n), 0, (int) (((c.getWidth())/2) - 10 - n), c.getHeight(), p);
				}
			}
		}
		//Radio
		if (displayType == 11) {
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth((int)(((c.getHeight() * 1.5))/(double)50) - 5);
			fadeAmt = 150;
			if (visible) {
				
				int alphaVal = 0;
				int avg = 0;
				for (int i = 0; i < resolution; i++) {
					avg += Math.abs(data[i]);
				}
				avg /= (resolution * 2);
				if (tempAvg < avg){
					tempAvg = avg;
				}
				else if (tempAvg > 0){
					tempAvg-= 0.3;
				}
				
				int[] tempLagData = new int[50];
				for (int i = 0; i < 50; i++) {
					tempLagData[i] = lagData[i];
				}
				lagData[0] = data[0];
				for (int i = 1; i < 50; i++) {
					lagData[i] = tempLagData[i-1];
				}
				
				for (int i = 0; i < 50; i++) {
					
					double m = 255/50;
					if(tempAvg > i) {
						if (cToggle2 == 0) {
							p.setColor(Color.argb(5 * (Math.abs(lagData[i])), (int)(i * m), 255, 255 - (int)(i * m)));
						}
						if (cToggle2 == 1) {
							p.setColor(Color.argb(5 * (Math.abs(lagData[i])), 255, (int)(i * m), 0));
						}
					}
					else{
						p.setColor(Color.argb(0,0,0,0));
					}
					
				
					double n = i * (((c.getHeight() * 1.5))/(double)50);
					c.drawCircle(c.getWidth(), c.getHeight(), (float) n, p);
				}
			}
		p.setStyle(Paint.Style.FILL);
		}
		
		//Fireworks
		if (displayType == 12) {
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(10);
			fadeAmt = 200;
			if (visible) {
				
				int[] tempLagData = new int[50];
				for (int i = 0; i < 50; i++) {
					tempLagData[i] = lagData[i];
				}
				lagData[0] = data[0];
				for (int i = 1; i < 50; i++) {
					lagData[i] = tempLagData[i-1];
				}
				
				int[][] tempLocData = new int[50][2];
				for (int i = 0; i < 50; i++) {
					tempLocData[i][0] = locData[i][0];
					tempLocData[i][1] = locData[i][1];
				}
				
				if (Math.abs(data[0]) > 20 + Math.abs(lagData[1]) && Math.abs(data[0]) > 20 + Math.abs(lagData[2])){
					locData[0][0] = (int) (Math.random() * c.getWidth());
					locData[0][1] = (int) (Math.random() * c.getHeight());
				}
				else{
					locData[0][0] = -10000;
					locData[0][1] = -10000;
				}
				
				for (int i = 1; i < 50; i++) {
					locData[i][0] = tempLocData[i-1][0];
					locData[i][1] = tempLocData[i-1][1];
				}

				for (int i = 0; i < 50; i++) {
					
					double m = 255/50;
					
					int alphaValue = (4 * (Math.abs(lagData[i]))) - (i*5);
					if (alphaValue < 0){
						alphaValue = 0;
					}

					if (cToggle2 == 0) {
						p.setColor(Color.argb(alphaValue, (int)(i * m), 255, 255 - (int)(i * m)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(alphaValue, 255, (int)(i * m), 0));
					}
				
					double n = i * (((c.getHeight())/2)/(double)50);
					c.drawCircle(locData[i][0], locData[i][1], (float) n, p);
				}
			}
		p.setStyle(Paint.Style.FILL);
		}
		
		//Tunnel
		if (displayType == 13) {
			fadeAmt = 255;
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(3);
			if (visible) {
				
				int[] tempLagData = new int[50];
				for (int i = 0; i < 50; i++) {
					tempLagData[i] = lagData[i];
				}
				lagData[0] = data[0];
				for (int i = 1; i < 50; i++) {
					lagData[i] = tempLagData[i-1];
				}
				
				//System.out.println(lagData[0] != lagData[50]);

				for (int i = 0; i < 50; i++) {
					
					double m = 255/50;
					
					if (cToggle2 == 0) {
						p.setColor(Color.argb((4 * Math.abs(lagData[i])), (int)(i * m), 255, 255 - (int)(i * m)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb((4 * Math.abs(lagData[i])), 255, (int)(i * m), 0));
					}
					
					double n = 1+((50-i)/50.0)*((50-i) * (((c.getHeight())/2)/(double)50));
					
					c.drawLine((int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) + n),
							   (int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) + n), p);
					c.drawLine((int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) - n),
							   (int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) - n), p);
					
					c.drawLine((int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) - n), 
							   (int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) + n + 1), p);
					c.drawLine((int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) - n), 
							   (int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) + n), p);
				}
			}
			
			p.setStyle(Paint.Style.FILL);
		}
		//Fuzz
		if (displayType == 14) {
			fadeAmt = 255;
			if (visible) {
				for (int i = 0; i < resolution; i++) {
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255 - (i/2), (i / 2), 255, 255 - (i / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255 - (i/2), 255, (i / 2), 0));
					}
					int i2 = 0;
					
					if (i < 511){
						i2 = i + 1;
					}
					
					double pos = (i * (360.0/512.0)) + 90;
					double pos2 = (i2 * (360.0/512.0)) + 90;
					
					double xCo = Math.cos(Math.toRadians(pos));
					double yCo = Math.sin(Math.toRadians(pos));
					double xCo2 = Math.cos(Math.toRadians(pos2));
					double yCo2 = Math.sin(Math.toRadians(pos2));
					
					int val1 = (c.getHeight() / 4) + data[i];// * (1 + i/15);
					int val2 = (c.getHeight() / 4) + data[i2];// * (1 + i/15);
					
					if (i == 0){
						c.drawLine((int)((c.getWidth() / 2) + (xCo * val1)), 
								   (int)((c.getHeight() / 2) - (c.getHeight() / 4) + (yCo * val1)), 
								   (int)((c.getWidth() / 2) - (xCo * val1)), 
								   (int)((c.getHeight() / 2) + (c.getHeight() / 4) - (yCo * val1)), p);
					}
					
					c.drawLine((int)((c.getWidth() / 2) + (xCo * val1)), 
							   (int)((c.getHeight() / 2) - (c.getHeight() / 4) + (yCo * val1)), 
							   (int)((c.getWidth() / 2) + (xCo2 * val2)), 
							   (int)((c.getHeight() / 2) - (c.getHeight() / 4) + (yCo2 * val2)), p);
					
					c.drawLine((int)((c.getWidth() / 2) - (xCo * val1)), 
							   (int)((c.getHeight() / 2) + (c.getHeight() / 4) - (yCo * val1)), 
							   (int)((c.getWidth() / 2) - (xCo2 * val2)), 
							   (int)((c.getHeight() / 2) + (c.getHeight() / 4) - (yCo2 * val2)), p);
					
				}
			}
		}
		// Spiral
		if (displayType == 15) {fadeAmt = 255;
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(3);
		if (visible) {
			
			int[] tempLagData = new int[50];
			for (int i = 0; i < 50; i++) {
				tempLagData[i] = lagData[i];
			}
			lagData[0] = data[0];
			for (int i = 1; i < 50; i++) {
				lagData[i] = tempLagData[i-1];
			}
			
			//System.out.println(lagData[0] != lagData[50]);

			for (int i = 0; i < 50; i++) {
				
				double m = 255/50;
				
				if (cToggle2 == 0) {
					p.setColor(Color.argb((4 * Math.abs(lagData[i])), (int)(i * m), 255, 255 - (int)(i * m)));
				}
				if (cToggle2 == 1) {
					p.setColor(Color.argb((4 * Math.abs(lagData[i])), 255, (int)(i * m), 0));
				}
				
				double n = 1+(i/50.0)*(i * (((c.getHeight())*0.6)/(double)50));
				
				c.drawLine((int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) + n),
						   (int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) + n), p);
				c.drawLine((int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) - n),
						   (int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) - n), p);
				
				c.drawLine((int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) - n), 
						   (int) (((c.getWidth())/2) + n), (int) (((c.getHeight())/2) + n + 1), p);
				c.drawLine((int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) - n), 
						   (int) (((c.getWidth())/2) - n), (int) (((c.getHeight())/2) + n), p);
				c.rotate((int)(180.0/50.0), c.getWidth()/2, c.getHeight()/2);
			}
		}
		
		p.setStyle(Paint.Style.FILL);
		}
		
		// Ascension
		if (displayType == 16) {fadeAmt = 255;
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(4);
		if (visible) {
			
			int[] tempLagData = new int[50];
			for (int i = 0; i < 50; i++) {
				tempLagData[i] = lagData[i];
			}
			lagData[0] = data[0];
			for (int i = 1; i < 50; i++) {
				lagData[i] = tempLagData[i-1];
			}
			
			//System.out.println(lagData[0] != lagData[50]);

			for (int i = 0; i < 50; i++) {
				
				double m = 255/50;
				
				if (cToggle2 == 0) {
					p.setColor(Color.argb((4 * Math.abs(lagData[i])), (int)(i * m), 255, 255 - (int)(i * m)));
				}
				if (cToggle2 == 1) {
					p.setColor(Color.argb((4 * Math.abs(lagData[i])), 255, (int)(i * m), 0));
				}
				//POINT WHERE ITS COOL
				double n = 1+(i/50.0)*(i * (c.getHeight()/(double)50));
				
				c.drawLine( 0, (int) (c.getHeight() - n),
						   c.getWidth(), (int) (c.getHeight() - n), p);
			}
		}
		
		p.setStyle(Paint.Style.FILL);
		}
		
		// Line
		if (displayType == 17) {fadeAmt = 255;
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth((c.getHeight()/50));
		if (visible) {
			
			int[] tempLagData = new int[50];
			for (int i = 0; i < 50; i++) {
				tempLagData[i] = lagData[i];
			}
			lagData[0] = data[0];
			for (int i = 1; i < 50; i++) {
				lagData[i] = tempLagData[i-1];
			}
			
			for (int i = 0; i < 50; i++) {
				
				double m = 255/50;
				
				if (cToggle2 == 0) {
					p.setColor(Color.argb((4 * Math.abs(lagData[i])), (int)(i * m), 255, 255 - (int)(i * m)));
				}
				if (cToggle2 == 1) {
					p.setColor(Color.argb((4 * Math.abs(lagData[i])), 255, (int)(i * m), 0));
				}
				double n = (i * (c.getHeight()/(double)50));
				
				c.drawLine( 0, (int) (c.getHeight() - n),
						   c.getWidth(), (int) (c.getHeight() - n), p);
			}
		}
		
		p.setStyle(Paint.Style.FILL);
		}
		
		// Big Ess
		if (displayType == 18) {
		fadeAmt = 150;
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(3);
		if (visible) {
			
			int[] tempLagData = new int[50];
			for (int i = 0; i < 50; i++) {
				tempLagData[i] = lagData[i];
			}
			
			lagData[0] = data[0];
			if (Math.abs(data[0]) > 15 + Math.abs(lagData[1]) && Math.abs(data[0]) > 15 + Math.abs(lagData[2])){
				lagData[0] = 0;
			}
			for (int i = 1; i < 50; i++) {
				lagData[i] = tempLagData[i-1];
				
			}
			
			
			
			for (int i = 0; i < 50; i++) {
				
				double m = 255/50;
				
				int alphaValue = (4 * (Math.abs(lagData[i])));
				if (alphaValue < 0){
					alphaValue = 0;
				}

				if (cToggle2 == 0) {
					p.setColor(Color.argb(alphaValue, (int)(i * m), 255, 255 - (int)(i * m)));
				}
				if (cToggle2 == 1) {
					p.setColor(Color.argb(alphaValue, 255, (int)(i * m), 0));
				}
				
				int r = (int) (c.getWidth() / 10.0);
				int xVal = r + r*2*((i)/10);
				int yVal = r + r*2*((i)%10);
				c.drawCircle(xVal, yVal, r - 5, p);
			}
		}
		
		p.setStyle(Paint.Style.FILL);
		}
		

		//Waves coming in
		if (displayType == 19) {
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(2);
			fadeAmt = 255;
			if (visible) {
				
				int[][] tempWaveData = new int[waveAmt][resolution];
				for (int i = 0; i < waveAmt; i++) {
					tempWaveData[i] = waveData[i];
				}

				waveData[0] = data;
				
				for (int i = 1; i < waveAmt; i++) {
					waveData[i] = tempWaveData[i-1];
				}
					
				for (int j = 0; j < resolution; j++) {
					
					if (cToggle2 == 0) {
						p.setColor(Color.argb(255, (j / 2), 255, 255 - (j / 2)));
					}
					if (cToggle2 == 1) {
						p.setColor(Color.argb(255, 255, (j / 2), 0));
					}
					
					
					int j2 = 0;
					if (j < 511){
						j2 = j + 1;
					}
					
					
					float yCo = (float) (j * ((double)c.getHeight() / (double)resolution));
					float xCo = (c.getWidth()/2) + waveData[0][j];
					float yCo2 = (float) ((j+1) * ((double)c.getHeight() / (double)resolution));
					float xCo2 = (c.getWidth()/2) + waveData[0][j2];
					
					c.drawLine(xCo,yCo,xCo2,yCo2,p); 
					
				}
					
				
			}
			p.setStyle(Paint.Style.FILL);

		}	
		//Staggered Circles, looks like a 3d cone from the side, radius controlled by frequency values
	}

	public void getData() {
		if (visible && grabAudio != null) {
			while (!dataExists) {
				try {
					data = grabAudio.getFormattedData(1, 1);
					tempData = data;
					dataExists = true;
				} catch (NullPointerException e) {
				}
			} // end if
		} // end while
		dataExists = false;
	}

	public void update() {
	}

	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			ax = event.getX();
			ay = event.getY();
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			ax = event.getX();
			ay = event.getY();
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
		}

		// update();

		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// update();
		visible = true;
		if (grabAudio == null){
			if (frequency){
				grabAudio = new GrabAudio(1, resolution, 0);
			}
			else {
				grabAudio = new GrabAudio(0, resolution, 0);
			}
		}
		grabAudio.start();
		getData();
		// setWillNotDraw(false); //Allows us to use invalidate() to call
		// onDraw()

		_thread = new MainThread(getHolder(), this); // Start the thread that
		_thread.setRunning(true); // will make calls to
		_thread.start(); // onDraw()
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		if (grabAudio != null) {
			grabAudio.stop();
			grabAudio.release();
			grabAudio = null;
		}
		try {
			_thread.setRunning(false); // Tells thread to stop
			_thread.join(); // Removes thread from memory.
		} catch (InterruptedException e) {
		}
	}

}