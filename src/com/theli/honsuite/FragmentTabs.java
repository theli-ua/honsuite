package com.theli.honsuite;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.lorecraft.phparser.SerializedPhpParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TabHost;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;

import roboguice.inject.InjectView;
import roboguice.inject.ContentView;

/**
 * This demonstrates how you can implement switching between the tabs of a
 * TabHost through fragments. It uses a trick (see the code below) to allow the
 * tabs to switch between fragments instead of simple views.
 */
@ContentView(R.layout.fragment_tabs)
public class FragmentTabs extends RoboSherlockFragmentActivity {
	@InjectView(android.R.id.tabhost)
	TabHost mTabHost;
	TabManager mTabManager;
	@Inject HoNData honData;;

	protected void checkForHoNUpdate() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTabHost.setup();

		mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);

		mTabManager.addTab(mTabHost.newTabSpec("motd").setIndicator("MOTD"),
				MotdActivity.MotdFragment.class, null);
		//mTabManager.addTab(mTabHost.newTabSpec("ladder").setIndicator("Ladder"),
				//LadderActivity.LadderFragment.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
		checkForHoNUpdate();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

	/**
	 * This is a helper class that implements a generic mechanism for
	 * associating fragments with the tabs in a tab host. It relies on a trick.
	 * Normally a tab host has a simple API for supplying a View or Intent that
	 * each tab will show. This is not sufficient for switching between
	 * fragments. So instead we make the content part of the tab host 0dp high
	 * (it is not shown) and the TabManager supplies its own dummy view to show
	 * as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct fragment shown in a separate content area whenever
	 * the selected tab changes.
	 */
	public static class TabManager implements TabHost.OnTabChangeListener {
		private final FragmentActivity mActivity;
		private final TabHost mTabHost;
		private final int mContainerId;
		private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
		TabInfo mLastTab;

		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;
			private Fragment fragment;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabManager(FragmentActivity activity, TabHost tabHost,
				int containerId) {
			mActivity = activity;
			mTabHost = tabHost;
			mContainerId = containerId;
			mTabHost.setOnTabChangedListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mActivity));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state. If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			info.fragment = mActivity.getSupportFragmentManager()
					.findFragmentByTag(tag);
			if (info.fragment != null && !info.fragment.isDetached()) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager()
						.beginTransaction();
				ft.detach(info.fragment);
				ft.commit();
			}

			mTabs.put(tag, info);
			mTabHost.addTab(tabSpec);
		}

		@Override
		public void onTabChanged(String tabId) {
			TabInfo newTab = mTabs.get(tabId);
			if (mLastTab != newTab) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager()
						.beginTransaction();
				if (mLastTab != null) {
					if (mLastTab.fragment != null) {
						ft.detach(mLastTab.fragment);
					}
				}
				if (newTab != null) {
					if (newTab.fragment == null) {
						newTab.fragment = Fragment.instantiate(mActivity,
								newTab.clss.getName(), newTab.args);
						ft.add(mContainerId, newTab.fragment, newTab.tag);
					} else {
						ft.attach(newTab.fragment);
					}
				}

				mLastTab = newTab;
				ft.commit();
				mActivity.getSupportFragmentManager()
						.executePendingTransactions();
			}
		}
	}
}