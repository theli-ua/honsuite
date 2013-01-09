package com.theli.honsuite;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.AsyncTask;

import roboguice.util.Strings;

public class HoNFileDownloader extends AsyncTask<Void,Integer,Integer> {
	
	Queue<HoNManifest.ManifestEntry> queue;
	final Integer maxThreads;
	final Integer totalFiles;
	final File outputDirectory;
	private final Map<?,?> versionInfo;

	public HoNFileDownloader(Collection<HoNManifest.ManifestEntry> files,
			Integer maxThreads, Map<?,?> versionInfo, File outputDirectory)
	{
		queue = new ConcurrentLinkedQueue<HoNManifest.ManifestEntry>();
		queue.addAll(files);		
		this.maxThreads = maxThreads;
		this.totalFiles = files.size();
		this.versionInfo = versionInfo;
		this.outputDirectory = outputDirectory;
	}
	@Override
	protected Integer doInBackground(Void... arg0) {
		List<Thread> threads = new ArrayList<Thread>();
		int threadCount = this.maxThreads;
		while(threadCount --> 0)
		{
			Thread t = new Thread(new Downloader(queue, getVersionInfo(), this.outputDirectory));					
			t.start();
			threads.add(t);
		}
		
		while(!threads.isEmpty())
		{
			try {
				threads.get(0).join(1000);
			} catch (Exception e)
			{
				
			}
			if(!threads.get(0).isAlive())
				threads.remove(0);
		}
		
		return 0;
	}
	
	public Map<?,?> getVersionInfo() {
		return versionInfo;
	}

	private class Downloader implements Runnable {
		private final Queue<HoNManifest.ManifestEntry> queue;
		private final Map<?,?> versionInfo;
		private final File outdir;
		public Downloader(Queue<HoNManifest.ManifestEntry> q, Map<?,?> versionInfo,File outputDirectory)
		{
			this.queue = q;
			this.versionInfo = versionInfo;
			this.outdir = outputDirectory;
		}
		
		private String normalizeVersion(String v)
		{
			String[] bits = v.split("\\.");
			if (bits[bits.length - 1].equals("0"))
			{
				return Strings.join(".", Arrays.copyOf(bits, bits.length - 1));
			}
			return v;
		}

		@Override
		public void run(){
			Map<?,?> vI = (Map<?,?>)versionInfo.get(0);
			String url_prefix = String.format("%s%s/%s/", (String)vI.get("url"),(String)vI.get("os"),
					(String)vI.get("arch"));
			String url2_prefix = String.format("%s%s/%s/", (String)vI.get("url2"),(String)vI.get("os"),
					(String)vI.get("arch"));
			while (!queue.isEmpty())
			{
				HoNManifest.ManifestEntry task = queue.poll();
				if (task==null)
					continue;
				
				try{
					URL url = new URL(String.format("%s%s/%s",url_prefix,
							normalizeVersion(task.version), task.path)); 
					URL url2 = new URL(String.format("%s%s/%s",url2_prefix,
							normalizeVersion(task.version), task.path)); 
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					if(conn.getResponseCode() == 404)
					{
						conn = (HttpURLConnection)url2.openConnection();
					}
					InputStream input = new BufferedInputStream(conn.getInputStream());
					File out = new File(this.outdir,task.path + ".zip");
					out.getParentFile().mkdirs();
					OutputStream output = new FileOutputStream(out);
					byte data[] = new byte[1024];
					int count;
					while ((count = input.read(data)) != -1) {
						output.write(data, 0, count);
					}
					output.close();
				}
				catch  (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}

}
