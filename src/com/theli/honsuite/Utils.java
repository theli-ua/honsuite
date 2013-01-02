package com.theli.honsuite;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	final static Pattern SplitPattern = Pattern.compile("(.*?)(?:\\^([0-9]{3}|[a-zA-Z*]))");
	static final String[] hon_colors = new String []
			{"00","1C","38","54","70","8C","A8","C4","E0","FF"};
    private static final Map<String, String> honColorMap;
    static {
        Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("o", "ffa500");
        aMap.put("r", "ff0000");
        aMap.put("y", "ffff00");
        aMap.put("g", "008000");
        aMap.put("c", "00ffff");
        aMap.put("b", "0000ff");
        aMap.put("m", "ff00ff");
        honColorMap = Collections.unmodifiableMap(aMap);
    }
	public static String HoN2HTML(String input)
	{
		input = input + "^*";
		StringBuilder output = new StringBuilder();
		Boolean tagOpen = false;
		Matcher m = SplitPattern.matcher(input);
		while (m.find())
		{
			output.append(m.group(1));
			String color = m.group(2).toLowerCase(Locale.US);
			if (tagOpen)
				output.append("</font>");
			if (color.length() == 3)
			{
				int a = Integer.decode(color.substring(0,1));
				int b = Integer.decode(color.substring(1,2));
				int c = Integer.decode(color.substring(2,3));
				output.append(String.format("<font color=\"#%s%s%s\">",
						hon_colors[a],hon_colors[b],hon_colors[c]));
				tagOpen = true;
			}
			else
			{
				if(honColorMap.containsKey(color))
				{
					output.append(String.format("<font color=\"#%s\">",honColorMap.get(color)));
					tagOpen = true;
				}
			}
		}
		
		return output.toString();	
	}
}
