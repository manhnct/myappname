package hcmut.cse.ten.quickkeyboardviet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.android.inputmethod.latin.SettingsActivity;

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		Button enable_ime = (Button) findViewById(R.id.enable_ime);
		enable_ime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onCreateDialog().show();
			}
		});
		Button choose_ime = (Button) findViewById(R.id.choose_ime);
		choose_ime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (mgr != null) {
					mgr.showInputMethodPicker();
				}
			}
		});
		Button settings_ime = (Button) findViewById(R.id.settings_ime);
		final Intent i = new Intent(this, SettingsActivity.class);
		settings_ime.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(i);
			}
		});
	}

	public AlertDialog onCreateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Back Key")
				.setMessage(
						"Remember to press the back key after you have selected Quick Keyboard.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(
								Settings.ACTION_INPUT_METHOD_SETTINGS));
					}
				});
		return builder.create();

	}

}