package com.theli.honsuite;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import roboguice.util.SafeAsyncTask;
import roboguice.util.Strings;

public abstract class URLRequester extends SafeAsyncTask<String> {
	String postData;
	URL url;

	public URLRequester(URL url) {
		this.postData = null;
		this.url = url;
	}

	public URLRequester(URL url, String postData) {
		this.postData = postData;
		this.url = url;
	}

	@Override
	public String call() throws Exception {
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

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(postData);
			wr.flush();
			wr.close();
		}
		InputStream is = conn.getInputStream();
		return Strings.toString(is);
	}
}
