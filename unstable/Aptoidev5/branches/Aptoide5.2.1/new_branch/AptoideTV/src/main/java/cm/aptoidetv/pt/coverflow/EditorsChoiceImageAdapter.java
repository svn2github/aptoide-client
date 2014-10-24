package cm.aptoidetv.pt.coverflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cm.aptoide.ptdev.fragments.Home;
import cm.aptoide.ptdev.fragments.HomeItem;
import cm.aptoidetv.pt.R;

public class EditorsChoiceImageAdapter extends BaseAdapter {
	private Context mContext;

	private FileInputStream fis;

	private ArrayList<Home> mImageUrls;

//	private ArrayList<ImageView> mImages;

	public EditorsChoiceImageAdapter(Context c, ArrayList<Home> items) {
		mContext = c;
		this.mImageUrls = items;
	}
	
	public Bitmap getBitmapFromURL(String src) {
		Bitmap image;
	    try {
	        URL url = new URL(src);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        image = BitmapFactory.decodeStream(input);
	        return image;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return BitmapFactory.decodeResource(mContext.getResources(), android.R.drawable.sym_def_app_icon);
	    }
	}
	
//	private Bitmap decodeFile(String src){
//	    try {
//	        //Decode image size
//	        BitmapFactory.Options o = new BitmapFactory.Options();
//	        o.inJustDecodeBounds = true;
//	        BitmapFactory.decodeStream(new FileInputStream(src),null,o);
//	        
//	        //The new size we want to scale to
//	        final int REQUIRED_SIZE=70;
//
//	        //Find the correct scale value. It should be the power of 2.
//	        int scale=1;
//	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
//	            scale*=2;
//
//	        //Decode with inSampleSize
//	        BitmapFactory.Options o2 = new BitmapFactory.Options();
//	        o2.inSampleSize=scale;
//	        return BitmapFactory.decodeStream(new FileInputStream(src), null, o2);
//	    } catch (FileNotFoundException e) {
//	    	Log.d("EditorsChoiceImage", "Not found");
//	    	e.printStackTrace();
//	    } catch (IOException e) {
//			e.printStackTrace();
//		}
//	    return BitmapFactory.decodeResource(mContext.getResources(), android.R.drawable.sym_def_app_icon);
//	}

//	private Bitmap decodeFile(String src){
//	    Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), android.R.drawable.sym_def_app_icon);
//	    try {
//	    	URL url = new URL(src);
//	    	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//	    	connection.setDoInput(true);
//	    	connection.connect();
//	    	InputStream input = connection.getInputStream();
//
//	    	//Decode image size
//	    	BitmapFactory.Options o = new BitmapFactory.Options();
//	        o.inJustDecodeBounds = true;
//
////	        FileInputStream fis = new FileInputStream(input);
//	        b = BitmapFactory.decodeStream(input, null, o);
//	        input.close();
//
//	        int scale = 1;
//	        int IMAGE_MAX_SIZE = 500;
//			if (o.outHeight > IMAGE_MAX_SIZE  || o.outWidth > IMAGE_MAX_SIZE) {
//	            scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / 
//	               (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
//	        }
//
//	        //Decode with inSampleSize
//	        BitmapFactory.Options o2 = new BitmapFactory.Options();
//	        o2.inSampleSize = scale;
//	        fis = new FileInputStream(src);
//	        b = BitmapFactory.decodeStream(fis, null, o2);
//	        fis.close();
//	    } catch (IOException e) {
//	    	Log.d("EditorsChoice","Not found");
//	    }
//		
//		ImageLoader.getInstance().loadImage(mContext, src, new ImageLoadingListener() {
//			
//			@Override
//			public void onLoadingStarted() {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onLoadingFailed(FailReason failReason) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void onLoadingComplete(Bitmap loadedImage) {
//				
//			}
//			
//			@Override
//			public void onLoadingCancelled() {
//				// TODO Auto-generated method stub
//				
//			}
//		})
//		
//	    return b;
//	}
	
	public int getCount() {
		return mImageUrls.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) { return (((HomeItem)mImageUrls.get(position)).getId()); }

	public View getView(int position, View convertView, ViewGroup parent) {

		//Use this code if you want to load from resources
		//ImageView i = new ImageView(mContext);
		//i.setImageResource(mImageIds[position]);
		//i.setLayoutParams(new CoverFlow.LayoutParams(130, 130));
		//i.setScaleType(ImageView.ScaleType.MATRIX);	        
		//return i;
		convertView = new ImageView(mContext);

        final DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true).displayer(new FadeInBitmapDisplayer(1000)).build();
        convertView.setLayoutParams(new CoverFlow.LayoutParams(750, 350));
//		((ImageView) convertView).setScaleType(ScaleType.MATRIX);
		((ImageView) convertView).setImageResource(R.drawable.icon_non_available);
//		ImageLoader.getInstance().displayImage(mImageUrls.get(position).get("url"), (ImageView) convertView,options);
        ImageLoader.getInstance().displayImage(((HomeItem) mImageUrls.get(position)).getIcon(), (ImageView) convertView, options);

		return convertView;
	}
	/** Returns the size (0.0f to 1.0f) of the views 
	 * depending on the 'offset' to the center. */ 
	public float getScale(boolean focused, int offset) { 
		/* Formula: 1 / (2 ^ offset) */ 
		return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset))); 
	} 

}

