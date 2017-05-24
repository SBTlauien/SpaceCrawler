package com.sbtlauien.spacecrawler;

import android.content.Intent;
import android.os.Environment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

public class WebCrawler implements Runnable {

    private static ArrayList<String> watchList = new ArrayList<>(), whiteList = new ArrayList<>(), blackList = new ArrayList<>(),
            internalLinks = new ArrayList<>(), externalLinks = new ArrayList<>(), finished = new ArrayList<>(), fileExtension = new ArrayList<>();
    private static String mainUrl, userAgent;
    private static int pagesCrawled, watchListCount, internalLinkSpot, errorCount;
    private static boolean atomic = false, crawlExternal = false, useWatchList = false, useWhiteList = false, useBlackList = false;

    public WebCrawler(String url, String ua) {
        mainUrl = url.endsWith("/")?url.substring(0, url.length() - 1).replace("https://www.", "https://").replace("http://www.", "http://").
                toLowerCase():url.replace("https://www.", "https://").replace("http://www.", "http://").toLowerCase();
        userAgent = ua;
        watchList = new ArrayList<>();
        internalLinks = new ArrayList<>();
        externalLinks = new ArrayList<>();
        whiteList = new ArrayList<>();
        blackList = new ArrayList<>();
        finished = new ArrayList<>();
        pagesCrawled = 0;
        watchListCount = 0;
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/WhiteList.txt");
            if(!f.exists()) {
                OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(f));
                outputWriter.flush();
                outputWriter.close();
            }
            BufferedReader readerFile = new BufferedReader(new FileReader(f));
            String currentLine;
            while ((currentLine = readerFile.readLine()) != null) {
                whiteList.add(currentLine);
            }
            if (whiteList.isEmpty()) useWhiteList = false;
            readerFile.close();
            f = new File(Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/BlackList.txt");
            if(!f.exists()) {
                OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(f));
                outputWriter.flush();
                outputWriter.close();
            }
            readerFile = new BufferedReader(new FileReader(f));
            while ((currentLine = readerFile.readLine()) != null) {
                blackList.add(currentLine);
            }
            if (blackList.isEmpty()) useBlackList = false;
            readerFile.close();
            f = new File(Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/WatchList.txt");
            if(!f.exists()) {
                OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(f));
                outputWriter.flush();
                outputWriter.close();
            }
            readerFile = new BufferedReader(new FileReader(f));
            while ((currentLine = readerFile.readLine()) != null) {
                watchList.add(currentLine);
            }
            if (watchList.isEmpty()) useWatchList = false;
            readerFile.close();
        } catch (Exception e) {
            MainActivity.toast(e.getMessage(), true);
        }
    }

    public void run() {
        crawlPage(mainUrl);
    }

    private static void runner(String url) {
        try {
            if (crawlExternal && !externalLinks.isEmpty() && !atomic){
                String prefix;
                prefix = url.startsWith("https")?"https://":"http://";
                finished.add(prefix + new URL(url).getHost());
                internalLinks = new ArrayList<>();
                pagesCrawled = 0;
                prefix = externalLinks.get(0).startsWith("https")?"https://":"http://";
                String host = new URL(externalLinks.get(0)).getHost();
                MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "DOMAIN: " + host));
                for (int i = externalLinks.size() - 1; i > -1; i--){
                    if (externalLinks.get(i).contains(host)){
                        internalLinks.add(externalLinks.get(i));
                        externalLinks.remove(i);
                    }
                }
                MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "FINISHED=" + finished.size()));
                MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + internalLinks.size()));
                MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "EXTERNALPAGES=" + externalLinks.size()));
                crawlPage(prefix + host);
            }
        }catch (Exception e){
            MainActivity.toast(e.getMessage(), true);
        }
    }

    private static void crawlPage(String url) {
        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "CRAWLING: " + url));
        pagesCrawled++;
        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "PAGES=" + pagesCrawled));
        if (useWatchList && withinListItem(url, watchList)) {
            watchListCount++;
            MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "WATCHLIST=" + watchListCount));
            try {
                OutputStreamWriter outputWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/SpaceCrawler/Results.txt", true));
                outputWriter.append(url).append("\n");
                outputWriter.flush();
                outputWriter.close();
            } catch (Exception e) {
                MainActivity.toast(e.getMessage(), true);
            }
        }
        Document doc = null;
        boolean error = false;
        try {
            doc = Jsoup.connect(url).userAgent(userAgent).timeout(5000).get();
        } catch (IOException e) {
            MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "ERROR: " + url + " - " + e.getMessage()));
            errorCount++;
            MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "ERROR=" + errorCount));
            error = true;
        }
        if (!error) {
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                if (atomic) return;
                String linkUrl = link.attr("abs:href").endsWith("/") ? link.attr("abs:href").substring(0, link.attr("abs:href").length() - 1).replace("https://www.", "https://").
                        replace("http://www.", "http://").toLowerCase() : link.attr("abs:href").replace("https://www.", "https://").replace("http://www.", "http://").toLowerCase();
                if (linkUrl.startsWith(mainUrl) && !internalLinks.contains(linkUrl) && !linkUrl.equals("") && !aFile(linkUrl)){
                    if (useBlackList && useWhiteList && !withinListItem(linkUrl, blackList) && withinListItem(linkUrl, whiteList)) {
                        internalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + internalLinks.size()));
                    } else if (useBlackList && !withinListItem(linkUrl, blackList)) {
                        internalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + internalLinks.size()));
                    } else if (useWhiteList && withinListItem(linkUrl, whiteList)){
                        internalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + internalLinks.size()));
                    } else {
                        internalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + internalLinks.size()));
                    }
                } else if (!externalLinks.contains(linkUrl) && !withinListDomain(linkUrl, finished) && !linkUrl.equals("") && !aFile(linkUrl)) {
                    if (useBlackList && useWhiteList && !withinListItem(linkUrl, blackList) && withinListItem(linkUrl, whiteList)) {
                        externalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + externalLinks.size()));
                    } else if (useBlackList && !withinListItem(linkUrl, blackList)) {
                        externalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + externalLinks.size()));
                    } else if (useWhiteList && withinListItem(linkUrl, whiteList)){
                        externalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + externalLinks.size()));
                    } else {
                        externalLinks.add(linkUrl);
                        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "INTERNALPAGES=" + externalLinks.size()));
                    }
                }
            }
        }
        next(url);
    }

    private static void next(String previousUrl){
        if (!internalLinks.isEmpty() && internalLinkSpot < internalLinks.size() && !atomic){
            internalLinkSpot++;
            crawlPage(internalLinks.get(internalLinkSpot));
        } else if (!atomic) {
            runner(previousUrl);
        }
        MainActivity.getActivity().sendBroadcast(new Intent().setAction("LINE_ACTION").putExtra("lineKey", "ALLFINISHED=TRUE"));
    }

    private static boolean withinListDomain(String s, ArrayList<String> al){
        boolean b = false;
        for (String d: al) {
            if (s.contains(d.split("//")[1])) b = true;
        }
        return b;
    }

    private static boolean withinListItem(String s, ArrayList<String> al){
        boolean b = false;
        for (String d: al) {
            if (s.contains(d)) b = true;
        }
        return b;
    }

    private static boolean aFile(String s){
        boolean b = false;
        for (String ex: fileExtension) {
            if (s.endsWith(ex)) b = true;
        }
        return b;
    }

    public static ArrayList getList(int listId) {
        switch (listId){
            case 0: return internalLinks;
            case 1: return externalLinks;
            case 2: return finished;
        }
        return null;
    }

    public static void setUse(int listId, boolean b){
        switch (listId){
            case 0: useWatchList = b; break;
            case 1: useWhiteList = b; break;
            case 2: useBlackList = b; break;
        }
    }

    public static boolean getUse(int listId){
        switch (listId){
            case 0: return useWatchList;
            case 1: return useWhiteList;
            case 2: return useBlackList;
        }
        return false;
    }

    public static void setExtensions(String is){
        String[] sa = is.split("\n");
        for (String s: sa) {
            fileExtension.add(s);
        }
    }

    public static void setAtomic(boolean b){
        atomic = b;
    }

    public static void setCrawlExternal(boolean b){crawlExternal = b;}

    public static boolean getCrawlExternal(){return crawlExternal;}

}