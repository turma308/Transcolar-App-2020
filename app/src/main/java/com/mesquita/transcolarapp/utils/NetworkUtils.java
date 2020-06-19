package com.mesquita.transcolarapp.utils;

import android.os.AsyncTask;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NetworkUtils extends AsyncTask<String, Void, String>
{

    @Override
    protected String doInBackground(String... params)
    {
        return downloadContent(params[0]);
    }

    private String downloadContent(String urlStr)
    {
        URL url = null;
        String response = null;
        try
        {
            url = new URL(urlStr);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        if (url != null)
        {
            HttpURLConnection urlConnection = null;
            try
            {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                StringBuilder textBuilder = new StringBuilder();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
                    try (Reader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(StandardCharsets.UTF_8.name()))))
                    {
                        int c = 0;
                        while ((c = reader.read()) != -1)
                        {
                            textBuilder.append((char) c);
                        }
                    }
                }
                response = textBuilder.toString();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                urlConnection.disconnect();
            }
        }
        return response.toString();
    }
}
