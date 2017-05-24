package com.sbtlauien.spacecrawler;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class Explorer extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
		setContentView(R.layout.viewlist);
		Button backButton = (Button) findViewById(R.id.back);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		ArrayAdapter<String> fileList = new ArrayAdapter<>(this, R.layout.rowwrapped, getIntent().getExtras().getStringArrayList("data"));
		setListAdapter(fileList);
	}

	@Override
	public void onBackPressed() {finish();}

}