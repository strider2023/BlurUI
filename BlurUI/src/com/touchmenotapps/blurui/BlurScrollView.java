package com.touchmenotapps.blurui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v8.renderscript.*;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * 
 * @author Arindam Nath
 *
 */
public class BlurScrollView extends ScrollView {

    private int backgroundImage = -1;
    private boolean isImageBlur = false, isOriginal = false;
    private float mMaxThreshold = 0.5f, ratio = 0;
    private Bitmap blurImage;
    private int r, g, b;
    private RenderScript rs;
    private int mBlurStart = 0;
    private Allocation input, output;
    private ScriptIntrinsicBlur script;
    
    /**
     * 
     * @param context
     */
	public BlurScrollView(Context context) {
		super(context);
		init();
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public BlurScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public BlurScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * 
	 */
	private void init() {
		rs = RenderScript.create(getContext());
		script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);		
		ratio = (float) (t - mBlurStart) / getHeight();
		if(ratio > 0) { //Handles over scroll on fling
			if(mMaxThreshold != 0 && ratio < mMaxThreshold) {
				if(isImageBlur) {
					setBlur(ratio * 10);
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
						setBackground(new BitmapDrawable(getResources(), blurImage));
					else
						setBackgroundDrawable(new BitmapDrawable(getResources(), blurImage));
				} else 
					setBackgroundColor(Color.argb((int) (ratio * 255), r, g, b));				
			}
			isOriginal = false;
		} else {
			//On over scroll fling revert to the original value
			//The isOriginal check stops the setting of background again and again
			//once the image has returned to it's original state.
			if(ratio < 0 && !isOriginal) {
				if(isImageBlur) {
					if(backgroundImage != -1)
						setBackgroundResource(backgroundImage);
				} else 
					setBackgroundColor(Color.argb(0, r, g, b));
				isOriginal = true;
			}
		}
	}
		
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(resid);
		this.backgroundImage = resid;
		//Clear the bitmap memory space
		if(blurImage != null) {
			blurImage.recycle();
			blurImage = null;
		}
		//Crop the image so that the aspect ratio doesn't get hampered
		Bitmap originalImage=BitmapFactory.decodeResource(getResources(), resid);
		Bitmap cropImage  = Bitmap.createBitmap(originalImage, 
				0, 0, 
				(originalImage.getWidth() > getResources().getDisplayMetrics().widthPixels) ? getResources().getDisplayMetrics().widthPixels : originalImage.getWidth(), 
				(originalImage.getHeight() > getResources().getDisplayMetrics().heightPixels) ? getResources().getDisplayMetrics().heightPixels : originalImage.getHeight());
		blurImage = Bitmap.createBitmap(cropImage, 0, 0, cropImage.getWidth(), cropImage.getHeight());
		//use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
		input = Allocation.createFromBitmap(rs, blurImage); 
		output = Allocation.createTyped(rs, input.getType());
		//Check the android version and use code likewise
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			setBackground(new BitmapDrawable(getResources(), blurImage));
		else
			setBackgroundDrawable(new BitmapDrawable(getResources(), blurImage));
		isImageBlur = true;
		//Clear memory space for the temporary images
		originalImage.recycle();
		cropImage.recycle();
		originalImage = null;		
		cropImage = null;
	} 
	
	/**
	 * Blur image
	 * @param val
	 */
	private void setBlur(float val) {
		if(blurImage != null) {
			script.setRadius((val > 0) ? val : 1);
			script.setInput(input);
			script.forEach(output);
			output.copyTo(blurImage);
		}
	}
	
	/**
	 * Set the background blur color
	 * @param r - Red component
	 * @param g - Green component
	 * @param b - Blue component
	 */
	public void setBackgroundBlurColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		isImageBlur = false;
	}
	
	/**
	 * Set the maximum blur / color intensity threshold
	 * @param max - Value range starts form 0 
	 * (ends at 1 for colored background only). 
	 */
	public void setMaxBlurThreshold(float max) {
		mMaxThreshold = max;
	}
	
	/**
	 * Set the height from when the background image should start blurring
	 * @param height
	 */
	public void setBlurStartPosition(final int height) {
		mBlurStart = height;
	}
}
