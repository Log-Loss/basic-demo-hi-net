package com.example.demo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PostScoreIterListener implements IterationListener {
    private int printIterations = 10;
    private static final Logger log = LoggerFactory.getLogger(ScoreIterationListener.class);
    private boolean invoked = false;
    private long iterCount = 0L;

    public PostScoreIterListener(int printIterations) {
        this.printIterations = printIterations;
    }

    public PostScoreIterListener() {
    }

    public boolean invoked() {
        return this.invoked;
    }

    public void invoke() {
        this.invoked = true;
    }

    public void iterationDone(Model model, int iteration) {
        if (this.printIterations <= 0) {
            this.printIterations = 1;
        }

        if (this.iterCount % (long)this.printIterations == 0L) {
            this.invoke();
            double result = model.score();
//            log.info("Score at iteration " + this.iterCount + " is " + result);
            try {
                sendPost("http://localhost:8888/post", new BasicNameValuePair("score", result+""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ++this.iterCount;
    }

    public static void sendPost(String url, BasicNameValuePair pair) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(pair);
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                // do something useful
            } finally {
                instream.close();
            }
        }
    }
}