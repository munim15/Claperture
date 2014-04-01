package com.example.clap;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class ImgShow extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width1 = size.x;
        height1 = size.y;
        _scratch = rez(BitmapFactory.decodeByteArray(intent.getByteArrayExtra("image"), 0, getIntent().getByteArrayExtra("image").length));
        uri = Uri.parse(intent.getStringExtra("ur"));
        setContentView(new Panel(this));
        //getActionBar().show();
    }
    public Bitmap rez(Bitmap bm) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) width1) / width;
	    float scaleHeight = ((float) height1) / height;
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix matrix = new Matrix();
	    // RESIZE THE BIT MAP
	    matrix.postScale(scaleWidth, scaleHeight);

	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}
    static int counter;
    Bitmap _scratch;
    Uri uri;
    int width1, height1;
    Intent intent;
    class Panel extends View {
        public Panel(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            //Bitmap _scratch = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(_scratch, 10, 10, null);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		//MenuInflater inflater = getMenuInflater();
		//inflater.inflate(R.menu.main, menu);
		//viewMenu = menu.findItem(R.id.color);
    	//System.out.println("AAAAAAACTIOOOOON BAAAAR");
    	getMenuInflater().inflate(R.menu.imgmenu, menu);
		return true;
	}
    
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    
	    case R.id.share:
	    	Intent shareIntent = new Intent(Intent.ACTION_SEND);
    		shareIntent.setType("image/jpeg");
    		/*String path = Environment.getExternalStorageDirectory().toString();
	    	  OutputStream fOut = null;
	    	  try {
	    	  File file = new File("/mnt/sdcard/", "sharing"+".JPEG");
	    	  fOut = new FileOutputStream(file);

	    	  _scratch.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
	    	  fOut.flush();
	    	  fOut.close();
	    	  counter += 1;
	    	  MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
	    	  } catch (Exception e) {
	              Log.e("Error", e.toString());
	          }
    		File o = new File("/mnt/sdcard/", "sharing.JPEG");*/
    		shareIntent.putExtra("android.intent.extra.STREAM", uri);
    		
    		this.startActivity(Intent.createChooser(shareIntent , "Share options"));
    		return true;
	    case R.id.save:
	    	OutputStream imageFileOS;
			try {
				imageFileOS = getContentResolver().openOutputStream(uri);
				imageFileOS.write(intent.getByteArrayExtra("image"));
				imageFileOS.flush();
				imageFileOS.close();
				Toast.makeText(this, 
						"Image saved: " + uri.toString(), 
						Toast.LENGTH_LONG).show();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return true;
	    default:
            return super.onOptionsItemSelected(item);
	    }
	}
}