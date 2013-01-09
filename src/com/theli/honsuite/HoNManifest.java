package com.theli.honsuite;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class HoNManifest {
	public static class ManifestEntry
	{
		public String checksum;
		public int size;
		public String path;
		public String version;
		public ManifestEntry(String checksum, String path, String version, int size)
		{
			this.path = path;
			this.checksum = checksum;
			this.version = version;
			this.size = size;
		}
		public int hashCode()
		{
			return (37 * this.size + path.hashCode()) * 37 + version.hashCode();			
		}
		public boolean equals(Object o) { 
			ManifestEntry e = (ManifestEntry)o;
			return e.path.equals(this.path) 
					&& e.version.equals(this.version) 
					&& e.checksum.equals(this.checksum); 
		}
	}
	Map<String,ManifestEntry> files;
	String os;
	String arch;
	String version;
	protected void parse(XmlPullParser parser) throws XmlPullParserException, IOException
	{
		HashMap<String,ManifestEntry> map = new HashMap<String, ManifestEntry>();
	    parser.require(XmlPullParser.START_TAG, null, "manifest");
	    os = parser.getAttributeValue(null, "os");
	    arch = parser.getAttributeValue(null, "arch");
	    version = parser.getAttributeValue(null, "version");
	    while (parser.next() != XmlPullParser.END_TAG) {
	        if (parser.getEventType() != XmlPullParser.START_TAG) {
	            continue;
	        }
	        String name = parser.getName();
	        // Starts by looking for the entry tag
	        if (name.equals("file")) {
	            //entries.add(readEntry(parser));
	        	String p = parser.getAttributeValue(null, "path");
	        	String c = parser.getAttributeValue(null,"checksum");
	        	String v = parser.getAttributeValue(null,"version");
	        	int size = Integer.decode(parser.getAttributeValue(null, "size"));
	        	map.put(p, new ManifestEntry(c,p,v,size));
	        } 
	        parser.next();
	    }  	
		
		
		
		files = Collections.unmodifiableMap(map);
	}
	public HoNManifest()
	{
		files = Collections.emptyMap();
	}
	
	public HoNManifest(InputStream in)
	{
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(new BufferedInputStream(in), null);
			parser.nextTag();
			parse(parser);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally {
			try{
				in.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public static class DownloadChangeSet
	{
		public Set<ManifestEntry> downloads;
		public Set<ManifestEntry> deletes;
		public DownloadChangeSet(HoNManifest oldManifest, HoNManifest newManifest)
		{
			this.deletes = new HashSet<ManifestEntry>(oldManifest.files.values());
			this.downloads = new HashSet<ManifestEntry>(newManifest.files.values());
			this.downloads.removeAll(oldManifest.files.values());
			this.deletes.removeAll(newManifest.files.values());
		}
	}

}
