package com.sbtlauien.spacecrawler;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private TextView error, pages, finished, internalPages, externalPages, watchListCount, results;
    private EditText urlData, uaData;
    private static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        setContentView(R.layout.activity_main);
        final LineReceiver lineReceiver = new LineReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("LINE_ACTION");
        registerReceiver(lineReceiver, intentFilter);
        error = (TextView) findViewById(R.id.error);
        pages = (TextView) findViewById(R.id.pages);
        finished = (TextView) findViewById(R.id.finished);
        internalPages = (TextView) findViewById(R.id.internalPages);
        externalPages = (TextView) findViewById(R.id.externalPages);
        watchListCount = (TextView) findViewById(R.id.watchListCount);
        results = (TextView) findViewById(R.id.results);
        urlData = (EditText) findViewById(R.id.url);
        uaData = (EditText) findViewById(R.id.ua);
        startButton = (Button) findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (startButton.getText().equals("START")) {
                    WebCrawler.setAtomic(false);
                    startButton.setText(R.string.stop);
                    new Thread(new WebCrawler(urlData.getText().toString(), uaData.getText().toString()), "WebCrawler").start();
                } else {
                    WebCrawler.setAtomic(true);
                    startButton.setText(R.string.start);
                }
            }
        });
        Button optionsButton = (Button) findViewById(R.id.options);
        optionsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(getApplicationContext(), Options.class));
            }
        });
        Button viewFinishedDomainsButton = (Button) findViewById(R.id.finishedDomains);
        viewFinishedDomainsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (!WebCrawler.getList(2).isEmpty()) {
                    startActivity(new Intent(getApplicationContext(), Explorer.class).putExtra("data", WebCrawler.getList(2)));
                } else {
                    toast("LIST IS EMPTY", false);
                }
            }
        });
        Button viewInternalPagesButton = (Button) findViewById(R.id.viewInternalPages);
        viewInternalPagesButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (!WebCrawler.getList(0).isEmpty()) {
                    startActivity(new Intent(getApplicationContext(), Explorer.class).putExtra("data", WebCrawler.getList(0)));
                } else {
                    toast("LIST IS EMPTY", false);
                }
            }
        });
        Button viewExternalPagesButton = (Button) findViewById(R.id.viewExternalPages);
        viewExternalPagesButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (!WebCrawler.getList(1).isEmpty()) {
                    startActivity(new Intent(getApplicationContext(), Explorer.class).putExtra("data", WebCrawler.getList(1)));
                } else {
                    toast("LIST IS EMPTY", false);
                }
            }
        });
        Button viewWatchlistButton = (Button) findViewById(R.id.viewWatchlist);
        viewWatchlistButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ArrayList<String> al = new ArrayList<>();
                try {
                    File f = new File(Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/Results.txt");
                    if(!f.exists()) {
                        OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(f));
                        outputWriter.flush();
                        outputWriter.close();
                    }
                    BufferedReader readerFile;
                    readerFile = new BufferedReader(new FileReader(f));
                    String currentLine;
                    while ((currentLine = readerFile.readLine()) != null) {
                        al.add(currentLine);
                    }
                    readerFile.close();
                    if (!al.isEmpty()) {
                        startActivity(new Intent(getApplicationContext(), Explorer.class).putExtra("data", al));
                    } else {
                        toast("LIST IS EMPTY", false);
                    }
                } catch (Exception e) {
                    MainActivity.toast(e.getMessage(), true);
                }
            }
        });
        viewWatchlistButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                try {
                    File f = new File(Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/Results.txt");
                    OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(f, false));
                    outputWriter.write("");
                    outputWriter.flush();
                    outputWriter.close();
                    toast("WATCHLIST CLEARED", false);
                } catch (Exception e) {
                    MainActivity.toast(e.getMessage(), true);
                }
                return true;
            }
        });
        Button exitButton = (Button) findViewById(R.id.exit);
        exitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                WebCrawler.setAtomic(true);
                unregisterReceiver(lineReceiver);
                finish();
            }
        });
        activity = this;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        }
        File f = new File(Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler");
        if (!f.exists()){f.mkdir();}
        WebCrawler.setAtomic(false);
        try {
            java.util.Scanner s = new java.util.Scanner(getAssets().open("Extensions")).useDelimiter("\\A");
            WebCrawler.setExtensions(s.hasNext() ? s.next() : "");
        } catch (Exception e){}
    }

    public static void toast(String s, boolean longToast){
        View customToastView = activity.getLayoutInflater().inflate(R.layout.toast, null);
        TextView messageText = (TextView)customToastView.findViewById(R.id.toastMessage);
        messageText.setText(s);
        Toast customToast = new Toast(activity.getApplicationContext());
        customToast.setView(customToastView);
        customToast.setGravity(Gravity.TOP, 0, 20);
        customToast.setDuration(longToast?Toast.LENGTH_LONG:Toast.LENGTH_SHORT);
        customToast.show();
    }

    public static Activity getActivity(){
        return activity;
    }

    private class LineReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1){
            if (arg1.getStringExtra("lineKey").startsWith("ERROR=")) {
                error.setText(arg1.getStringExtra("lineKey").split("ERROR=")[1]);
            } else if (arg1.getStringExtra("lineKey").startsWith("PAGES=")) {
                pages.setText(arg1.getStringExtra("lineKey").split("PAGES=")[1]);
            } else if (arg1.getStringExtra("lineKey").startsWith("FINISHED=")) {
                finished.setText(arg1.getStringExtra("lineKey").split("FINISHED=")[1]);
            } else if (arg1.getStringExtra("lineKey").startsWith("INTERNALPAGES=")) {
                internalPages.setText(arg1.getStringExtra("lineKey").split("INTERNALPAGES=")[1]);
            } else if (arg1.getStringExtra("lineKey").startsWith("EXTERNALPAGES=")){
                externalPages.setText(arg1.getStringExtra("lineKey").split("EXTERNALPAGES=")[1]);
            } else if (arg1.getStringExtra("lineKey").startsWith("WATCHLIST=")){
                watchListCount.setText(arg1.getStringExtra("lineKey").split("WATCHLIST=")[1]);
            } else if (arg1.getStringExtra("lineKey").startsWith("ALLFINISHED=TRUE")){
                if (!startButton.getText().toString().equals("START")){
                    results.append("FINISHED\n");
                    toast("FINISHED CRAWLING", false);
                }
                startButton.setText("START");
            } else {
                if (arg1.getStringExtra("lineKey").startsWith("DOMAIN: ")){
                    results.setText("");
                    pages.setText("0");
                    internalPages.setText("0");
                    externalPages.setText("0");
                    watchListCount.setText("0");
                    urlData.setText(arg1.getStringExtra("lineKey").split("DOMAIN: ")[1]);
                }
                results.append(arg1.getStringExtra("lineKey") + "\n");
            }
        }
    }

}