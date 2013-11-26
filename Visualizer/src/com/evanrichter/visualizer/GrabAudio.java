/*
* This file is part of DeadMau5 Audio Visualizer.
*
* DeadMau5 Audio Visualizer is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License.
*
* DeadMau5 Audio Visualizer is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with DeadMau5 Audio Visualizer.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.evanrichter.visualizer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

public class GrabAudio {

        private final String TAG = "DeadMau5";
        private byte[] mRawData;
        private int[] mFormattedData;
        private int mType;
        private Visualizer mVisualizer;
        private Method mSnoop;
        private boolean mUseSnoop = false;
        private long silencePeriod;
        private Random randomGen = new Random();
        private int[] calculatedData;
        private Queue<Integer> queue = new LinkedList<Integer>();
        private int[] pcmData;
        private int size;
        
        public static final int PCM_TYPE = 0;
        public static final int FFT_TYPE = 1;
        
        public GrabAudio(int type, int size, int audioSession) {
                this.size = size;
                if(Build.VERSION.SDK_INT < 9)
                        mUseSnoop = true;
                
                
                
                silencePeriod  = SystemClock.uptimeMillis();
                
                pcmData = new int[size];
                for (int i = 0; i < size; i++)
                        pcmData[i] = (int)(randomGen.nextFloat() * 60 - 30);
                
                /*for(int i = 0; i < pcmData.length; i++)
                        queue.offer(pcmData[i]);*/
                
                mFormattedData = new int[size];
                calculatedData = new int[size];
                 
                if(!mUseSnoop) {
                        int range[] = new int[2];
                        
                        range = Visualizer.getCaptureSizeRange();
                        //Log.v(TAG, Integer.toString(range[0]) + "-" + Integer.toString(range[1]));
                        mType = type;
        
                if (size < range[0]) {
                    size = range[0];
                }
                if (size > range[1]) {
                    size = range[1];
                }
                mRawData = new byte[size];
                        if (type == FFT_TYPE)
                                size /= 2;
                        
                mVisualizer = null;
                try {
                    mVisualizer = new Visualizer(audioSession);
                    if (mVisualizer != null) {
                        if (mVisualizer.getEnabled()) {
                            mVisualizer.setEnabled(false);
                        }
                        mVisualizer.setCaptureSize(mRawData.length);
                    }
                } catch (UnsupportedOperationException e) {
                    Log.v(TAG, "UnsupportedOperationException");
                } catch (IllegalStateException e) {
                    Log.v(TAG, "IllegalStateException");
                } catch (RuntimeException e) {
                    Log.v(TAG, "RuntimeException");
                }
                }
        }
        
         public void start() {
                 if(!mUseSnoop) {
                if (mVisualizer != null) {
                    try {
                        if (!mVisualizer.getEnabled()) {
                            mVisualizer.setEnabled(true);
                        }
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "start() IllegalStateException");
                    }
                }
                 }
         }
         
         public void stop() {
                 if(!mUseSnoop) {
                if (mVisualizer != null) {
                    try {
                        if (mVisualizer.getEnabled()) {
                            mVisualizer.setEnabled(false);
                        }
                    } catch (IllegalStateException e) {
                        Log.v(TAG, "stop() IllegalStateException");
                    }
                }
                 }
         }
         
         public void release() {
                 if(!mUseSnoop) {
                if (mVisualizer != null) {
                    mVisualizer.release();
                    mVisualizer = null;
                }
                 }
         }
         
         public byte[] getRawData() {
                captureData();
                return mRawData;
            }

         public int[] getFormattedData(int num, int den) {
                 if (num == 0)
                         num += 1;
                 if(!mUseSnoop) {
                         captureData();
                         if (mType == PCM_TYPE) {
                                 for (int i = 0; i < mFormattedData.length; i++) {
                                         int tmp = ((int)mRawData[i] & 0xFF) - 128;
                                         mFormattedData[i] = (tmp * num) / den;
                                 }
                         } else {
                                 for (int i = 0; i < mFormattedData.length; i++) {
                                         mFormattedData[i] = ((int)mRawData[i] * num) / den;
                                 }
                         }
                 }
                 else {
                         short[] temp = new short[calculatedData.length];
                         if(snoop(temp, 0) != 1)
                                 for(int i = 0; i < temp.length; i++) {
                                         calculatedData[i] = ((int)temp[i] * num) / 100;
                                         mFormattedData = calculatedData;
                                 }
                         else {
                                 Arrays.fill(calculatedData, 0);
                                 mFormattedData = calculatedData;
                         }
                 }
                 
                 if(mFormattedData[0] == 0 && SystemClock.uptimeMillis() - silencePeriod >= 2000) {
                         mFormattedData = getFakeData(num, den);
                 }
         else if (mFormattedData[0] != 0){
                 silencePeriod = SystemClock.uptimeMillis();
         }
         
                 return mFormattedData;
         }
        
         public void captureData() {
                int status = Visualizer.ERROR;
                try {
                        if(mType == PCM_TYPE)
                                status = mVisualizer.getWaveForm(mRawData);
                        else
                                status = mVisualizer.getFft(mRawData);
                } catch(Exception e) {
                        
                } finally {
                        if (status != Visualizer.SUCCESS) {
                                Log.v(TAG, Integer.toString(status));
                        if (mType == PCM_TYPE)
                            Arrays.fill(mRawData, (byte)0x80);
                        else
                            Arrays.fill(mRawData, (byte)0);
                        }
                }
         }
         
         private static int customParseInt( final String s )
         {
             // Check for a sign.
             int num  = 0;
             int sign = -1;
             final int len  = s.length( );
             final char ch  = s.charAt( 0 );
             if ( ch == '-' )
                 sign = 1;
             else
                 num = '0' - ch;

             // Build the number.
             int i = 1;
             while ( i < len )
                 num = num*10 + '0' - s.charAt( i++ );

             return sign * num;
         } 

         private int snoop(short[] outData, int kind)
         {    
             if ( mSnoop != null )
             {
                 try
                 {
                     return customParseInt( (mSnoop.invoke( MediaPlayer.class , outData, kind)).toString() );
                 }
                 catch ( Exception e )
                 {
                     Log.e( TAG, "Failed to MediaPlayer.snoop()!", e );
                     return 1;
                 }
             }
             else
             {
                 try {
                     Class c = MediaPlayer.class;
                     Method m = c.getMethod("snoop", outData.getClass(), Integer.TYPE);
                     m.setAccessible(true);
                     mSnoop = m;
                     return customParseInt( (m.invoke(c, outData, kind)).toString() ); 
                 } 
                 catch (Exception e) 
                 {
                     Log.e( TAG, "Failed to MediaPlayer.snoop()!", e );
                     return 1;
                 }
             }
         }
         
         private int[] getFakeData(int num, int den) {
                 //fakeData(num, den);
                 for (int i = 0; i < size; i++)
                         mFormattedData[i] = 0;
                 return mFormattedData;
         }
         
         private void fakeData(int num, int den) {
                 int temp = 0;
                 for(int i = 0; i < mFormattedData.length; i++) {
                         temp = queue.poll();
                         mFormattedData[i] = 0;
                         queue.offer(temp);
                 }
         }
         
         private int random(int a) {
                 return randomGen.nextInt(a);
         }
         
}