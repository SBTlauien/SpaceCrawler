package com.sbtlauien.spacecrawler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

public class ViewFile extends AppCompatActivity {

    private TextView file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        setContentView(R.layout.activity_viewfile);
        TextView currentFile = (TextView) findViewById(R.id.currentFile);
        currentFile.setText(getIntent().getExtras().getString("file").split("/")[getIntent().getExtras().getString("file").split("/").length - 1]);
        file = (TextView) findViewById(R.id.file);
        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                try {
                    OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(getIntent().getExtras().getString("file")));
                    outputWriter.append(file.getText().toString());
                    outputWriter.flush();
                    outputWriter.close();
                    MainActivity.toast("SAVED", false);
                } catch (Exception e){
                    MainActivity.toast(e.getMessage(), true);
                }
            }
        });
        Button exitButton = (Button) findViewById(R.id.exit);
        exitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });
        Button clearButton = (Button) findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                file.setText("");
            }
        });
        CheckBox useBox = (CheckBox) findViewById(R.id.use);
        useBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    if (getIntent().getExtras().getString("file").contains("WatchList.txt")){
                        WebCrawler.setUse(0, true);
                    } else if (getIntent().getExtras().getString("file").contains("WhiteList.txt")){
                        WebCrawler.setUse(1, true);
                    } else if (getIntent().getExtras().getString("file").contains("BlackList.txt")){
                        WebCrawler.setUse(2, true);
                    }
                } else {
                    if (getIntent().getExtras().getString("file").contains("WatchList.txt")){
                        WebCrawler.setUse(0, false);
                    } else if (getIntent().getExtras().getString("file").contains("WhiteList.txt")){
                        WebCrawler.setUse(1, false);
                    } else if (getIntent().getExtras().getString("file").contains("BlackList.txt")){
                        WebCrawler.setUse(2, false);
                    }
                }
            }
        });
        if (getIntent().getExtras().getString("file").contains("WatchList.txt")){
            useBox.setChecked(WebCrawler.getUse(0));
        } else if (getIntent().getExtras().getString("file").contains("WhiteList.txt")){
            useBox.setChecked(WebCrawler.getUse(1));
        } else if (getIntent().getExtras().getString("file").contains("BlackList.txt")){
            useBox.setChecked(WebCrawler.getUse(2));
        }
        try {
            File f = new File(getIntent().getExtras().getString("file"));
            if(!f.exists()) {
                OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(f));
                outputWriter.append(file.getText().toString());
                outputWriter.flush();
                outputWriter.close();
            }
            BufferedReader readerFile;
            readerFile = new BufferedReader(new FileReader(f));
            String currentLine;
            while ((currentLine = readerFile.readLine()) != null) {
                file.append(currentLine + "\n");
            }
            readerFile.close();
        } catch (Exception e) {
            MainActivity.toast(e.getMessage(), true);
        }
        if (file.getText().toString().length() > 2) file.setText(file.getText().toString().substring(0, file.getText().toString().length() - 1));
    }

    @Override
    public void onBackPressed() {finish();}

}