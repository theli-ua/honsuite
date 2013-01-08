package com.theli.honsuite;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.inject.Inject;

import roboguice.util.SafeAsyncTask;

public class HoNFileDownloader extends SafeAsyncTask<Integer> {
	
	@Inject ConcurrentLinkedQueue<HoNManifest.ManifestEntry> queue;
	Integer maxThreads;
	Integer totalFiles;
	private final Map<?,?> versionInfo;

	public HoNFileDownloader(Collection<HoNManifest.ManifestEntry> files,
			Integer maxThreads, Map<?,?> versionInfo)
	{
		queue.addAll(files);		
		this.maxThreads = maxThreads;
		this.totalFiles = files.size();
		this.versionInfo = versionInfo;
	}
	@Override
	public Integer call() throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		while(maxThreads --> 0)
		{
			Thread t = new Thread(new Downloader(queue, versionInfo));					
			t.start();
			threads.add(t);
		}
		
		while(!threads.isEmpty())
		{
			threads.get(0).join();
			threads.remove(0);
		}
		
		return 0;
	}
	
	private class Downloader implements Runnable {
		private final Queue<HoNManifest.ManifestEntry> queue;
		private final Map<?,?> versionInfo;
		public Downloader(Queue<HoNManifest.ManifestEntry> q, Map<?,?> versionInfo)
		{
			this.queue = q;
			this.versionInfo = versionInfo;
		}

		@Override
		public void run(){
			while (!queue.isEmpty())
			{
				HoNManifest.ManifestEntry task = queue.poll();
				if (task==null)
					continue;
				
				try{
					URL url = new URL("");
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				}
				catch  (Exception e)
				{
					e.printStackTrace();
				}
				//InputStream is = conn.getInputStream();
				//return Strings.toString(is);
				// TODO Auto-generated method stub
			}
		}
		
	}
}
