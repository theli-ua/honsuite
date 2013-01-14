package com.theli.honsuite;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipFile;

import org.lorecraft.phparser.SerializedPhpParser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainHoNSuiteActivity extends SlidingFragmentActivity {
	private Fragment mContent;

	@Inject HoNData honData;
	@Inject SharedPreferences sharedPref;


	protected void checkForHoNUpdate() {
		Boolean bCheckUpdate = sharedPref.getBoolean(
				Globals.KEY_PREF_SYNC_STARTUP, true);
		File manifestFile = new File(getExternalFilesDir(null),
				"manifest.xml.zip");
		File newManifestFile = new File(new File(getExternalFilesDir(null), ".tmp"),"manifest.xml.zip");
		Boolean bContinue = false;
		if (!newManifestFile.exists()) {
			if (manifestFile.exists()) {
				bContinue = true;
				bCheckUpdate = false;
			} else
				bCheckUpdate = true;
		}
		if (bCheckUpdate || bContinue) {
			try {
				new URLRequester(new URL(Globals.HON_NA_MASTERSERVER
						+ Globals.HON_MASTERSERVER_PATCHER_PATH),
						"version=0.0.0.0&os=wac&arch=i686") {
					@Override
					protected void onSuccess(String res) {
						Map<?,?> version_result = (Map<?,?>)(new SerializedPhpParser(res)).parse();
						onNewVersion(version_result);
					}
				}.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	protected void onNewVersion(Map<?,?> version)
	{
		/*
		 {0: {u'arch': u'i686',
		     u'latest_manifest_checksum': u'e3b355abbc8ff2f1e872cfae784817aaed917677',
		     u'latest_manifest_size': u'956464',
		     u'latest_version': u'2.6.33',
		     u'os': u'wbc',
		     u'url': u'http://dl.heroesofnewerth.com/',
		     u'url2': u'http://patch.hon.s2games.com/',
		     u'version': u'2.6.33'},
		 u'current_version': u'...0',
		 u'version': u'2.6.33.0'}
		 */
		File manifestFile = new File(getExternalFilesDir(null),
				"manifest.xml.zip");
		File newManifestFile = new File(new File(getExternalFilesDir(null), ".tmp"),"manifest.xml.zip");
		HoNData.HoNManifest newManifest;
		HoNData.HoNManifest oldManifest;
		if (manifestFile.exists()) {
			try {
				ZipFile zip = new ZipFile(manifestFile);
				oldManifest = honData.new HoNManifest(zip.getInputStream(zip.getEntry("manifest.xml")));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				oldManifest = honData.new HoNManifest();
			}
		} 
		else
		{
			oldManifest = honData.new HoNManifest();
		}

		if (!newManifestFile.exists()) {
			if (oldManifest.version != (String)version.get("version"))
			{
				new HoNFileDownloader(
						Arrays.asList(new HoNManifest.ManifestEntry[]	{
								new HoNManifest.ManifestEntry(
										(String)((Map<?,?>)version.get(0)).get("latest_manifest_checksum"),
										"manifest.xml",
										(String)version.get("version"),
										Integer.decode((String)((Map<?,?>)version.get(0)).get("latest_manifest_size"))
									)}),
						1,
						version,
						new File(getExternalFilesDir(null),".tmp")
						)
				{
				@Override
				protected void onPostExecute(Integer res) {
						onNewVersion(getVersionInfo());
					}
				}.execute();
			}
		}
		else
		{
			try {
				ZipFile zip = new ZipFile(newManifestFile);
				newManifest = honData.new HoNManifest(zip.getInputStream(zip.getEntry("manifest.xml")));
				//HoNManifest.DownloadChangeSet changeSet = new HoNManifest.DownloadChangeSet(oldManifest, newManifest);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setTitle(mTitleRes);

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// set the Above View
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null)
			// mContent = new ColorFragment(R.color.red);
			mContent = new MotdFragment();

		// set the Above View
		setContentView(R.layout.content_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent).commit();

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new HonSuiteMenuFragment()).commit();

		// customize the SlidingMenu
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		
		
		checkForHoNUpdate();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
			/*
			 * case R.id.github: Util.goToGitHub(this); return true;
			 */
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchContent(Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		getSlidingMenu().showContent();
	}

	public static class HonSuiteMenuFragment extends ListFragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.list, null);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			String[] colors = getResources()
					.getStringArray(R.array.navigation_list);
			ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1,
					android.R.id.text1, colors);
			setListAdapter(colorAdapter);
		}

		@Override
		public void onListItemClick(ListView lv, View v, int position, long id) {
			Fragment newContent = null;
			switch(position){
			case 0:
				newContent = new MotdFragment();
				break;
			}
			if (newContent != null)
				switchFragment(newContent);
		}

		// the meat of switching the above fragment
		private void switchFragment(Fragment fragment) {
			((MainHoNSuiteActivity) getActivity()).switchContent(fragment);
		}

	}
}
