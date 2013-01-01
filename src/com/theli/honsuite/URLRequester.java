package com.theli.honsuite;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public final class URLRequester extends AsyncTask<URL, Void, String> {
	String postData;
	String script;
	
	public URLRequester(String script)
	{
		this.postData = null;
		this.script = script;
	}
	public URLRequester(String script, String postData)
	{
		this.postData = postData;
		this.script = script;
	}
	
	@Override
	protected String doInBackground(URL... urls) {
		StringBuffer response = new StringBuffer();
		try {
			//URL url = new URL("http://" + Globals.HON_MASTERSERVER + "/"
					//+ script);
			URL url = urls[0];
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			if (postData != null) {
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length",
						"" + Integer.toString(postData.getBytes().length));
				conn.setRequestProperty("Content-Language", "en-US");
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				DataOutputStream wr = new DataOutputStream(
						conn.getOutputStream());
				wr.writeBytes(postData);
				wr.flush();
				wr.close();
			}
			InputStream is = conn.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append(System.getProperty("line.separator"));
			}
			rd.close();
		} catch (Exception e) {
			return null;
		}
		return response.toString();
	}
}
