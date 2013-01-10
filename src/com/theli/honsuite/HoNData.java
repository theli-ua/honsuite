package com.theli.honsuite;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import roboguice.inject.ContextSingleton;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;

@ContextSingleton
public class HoNData {
	@Inject Context context;
	SQLiteDatabase db = (new ManifestTableOpener()).getWritableDatabase();
	
	private static final String OS = "os";
	private static final String VERSION = "version";
	private static final String ARCH = "arch";

	private static final String MANIFESTS_TABLE_NAME = "manifests";
	private static final String MANIFESTS_REFERENCE = "tablename";

	private static final String STRINGTABLE_NAME = "stringtable";
	private static final String STRINGTABLE_KEY = "key";
	private static final String STRINGTABLE_VALUE = "value";

	private static final String ENTITYTABLE_NAME = "entities";
	private static final String ENTITYTABLE_KEY = "name";
	private static final String ENTITYTABLE_VALUE = "path";

	private static final String FILE_PATH = "path";
	private static final String FILE_VERSION = VERSION;
	private static final String FILE_SIZE = "size";
	private static final String FILE_CHECKSUM = "checksum";
	private static final String FILE_ZIPSIZE = "zipsize";
	
	

	private class ManifestTableOpener extends SQLiteOpenHelper
	{
		private static final int DATABASE_VERSION = 2;
		private static final String DATABASE_NAME = "hondata";
		
	    private static final String MANIFESTS_TABLE_CREATE =
	                "CREATE TABLE " + MANIFESTS_TABLE_NAME + " (" +
	                OS + " TEXT, " +
	                VERSION + " TEXT, " +
	                MANIFESTS_REFERENCE + " TEXT," +
	                ARCH + " TEXT);";
	    
	    private static final String STRINGTABLE_CREATE = 
	    		"CREATE TABLE " + STRINGTABLE_NAME + " (" +
	    		STRINGTABLE_KEY + " TEXT PRIMARY KEY, " + 
	    		STRINGTABLE_VALUE + " TEXT );";
	    
	    private static final String ENTITYTABLE_CREATE = 
	    		"CREATE TABLE " + ENTITYTABLE_NAME + " (" +
	    		ENTITYTABLE_KEY + " TEXT PRIMARY KEY, " + 
	    		ENTITYTABLE_VALUE + " TEXT );";
	    
	    private static final String FILES_TABLE_CREATE = 
	    		"CREATE TABLE %s (" +
	    		FILE_PATH + " TEXT," +
	    		FILE_SIZE + " INTEGER," +
	    		FILE_ZIPSIZE + " INTEGER," +
	    		FILE_CHECKSUM + " CHECKSUM," +
	    		FILE_VERSION + " TEXT," +
	    		"PRIMARY KEY (" + FILE_PATH + "," + FILE_VERSION + ");";
	    		
	    
		public ManifestTableOpener() {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(MANIFESTS_TABLE_CREATE);
			
			db.execSQL(STRINGTABLE_CREATE);
			
			db.execSQL(ENTITYTABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
		}
	}
		
	public class HoNManifest extends DefaultHandler {
		String os;
		String arch;
		String version;
		String table;
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
			// files = Collections.emptyList();
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
				
				//check if table with files for this manifest already exists
				db.q
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
			//this.files = new ArrayList<ManifestEntry>();
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
		/*
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
		*/
	}
}
