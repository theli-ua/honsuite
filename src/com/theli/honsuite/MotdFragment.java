package com.theli.honsuite;
import java.net.URL;
import java.util.LinkedHashMap;

import org.lorecraft.phparser.SerializedPhpParser;

import roboguice.inject.InjectView;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;


public class MotdFragment extends RoboSherlockFragment {
	@InjectView(R.id.text)	TextView content;
	public void setMOTD(String str)
	{
		content.setMovementMethod(new ScrollingMovementMethod());
		LinkedHashMap<?, ?>motdData;
		try {
			motdData = (LinkedHashMap<?, ?>)(new SerializedPhpParser(str)).parse();
			str = (String)motdData.get("motddata");
			str = str.replaceAll("\n", "<br/>");
			String[] motds = str.split("\\|");
			StringBuilder sb = new StringBuilder();
			for(String motd : motds)
			{
				sb.append("<p>");
				/* 
				 * Title
				 * Content
				 * Author
				 * Date
				 * Unknown
				 */
				String [] curMotd = motd.split("`");
				sb.append("<h1>");
				sb.append(Utils.HoN2HTML(curMotd[0]));
				sb.append("</h1>");

				sb.append(Utils.HoN2HTML(curMotd[1]));

				sb.append("<div align=\"right\"><i>");
				sb.append(curMotd[2]);
				sb.append("<br>");
				sb.append(curMotd[3]);
				sb.append("</i></div>");

				sb.append("</p>");
			}
			content.setText(Html.fromHtml(sb.toString()), TextView.BufferType.SPANNABLE);        	
		}
		catch (Exception e)
		{
			e.printStackTrace();
			content.setText(R.string.motd_parsing_error);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			// https://www.heroesofnewerth.com/gen/client_motd.php
			new URLRequester(new URL(Globals.HON_NA_WEBSITE
					+ "gen/client_motd.php")) {
				@Override
				protected void onSuccess(String res) {
					setMOTD(res);
				}
				@Override
				protected void onException(Exception e)
				{
					content.setText(R.string.motd_error);
				}
			}.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.simpletext, container, false);
		return v;
	}
}
