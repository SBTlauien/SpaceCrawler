package com.sbtlauien.spacecrawler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class Options extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        setContentView(R.layout.activity_options);
        Button linkWatchListButton = (Button) findViewById(R.id.linkWatchList);
        linkWatchListButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ViewFile.class).putExtra("file", Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/LinkWatchList.txt"));
            }
        });
        Button sourceWatchListButton = (Button) findViewById(R.id.sourceWatchList);
        sourceWatchListButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ViewFile.class).putExtra("file", Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/SourceWatchList.txt"));
            }
        });
        Button whiteListButton = (Button) findViewById(R.id.whiteList);
        whiteListButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ViewFile.class).putExtra("file", Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/WhiteList.txt"));
            }
        });
        Button blackListButton = (Button) findViewById(R.id.blackList);
        blackListButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), ViewFile.class).putExtra("file", Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/BlackList.txt"));
            }
        });
        Button exitButton = (Button) findViewById(R.id.exit);
        exitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
        CheckBox crawlExternalBox = (CheckBox) findViewById(R.id.crawlExternalOption);
        crawlExternalBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    WebCrawler.setCrawlExternal(buttonView.isChecked()?true:false);
            }
        });
        crawlExternalBox.setChecked(WebCrawler.getCrawlExternal());
    }

}