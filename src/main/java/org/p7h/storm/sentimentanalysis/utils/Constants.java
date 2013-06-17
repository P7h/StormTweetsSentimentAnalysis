package org.p7h.storm.sentimentanalysis.utils;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Constants used in this project.
 *
 * @author - Prashanth Babu
 */
public final class Constants {
	//Name of the Topology. Used while launching the LocalCluster
	public static final String TOPOLOGY_NAME = "SentimentAnalysis";

	//Properties file which has all the configurable parameters required for execution of this Topology.
	public static final String CONFIG_PROPERTIES_FILE = "config.properties";

	public static final String OAUTH_ACCESS_TOKEN = "OAUTH_ACCESS_TOKEN";
	public static final String OAUTH_ACCESS_TOKEN_SECRET = "OAUTH_ACCESS_TOKEN_SECRET";
	public static final String OAUTH_CONSUMER_KEY = "OAUTH_CONSUMER_KEY";
	public static final String OAUTH_CONSUMER_SECRET = "OAUTH_CONSUMER_SECRET";

	public static final String BING_MAPS_API_KEY = "BING_MAPS_API_KEY";
	//Bolt reads the Bing Maps API Value and stores the same to BING_MAPS_API_KEY_VALUE of Constants.java so that it can be used for reverse geocoding.
	//For the lack of time I am using this Constant or else using a good Design Pattern, this can be fine-tuned.
	public static String BING_MAPS_API_KEY_VALUE = "BING_MAPS_API_KEY_VALUE";

	//Sentiment scores of few words are present in this file.
	//For more info on this, please check: http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010
	public static final String AFINN_SENTIMENT_FILE_NAME = "AFINN-111.txt";

	//Codes of all the states of USA.
	//Used as a precautionary measure so that we can be completely sure that the State we got is indeed one of US States.
	public static final List<String> CONSOLIDATED_STATE_CODES = Lists.newArrayList("AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL","GA","HI","ID","IL","IN","IA","KS","KY","LA","ME","MT","NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","MD","MA","MI","MN","MS","MO","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV","WI","WY");
}
