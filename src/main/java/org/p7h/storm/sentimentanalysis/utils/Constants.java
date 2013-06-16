package org.p7h.storm.sentimentanalysis.utils;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Constants used in this project.
 *
 * @author - Prashanth Babu
 */
public class Constants {
	public static final String TOPOLOGY_NAME = "SentimentAnalysis";

	public static final String CONFIG_PROPERTIES_FILE = "config.properties";

	public static final String OAUTH_ACCESS_TOKEN = "OAUTH_ACCESS_TOKEN";
	public static final String OAUTH_ACCESS_TOKEN_SECRET = "OAUTH_ACCESS_TOKEN_SECRET";
	public static final String OAUTH_CONSUMER_KEY = "OAUTH_CONSUMER_KEY";
	public static final String OAUTH_CONSUMER_SECRET = "OAUTH_CONSUMER_SECRET";

	public static final String BING_MAPS_API_KEY_NAME = "BING_MAPS_API_KEY_NAME";
	//Spout will read the Bing Maps API Value and stores the same here so that the Bolt can use it.
	//For the lack of time I am using this Constant or else using a good Design Pattern, this can be fine-tuned.
	public static String BING_MAPS_API_KEY_VALUE = "BING_MAPS_API_KEY";

	public static final String AFINN_SENTIMENT_FILE_NAME = "AFINN-111.txt";
	public static final List<String> ALL_STATES = Lists.newArrayList("AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY","LA","ME","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","MD","MA","MI","MN","MS","MO","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV","WI","WY");
}
