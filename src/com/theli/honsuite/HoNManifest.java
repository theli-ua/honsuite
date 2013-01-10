package com.theli.honsuite;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class HoNManifest extends DefaultHandler {
	public static class ManifestEntry
	{
		public final String checksum;
		public final int size;
		public final String path;
		public final String version;
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
	Collection<ManifestEntry> files;
	String os;
	String arch;
	String version;
	static final String _osAttrName = "os";
	static final String _versionAttrName = "version";
	static final String _pathAttrName = "path";
	static final String _archAttrName = "arch";
	static final String _checksumAttrName = "checksum";
	static final String _sizeAttrName = "size";
	static final String _fileTag = "file";
	static final String _manifestTag = "manifest";
	public HoNManifest()
	{
		files = Collections.emptyList();
	}
	
	//opening element tag
	@Override
	public void startElement (String uri, String name, String qName, Attributes atts)
	{
	    //handle the start of an element
		if (name.equals(_manifestTag))
		{
			this.os = atts.getValue(_osAttrName);
			this.version = atts.getValue(_versionAttrName);
			this.arch = atts.getValue(_archAttrName);
		}
		/*
		else if (name.equals(_fileTag))
		{
			//this.files.put(e.path, e);
			this.files.add(
					new ManifestEntry(atts.getValue(_checksumAttrName),
							atts.getValue(_pathAttrName),atts.getValue(_versionAttrName),
					Integer.decode(atts.getValue(_sizeAttrName)))
					);
		}
		*/
	}
	
	public HoNManifest(InputStream in)
	{
		this.files = new ArrayList<ManifestEntry>();
		try {
			SAXParserFactory _f = SAXParserFactory.newInstance();
			SAXParser _p = _f.newSAXParser();
			XMLReader reader = _p.getXMLReader();
			reader.setContentHandler(this);
			reader.parse(new InputSource(in));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static class DownloadChangeSet
	{
		public Set<ManifestEntry> downloads;
		public Set<ManifestEntry> deletes;
		public DownloadChangeSet(HoNManifest oldManifest, HoNManifest newManifest)
		{
			this.deletes = new HashSet<ManifestEntry>(oldManifest.files);
			this.downloads = new HashSet<ManifestEntry>(newManifest.files);
			this.downloads.removeAll(oldManifest.files);
			this.deletes.removeAll(newManifest.files);
		}
	}
}
