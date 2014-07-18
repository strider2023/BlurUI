package com.touchmenotapps.blurui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Space;

/**
 * http://openweathermap.org/current
 * @author Arindam Nath
 *
 */
public class MainActivity extends Activity {

	private BlurScrollView scrollView;
	private Space mHeaderSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHeaderSpace = (Space) findViewById(R.id.header_space);
		scrollView = (BlurScrollView) findViewById(R.id.scroll_view);
		
		mHeaderSpace.setMinimumHeight((getResources().getDisplayMetrics().heightPixels/2));
		scrollView.setBlurStartPosition((getResources().getDisplayMetrics().heightPixels/3));
		scrollView.setBackgroundResource(R.drawable.rainy);		
	}
}
