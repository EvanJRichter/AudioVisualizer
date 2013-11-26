package com.evanrichter.visualizer;

import com.evanrichter.visualizer.R;
import com.heyzap.sdk.ads.HeyzapAds;
import com.heyzap.sdk.ads.InterstitialOverlay;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static int cMode = 0;
	private static int cMode2 = 0;
	private Spinner spinner;
	private String[] spinnerList;
	private String visSelected;
	private int visType = 0;
	private int frequency = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		//HeyzapAds.start(this);					//Full screen ads
		//InterstitialOverlay.display(this);
		Spinner spinner = (Spinner) findViewById(R.id.visualizer_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.visualizer_types,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				int index = arg0.getSelectedItemPosition();
				// storing string resources into Array
				spinnerList = getResources().getStringArray(R.array.visualizer_types);
				visSelected = spinnerList[index];
			}
			
			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onCheckboxClicked(View view) {
		if (cMode == 0) {
			cMode = 1;
		} else {
			cMode = 0;
		}
	}

	public void onCheckboxClicked2(View view) {
		if (cMode2 == 0) {
			cMode2 = 1;
		} else {
			cMode2 = 0;
		}
	}

	/** Called when the user clicks the Start Visualizer button */
	public void newVisualizer(View view) {
		if (visSelected.equals("Waves")){visType = 19; frequency = 1;}
		if (visSelected.equals("Rain")){visType = 18; frequency = 0;}
		if (visSelected.equals("Line")){visType = 17; frequency = 0;}
		if (visSelected.equals("Ascension")){visType = 16; frequency = 0;}
		if (visSelected.equals("Spiral")){visType = 15; frequency = 0;}
		if (visSelected.equals("Unity")){visType = 14; frequency = 0;}
		if (visSelected.equals("Tunnel")){visType = 13; frequency = 1;}
		if (visSelected.equals("Fireworks")){visType = 12; frequency = 1;}
		if (visSelected.equals("Radio")){visType = 11; frequency = 1;}
		if (visSelected.equals("Plaid")){visType = 10; frequency = 1;}
		if (visSelected.equals("Ring")){visType = 9; frequency = 1;}
		if (visSelected.equals("Flower")){visType = 8; frequency = 1;}
		if (visSelected.equals("Starburst")){visType = 7; frequency = 1;}
		if (visSelected.equals("Knot")){visType = 6; frequency = 1;}
		if (visSelected.equals("Wavelength")){visType = 5; frequency = 1;}
		if (visSelected.equals("Old School")){visType = 4; frequency = 0;}
		if (visSelected.equals("Rising")){visType = 3; frequency = 0;}
		if (visSelected.equals("Digital")){visType = 2; frequency = 0;}
		if (visSelected.equals("Paint Drops")){visType = 1; frequency = 0;}
		if (visSelected.equals("Frequency")){visType = 0; frequency = 0;}
		
		Intent intent = new Intent(this, NewVisualizerActivity.class);
		intent.putExtra("VISUALIZER_TYPE", visType);
		intent.putExtra("COLOR_MODE", cMode);
		intent.putExtra("COLOR_MODE2", cMode2);
		intent.putExtra("FREQUENCY", frequency);
		startActivity(intent);
	}
}
