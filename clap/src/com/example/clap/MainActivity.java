package com.example.clap;

import java.io.FileNotFoundException;
import android.os.CountDownTimer;

import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;


import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import be.hogent.tarsos.dsp.onsets.*;

public class MainActivity extends Activity implements AudioProc.OnAudioEventListener, PitchDetectionHandler, OnsetHandler, SurfaceHolder.Callback {

	private static final int SAMPLE_RATE = 16000;
	private AudioProc mAudioProc;
	private PitchProcessor mPitchProcessor;
	private Button mListenButton;
	//private static final String TAG = "PitchTrackerActivity";
	
	private TextView countdown;
    private SurfaceHolder holder;
    private Camera cam;
    
    private android.hardware.Camera.PictureCallback pictureBitmapCallback;
    private android.hardware.Camera.PictureCallback pictureJPEGCallback;
    private android.hardware.Camera.ShutterCallback shutterCallback =
    		new android.hardware.Camera.ShutterCallback() {

        public void onShutter()
        {
            ((AudioManager)getSystemService("audio")).playSoundEffect(4);
        }

    };
    private android.view.SurfaceHolder.Callback surfaceCallback;
    private SurfaceView sv;
    private Uri u;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sv = (SurfaceView) findViewById(R.id.surfaceView1);
        holder = sv.getHolder();
        holder.addCallback(this);
        countdown = (TextView)findViewById(R.id.textView1);

		
		mAudioProc = new AudioProc(SAMPLE_RATE);
		setPitchProcessor();
		mAudioProc.setOnAudioEventListener(this);
		mListenButton = (Button)findViewById(R.id.listen);
		
		
		/*Button buttonTakePicture = (Button)findViewById(R.id.button1);
        buttonTakePicture.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//cam.takePicture(myShutterCallback, 
					//	myPictureCallback_RAW, myPictureCallback_JPG);
				(new CountDownTimer(5000L, 500L) {


		            public void onFinish()
		            {
		            	cam.takePicture(myShutterCallback, 
								myPictureCallback_RAW, myPictureCallback_JPG);
		            	countdown.setText("");
		            }

		            public void onTick(long l)
		            {
		                countdown.setText((new StringBuilder()).append(l / 1000L).toString());
		            }

		        }).start();
			}});*/
		
		mListenButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!clapMode && !isOn) {
					mListenButton.setBackground(getResources().getDrawable(R.drawable.apc));
					takePicture();
				} else {
				if (mAudioProc.isRecording()) {
					mListenButton.setBackground(getResources().getDrawable(R.drawable.apb));
					mAudioProc.stop();
					//mListenButton.setText("Listen");
				} else {
					mAudioProc.listen();
					//mListenButton.setText("Stop listening");
					mListenButton.setBackground(getResources().getDrawable(R.drawable.apc));
				}
			}
			}
		});
		det = new PercussionOnsetDetector(SAMPLE_RATE, mAudioProc.getBufferSize() / 2, this, 30, 12);
	}

	public void setPitchProcessor() {
		PitchProcessor.PitchEstimationAlgorithm alg = 
				PitchProcessor.PitchEstimationAlgorithm.AMDF;
		mPitchProcessor = new PitchProcessor(alg, SAMPLE_RATE, mAudioProc.getBufferSize(), this);
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.switch_cam:
	    	if (previewing) {
	    		cam.stopPreview();
	    		previewing = false;
	    	}
            cam.release();
            if (camId == 0) {
            	camId = 1;
            } else {
            	camId = 0;
            }
            cam = Camera.open(camId);
            try
            {
                cam.setPreviewDisplay(holder);
            }
            catch (IOException ioexception)
            {
                ioexception.printStackTrace();
            }
            previewing = true;
            cam.startPreview();
            return true;
	    case R.id.clap:
	    	clapMode = true;
	    	Toast.makeText(MainActivity.this, 
					"Click on the Aperture button - Clap twice to start Countdown", 
					Toast.LENGTH_LONG).show();
	    	return true;
	    case R.id.click:
	    	Toast.makeText(MainActivity.this, 
					"Click on the Aperture to start Countdown", 
					Toast.LENGTH_LONG).show();
	    	clapMode = false;
	    	return true;
	    default:
            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void processAudioProcEvent(AudioEvent ae) {
		// detect pitch
		//mPitchProcessor.process(ae);
		det.process(ae);
	}

	@Override
	public void handlePitch(final PitchDetectionResult pitchDetectionResult,
			AudioEvent audioEvent) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (pitchDetectionResult.isPitched()) {
					//mPitchBox.setText(String.valueOf(pitchDetectionResult.getPitch()) + " Hz");
				} else {
					//mPitchBox.setText("No pitch detected");
				}
			
			}
			
		});
	}
	
	@Override
	public void handleOnset(double time, double s) {
		System.out.println("IM HERE!");
		double temp = time - prev;
		final double t = temp;
		if (temp <= 2.5) {
			System.out.println("IM INSIDE!");
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					//mPitchBox.setText(String.valueOf(t));
					takePicture();
					System.out.println("Two claps");
				}
				
			});
		} else {
			prev = time;
		}
		System.out.println("NOW IM HERE!");
	}
	double prev = -400;
	private PercussionOnsetDetector det;
	
	
    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k)
    {
        cam = Camera.open(camId);
        try
        {
            cam.setPreviewDisplay(surfaceholder);
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
        previewing = true;
        cam.startPreview();
    }
    
    public void surfaceCreated(SurfaceHolder surfaceholder)
    {
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder)
    {
        cam.stopPreview();
        cam.release();
    }
    
    
    
    ShutterCallback myShutterCallback = new ShutterCallback(){

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			
		}};
		
	PictureCallback myPictureCallback_RAW = new PictureCallback(){

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			// TODO Auto-generated method stub
			
		}};
		
	PictureCallback myPictureCallback_JPG = new PictureCallback(){

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			// TODO Auto-generated method stub
			/*Bitmap bitmapPicture 
				= BitmapFactory.decodeByteArray(arg0, 0, arg0.length);	*/
			
			Uri uriTarget = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, new ContentValues());

			OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(uriTarget);
				imageFileOS.write(arg0);
				imageFileOS.flush();
				imageFileOS.close();
				
				mAudioProc.stop();
				//mListenButton.setText("Listen");
				/*Toast.makeText(MainActivity.this, 
						"Image saved: " + uriTarget.toString(), 
						Toast.LENGTH_LONG).show();*/
                Intent intent = new Intent(MainActivity.this, ImgShow.class);
                intent.putExtra("image", arg0);
                intent.putExtra("ur", uriTarget.toString());
                System.out.println("Hi!!!!");
                startActivity(intent);
				mListenButton.setBackground(getResources().getDrawable(R.drawable.apb));
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//mListenButton.setVisibility(View.GONE);
			//cam.startPreview();
		}};

    public void takePicture() {
    	(new CountDownTimer(5000L, 500L) {


            public void onFinish()
            {
            	cam.takePicture(myShutterCallback, 
						myPictureCallback_RAW, myPictureCallback_JPG);
            	countdown.setText("");
            }

            public void onTick(long l)
            {
                countdown.setText((new StringBuilder()).append(l / 1000L).toString());
            }

        }).start();
    	
    }
    
    private boolean previewing = false;
    private int camId = 0;
    private boolean clapMode = true;
    boolean isOn = false;
    
}