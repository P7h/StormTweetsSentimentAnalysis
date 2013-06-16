package org.p7h.storm.sentimentanalysis.bolts;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.io.Resources;
import org.p7h.storm.sentimentanalysis.utils.BingMapsLookup;
import org.p7h.storm.sentimentanalysis.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Status;

/**
 * Counts the hashtags and displays the count info to the console and also logs to the file.
 *
 * @author - Prashanth Babu
 */
public final class SentimentCalculatorBolt extends BaseRichBolt {
	private static final Logger LOGGER = LoggerFactory.getLogger(SentimentCalculatorBolt.class);
	private static final long serialVersionUID = 3744516923854606735L;

	/**
	 * Interval between logging the output.
	 */
	private final long logIntervalInSeconds;

	private long runCounter;
	private Stopwatch stopwatch = null;
	private SortedMap<String,Integer> afinnSentimentMap = null;
	private SortedMap<String,Integer> stateSentimentMap = null;

	public SentimentCalculatorBolt(final long logIntervalInSeconds) {
		this.logIntervalInSeconds = logIntervalInSeconds;
	}

	@Override
	public final void prepare(final Map map, final TopologyContext topologyContext,
	                          final OutputCollector collector) {
		afinnSentimentMap = Maps.newTreeMap();
		stateSentimentMap = Maps.newTreeMap();

		final Properties properties = new Properties();
		try {
			properties.load(SentimentCalculatorBolt.class.getClassLoader()
					                .getResourceAsStream(Constants.CONFIG_PROPERTIES_FILE));
		} catch (final IOException exception) {
			//Should not occur. If it does, we cant continue. So exiting the program!
			LOGGER.error(exception.toString());
			System.exit(1);
		}
		//Spout will read the Bing Maps API Value and stores the same to BING_MAPS_API_KEY_VALUE of Constants.java  so that the Bolt can use it.
		//For the lack of time I am using this Constant or else using a good Design Pattern, this can be fine-tuned.
		Constants.BING_MAPS_API_KEY_VALUE = properties.getProperty(Constants.BING_MAPS_API_KEY_NAME);

		//Spout will read the AFINN Sentiment file and stores the same to a Map, AFINN_SENTIMENT_MAP of Constants.java so that the Bolt can use it.
		//For the lack of time I am using this Constant or else using a good Design Pattern, this can be fine-tuned.
		try {
			final URL url = Resources.getResource(Constants.AFINN_SENTIMENT_FILE_NAME);
			final String text = Resources.toString(url, Charsets.UTF_8);
			final Iterable<String> lineSplit = Splitter.on("\n").trimResults().omitEmptyStrings().split(text);
			List<String> tabSplit;
			for (final String str: lineSplit) {
				tabSplit = Lists.newArrayList(Splitter.on("\t").trimResults().omitEmptyStrings().split(str));
				afinnSentimentMap.put(tabSplit.get(0), Integer.parseInt(tabSplit.get(1)));
			}
		} catch (final IOException exception) {
			exception.printStackTrace();
			//Should not occur. If it occurs, we cant continue. So, exiting at this point itself.
			System.exit(1);
		}

		runCounter = 0;
		stopwatch = new Stopwatch();
		stopwatch.start();
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer outputFieldsDeclarer) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void execute(final Tuple input) {
		final Status status = (Status) input.getValueByField("tweet");
		final Optional<String> stateOptional = getStateFromTweet(status);
		String state;
		if(stateOptional.isPresent()) {
			state = stateOptional.get();
			final int sentimentOfTweet = getSentimentOfTweet(status);
			if(stateSentimentMap.containsKey(state)){
				final int previousSentiment = stateSentimentMap.get(state);
				stateSentimentMap.put(state, sentimentOfTweet + previousSentiment);
			} else {
				stateSentimentMap.put(state, sentimentOfTweet);
			}
		}

		if (logIntervalInSeconds <= stopwatch.elapsed(TimeUnit.SECONDS)) {
			logStateSentiments();
			stopwatch.reset();
			stopwatch.start();
		}
	}

