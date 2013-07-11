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
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import org.p7h.storm.sentimentanalysis.utils.Constants;
import org.p7h.storm.sentimentanalysis.utils.SentimentValueOrdering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Breaks each tweet into words and calculates the sentiment of each tweet and assocaites the sentiment value to the State
 * and logs the same to the console and also logs to the file.
 *
 * @author - Prashanth Babu
 */
public final class SentimentCalculatorBolt extends BaseRichBolt {
	private static final Logger LOGGER = LoggerFactory.getLogger(SentimentCalculatorBolt.class);
	private static final long serialVersionUID = -713541667509574750L;

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

		//Bolt will read the AFINN Sentiment file [which is in the classpath] and stores the key, value pairs to a Map.
		try {
			final URL url = Resources.getResource(Constants.AFINN_SENTIMENT_FILE_NAME);
			final String text = Resources.toString(url, Charsets.UTF_8);
			final Iterable<String> lineSplit = Splitter.on("\n").trimResults().omitEmptyStrings().split(text);
			List<String> tabSplit;
			for (final String str: lineSplit) {
				tabSplit = Lists.newArrayList(Splitter.on("\t").trimResults().omitEmptyStrings().split(str));
				afinnSentimentMap.put(tabSplit.get(0), Integer.parseInt(tabSplit.get(1)));
			}
		} catch (final IOException ioException) {
			LOGGER.error(ioException.getMessage(), ioException);
			ioException.printStackTrace();
			//Should not occur. If it occurs, we cant continue. So, exiting at this point itself.
			System.exit(1);
		}

		runCounter = 0;
		stopwatch = new Stopwatch();
		stopwatch.start();
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer outputFieldsDeclarer) {
		//No-op
	}

	@Override
	public final void execute(final Tuple input) {
		final String state = (String) input.getValueByField("state");
		final Status status = (Status) input.getValueByField("tweet");
		final int sentimentOfTweet = getSentimentOfTweet(status);

		Integer previousSentiment = stateSentimentMap.get(state);
		previousSentiment = (null == previousSentiment) ? sentimentOfTweet : previousSentiment + sentimentOfTweet;
		stateSentimentMap.put(state, previousSentiment);

		if (logIntervalInSeconds <= stopwatch.elapsed(TimeUnit.SECONDS)) {
			logSentimentsOfStates();
			stopwatch.reset();
			stopwatch.start();
		}
	}

	/**
	 * Gets the sentiment of the current tweet.
	 *
	 * @param status -- Status Object.
	 * @return sentiment of the current tweet.
	 */
	private final int getSentimentOfTweet(final Status status) {
		//Remove all punctuation and new line chars in the tweet.
		final String tweet = status.getText().replaceAll("\\p{Punct}|\\n", " ").toLowerCase();
		//Splitting the tweet on empty space.
		final Iterable<String> words = Splitter.on(' ')
				                               .trimResults()
				                               .omitEmptyStrings()
				                               .split(tweet);
		int sentimentOfCurrentTweet = 0;
		//Loop thru all the wordsd and find the sentiment of this tweet.
		for (final String word : words) {
			if(afinnSentimentMap.containsKey(word)){
				sentimentOfCurrentTweet += afinnSentimentMap.get(word);
			}
		}
		LOGGER.debug("Tweet : Sentiment {} ==> {}", tweet, sentimentOfCurrentTweet);
		return sentimentOfCurrentTweet;
	}

	//Ideally we should be knocking off the URLs from the tweet since they don't need to parsed.
	private String filterOutURLFromTweet(final Status status) {
		final String tweet = status.getText();
		final URLEntity[] urlEntities = status.getURLEntities();
		int startOfURL;
		int endOfURL;
		String truncatedTweet = "";
		for(final URLEntity urlEntity: urlEntities){
			startOfURL = urlEntity.getStart();
			endOfURL = urlEntity.getEnd();
			truncatedTweet += tweet.substring(0, startOfURL) + tweet.substring(endOfURL);
		}
		return truncatedTweet;
	}

	/**
	 * Logs the score of Sentiments of States at regular intervals.
	 */
	private final void logSentimentsOfStates() {
		final StringBuilder dumpSentimentsToLog = new StringBuilder();

		//Sort the Map before logging output based on the sentiment value so that we can get the happiest and unhappiest state.
		final List<Map.Entry<String, Integer>> list = new ArrayList<>(stateSentimentMap.entrySet());
		Collections.sort(list, new SentimentValueOrdering());

		for (final Map.Entry<String, Integer> state : list) {
			//Write to console and / or log file.
			dumpSentimentsToLog
					.append("\t")
					.append(state.getKey())
					.append(" ==> ")
					.append(state.getValue())
					.append("\n");
		}
		this.runCounter++;
		LOGGER.info("At {}, total # of States received in run#{}: {} ", new Date(), runCounter,
				           stateSentimentMap.size());
		LOGGER.info("\n{}", dumpSentimentsToLog.toString());

		//Decide whether to clear this map or not!
		//We better not clear it so that we can guage the sentiment value better.
		//stateSentimentMap.clear();
	}
}