package com.example.razli.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    ImageView imageView;
    Button button0, button1, button2, button3;
    Bitmap mCurrentCelebrityImage;
    String websiteHtml;
    ArrayList<String> mCelebrityNameList;
    ArrayList<String> mUrlList;
    Random random;
    int celebrityIndex = 0;
    int positionOfCorrectAnswer;
    int noOfRemovedCelebs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        mCelebrityNameList = new ArrayList<String>();
        mUrlList = new ArrayList<String>();
        random = new Random();

        // Get HTML from website
        downloadHtml();

        // Extract Celebrity Names and respective Image URL
        // Then stores them in 2 separate ArrayLists
        extractNamesAndUrlFromHtml();

        // Log the 2 ArrayLists
        Log.i(TAG, "buttonPressed: " + mCelebrityNameList);
        Log.i(TAG, "buttonPressed: " + mUrlList);

        setUpQuestion();
    }

    public void setUpQuestion() {

        // Get random celebrity
        celebrityIndex = random.nextInt(70) - noOfRemovedCelebs;

        // Switches image according to celebrityIndex
        downloadImage(mUrlList.get(celebrityIndex));

        // Put correct ans in random button
        positionOfCorrectAnswer = random.nextInt(4);

        int range = 70 - noOfRemovedCelebs;

        button0.setText(mCelebrityNameList.get(random.nextInt(range)));
        button1.setText(mCelebrityNameList.get(random.nextInt(range)));
        button2.setText(mCelebrityNameList.get(random.nextInt(range)));
        button3.setText(mCelebrityNameList.get(random.nextInt(range)));

        switch(positionOfCorrectAnswer) {
            case 0: button0.setText(mCelebrityNameList.get(celebrityIndex)); break;
            case 1: button1.setText(mCelebrityNameList.get(celebrityIndex)); break;
            case 2: button2.setText(mCelebrityNameList.get(celebrityIndex)); break;
            case 3: button3.setText(mCelebrityNameList.get(celebrityIndex)); break;
            default: break;
        }
    }

    public void buttonPressed(View view) {

        // Check if user pressed correct one. Create Toast
        // 5 means nothing has been pressed
        int buttonSelected = 5;

        switch(view.getTag().toString()) {
            case "0": buttonSelected = 0; break;
            case "1": buttonSelected = 1; break;
            case "2": buttonSelected = 2; break;
            case "3": buttonSelected = 3; break;
            default: break;
        }

        if(positionOfCorrectAnswer == buttonSelected) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong..", Toast.LENGTH_SHORT).show();
        }

        //celebrityIndex++;     don't use this anymore as index is random

        // Remove element so that it cant be repeated again
        Log.i(TAG, "setUpQuestion: Removed: " + mCelebrityNameList.get(celebrityIndex) + " at " + celebrityIndex);
        mCelebrityNameList.remove(celebrityIndex);
        mUrlList.remove(celebrityIndex);
        noOfRemovedCelebs++;

        setUpQuestion();
    }

    public void extractNamesAndUrlFromHtml() {

        // Extract Names
        Pattern p = Pattern.compile("alt=\"(.*?)\"/>");
        Matcher m = p.matcher(websiteHtml);

        while(m.find()) {
            mCelebrityNameList.add(m.group(1));
        }

        // Extract URLs for Images
        p = Pattern.compile("<img src=\"(.*?)\" alt=");
        m = p.matcher(websiteHtml);

        while(m.find()) {
            mUrlList.add(m.group(1));
        }
    }

    public void downloadImage(String imageUrl) {

        ImageDownloader task = new ImageDownloader();

        try {
            mCurrentCelebrityImage = task.execute(imageUrl).get();
            imageView.setImageBitmap(mCurrentCelebrityImage);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void downloadHtml() {
        HtmlDownloader htmlDownloader = new HtmlDownloader();

        try {
            websiteHtml = htmlDownloader.execute("http://www.posh24.se/kandisar").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                InputStream in = urlConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);

                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class HtmlDownloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while(data != -1) {
                    char currentChar = (char) data;
                    result += currentChar;

                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