	private final int getSentimentOfTweet(final Status status) {
		//Replacing all punctuation marks and new lines in the tweet with an empty space.
		final String tweet = status.getText().replaceAll("\\p{Punct}|\\n", " ").toLowerCase();
		//Splitting the tweet on empty space.
		final Iterable<String> words = Splitter.on(' ')
				                               .trimResults()
				                               .omitEmptyStrings()
				                               .split(tweet);
		int sentimentOfTweet = 0;
		for (final String word : words) {
			if(afinnSentimentMap.containsKey(word)){
				sentimentOfTweet += afinnSentimentMap.get(word);
			}
		}
		return sentimentOfTweet;
	}

	private final Optional<String> getStateFromTweet(final Status status) {
		String state = getStateFromGeoLocation(status);

		state = getStateFromPlaceObject(status, state);

		state = getStateFromUserObject(status, state);

		LOGGER.info("State before check:{}", state);
		if(null == state || !Constants.ALL_STATES.contains(state)) {
			LOGGER.info("State not present in our list. Skipping!");
			return Optional.absent();
		}
		LOGGER.info("State selected:{}", state);
		return Optional.of(state);
	}

	private final String getStateFromUserObject(final Status status, String state) {
		String stateFromUserObject = status.getUser().getLocation();
		LOGGER.info("State initially:{}", stateFromUserObject);
		if(null == state && null != stateFromUserObject && 1 < stateFromUserObject.length()) {
			String stateUser = stateFromUserObject.substring(stateFromUserObject.length() - 2);
			LOGGER.info("State from User:{}", stateFromUserObject);
			if("US".equalsIgnoreCase(stateUser) && 5 < stateFromUserObject.length()){
				stateUser = stateFromUserObject.substring(stateFromUserObject.length() - 6, stateFromUserObject.length() - 4);
				LOGGER.info("State from User again:{}", stateFromUserObject);
			}
			state = (2 == stateUser.length())? stateUser: null;
		}
		return state;
	}

	private final String getStateFromPlaceObject(final Status status, String state) {
		final Place place = status.getPlace();
		if (null == state && null != place) {
			final String placeName = place.getFullName();
			if (null != placeName && 2 < placeName.length()) {
				final String stateFromPlaceObject = placeName.substring(placeName.length() - 2);
				LOGGER.info("State from Place:{}", stateFromPlaceObject);
				state = (2 == stateFromPlaceObject.length())? stateFromPlaceObject: null;
			}
		}
		return state;
	}

	private final String getStateFromGeoLocation(final Status status) {
		String state = null;
		final double latitude;
		final double longitude;
		final GeoLocation geoLocation = status.getGeoLocation();
		if (null != geoLocation) {
			latitude = geoLocation.getLatitude();
			longitude = geoLocation.getLongitude();
			LOGGER.info("LatLng for BingMaps:{} and {}", latitude, longitude);
			final Optional<String> stateGeoOptional = BingMapsLookup.reverseGeocodeFromLatLong(latitude, longitude);
			if(stateGeoOptional.isPresent()){
				final String stateFromGeoLocation = stateGeoOptional.get();
				LOGGER.info("State from BingMaps:{}", stateFromGeoLocation);
				state = (2 == stateFromGeoLocation.length())? stateFromGeoLocation: null;
			}
		}
		return state;
	}

	private final void logStateSentiments() {
		final StringBuilder dumpSentimentsToLog = new StringBuilder();

		for (final Map.Entry<String, Integer> state : this.stateSentimentMap.entrySet()) {
			//Write to console and / or log file.
			dumpSentimentsToLog
					.append("\t")
					.append(state.getKey())
					.append(" ==> ")
					.append(state.getValue())
					.append("\n");
			runCounter++;
		}
		this.runCounter++;
		LOGGER.info("At {}, total # of States received in run#{}: {} ", new Date(), runCounter,
				           stateSentimentMap.size());
		LOGGER.info("\n{}", dumpSentimentsToLog.toString());

		//Decide to clear this map or not!
		//stateSentimentMap.clear();
	}
}