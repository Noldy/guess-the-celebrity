package com.example.mitta.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    private Button[] buttons;

    private int correctButton;

    ArrayList<String> celebURLS = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        buttons = new Button[4];
        buttons[0] = findViewById(R.id.button);
        buttons[1] = findViewById(R.id.button2);
        buttons[2] = findViewById(R.id.button3);
        buttons[3] = findViewById(R.id.button4);

        DownloadInfoTask infoTask = new DownloadInfoTask();
        String htmlString;
        try {
            htmlString = infoTask.execute("http://www.posh24.se/kandisar").get();

            String[] htmlParts = htmlString.split("channelListEntry");

            for (int part = 1; part < htmlParts.length; part++) {

                Pattern imageUrlPattern = Pattern.compile("src=\"(.*?)\"");
                Matcher imageMatcher = imageUrlPattern.matcher(htmlParts[part]);
                while (imageMatcher.find()) {
                    celebURLS.add(imageMatcher.group(1));
                }

                Pattern nameUrlPattern = Pattern.compile("alt=\"(.*?)\"");
                Matcher nameMatcher = nameUrlPattern.matcher(htmlParts[part]);
                while (nameMatcher.find()) {
                    celebNames.add(nameMatcher.group(1));
                }

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /*
        for(int i = 0; i < celebNames.size();i++){
            System.out.println(celebNames.get(i) + " " + celebURLS.get(i));
        }
        */
        nextQuiz();


    }


    // Downloads the celebrities' name and image urls
    public class DownloadInfoTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection urlConnection;
            String result = "";

            // Read the content of the html
            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;
                    result += current;
                    data = reader.read();

                }



                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Failed URL";
            } catch (IOException e) {
                e.printStackTrace();
                return "Failed IO";
            }


        }

    }


    // Downloads the images
    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream(); //
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    // Sets up the data and the UI for the next quiz
    public void nextQuiz() {

        int nextCelebIndex = generateRandomNumber(celebNames.size());

        // Set the image
        DownloadImageTask imageTask = new DownloadImageTask();
        Bitmap downloadedImage = null;
        try {
            downloadedImage = imageTask.execute(celebURLS.get(nextCelebIndex)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(downloadedImage);

        // Set the names on the buttons
        int nextCelebButton = generateRandomNumber(4);
        correctButton = nextCelebButton;
        for(int i = 0 ; i < buttons.length; i++){

            if(i != nextCelebButton){
                int randomCelebIndex = generateRandomNumber(celebNames.size());
                buttons[i].setText(celebNames.get(randomCelebIndex));
            }
            else if(i == nextCelebButton){
                buttons[i].setText(celebNames.get(nextCelebIndex));
            }

        }

        Log.i("Correct celeb: ", celebNames.get(nextCelebIndex));

    }


    // Evaluates the answer when a button is pressed
    public void onButtonClicked(View view) {

        String answer;
        int buttonTag = Integer.parseInt(view.getTag().toString());

        if(buttonTag == correctButton){
            answer = "correct";
        }
        else{
            answer = "wrong";
        }

        String toastText = "Your guess is " + answer;
        Toast.makeText(getApplicationContext(),toastText,Toast.LENGTH_SHORT).show();
        nextQuiz();

    }

    public int generateRandomNumber(int limit) {

        Random randomGenerator = new Random();
        int randomNumber = randomGenerator.nextInt(limit);
        return randomNumber;
    }
}
