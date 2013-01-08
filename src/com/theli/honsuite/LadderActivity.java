package com.theli.honsuite;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

public class LadderActivity extends RoboSherlockFragmentActivity {
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            LadderFragment list = new LadderFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class LadderFragment extends RoboSherlockFragment {
        private WebView content;
        static LadderFragment newInstance(int num) {
            LadderFragment f = new LadderFragment();
            return f;
        }
        

        @SuppressLint("SetJavaScriptEnabled")
		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            //View v = inflater.inflate(R.layout.simpletext, container, false);
            //return v;
        	content = new WebView(inflater.getContext());
        	content.getSettings().setBuiltInZoomControls(true); 
        	content.getSettings().setSupportZoom(true);
        	content.getSettings().setJavaScriptEnabled(true);

        	content.loadUrl(Globals.HON_NA_LADDER_URL);
        	return content;
        }
    }

}
