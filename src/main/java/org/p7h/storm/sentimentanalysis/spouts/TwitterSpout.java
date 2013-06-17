package org.p7h.storm.sentimentanalysis.spouts;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.p7h.storm.sentimentanalysis.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Spout which gets tweets from Twitter using OAuth Credentials.
 *
 * @author - Prashanth Babu
 */
public final class TwitterSpout extends BaseRichSpout {
	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSpout.class);
	private static final long serialVersionUID = -1590819539847344427L;

	private SpoutOutputCollector _outputCollector;
    private LinkedBlockingQueue<Status> _queue;
    private TwitterStream _twitterStream;

	@Override
	public final void open(final Map conf, final TopologyContext context,
	                 final SpoutOutputCollector collector) {
		this._queue = new LinkedBlockingQueue<>(1000);
		this._outputCollector = collector;

		final StatusListener statusListener = new StatusListener() {
			@Override
			public void onStatus(final Status status) {
				_queue.offer(status);
			}

			@Override
			public void onDeletionNotice(final StatusDeletionNotice sdn) {
			}

			@Override
			public void onTrackLimitationNotice(final int i) {
			}

			@Override
			public void onScrubGeo(final long l, final long l1) {
			}

			@Override
			public void onStallWarning(final StallWarning stallWarning) {
			}

			@Override
			public void onException(final Exception e) {
			}
		};
		//Twitter stream authentication setup
		final Properties properties = new Properties();
		try {
			properties.load(TwitterSpout.class.getClassLoader()
					                .getResourceAsStream(Constants.CONFIG_PROPERTIES_FILE));
		} catch (final IOException ioException) {
			//Should not occur. If it does, we cant continue. So exiting the program!
			LOGGER.error(ioException.getMessage(), ioException);
			System.exit(1);
		}

		final ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setIncludeEntitiesEnabled(true);

		configurationBuilder.setOAuthAccessToken(properties.getProperty(Constants.OAUTH_ACCESS_TOKEN));
		configurationBuilder.setOAuthAccessTokenSecret(properties.getProperty(Constants.OAUTH_ACCESS_TOKEN_SECRET));
		configurationBuilder.setOAuthConsumerKey(properties.getProperty(Constants.OAUTH_CONSUMER_KEY));
		configurationBuilder.setOAuthConsumerSecret(properties.getProperty(Constants.OAUTH_CONSUMER_SECRET));
		this._twitterStream = new TwitterStreamFactory(configurationBuilder.build()).getInstance();
		this._twitterStream.addListener(statusListener);

		//Our usecase computes the sentiments of States of USA based on tweets.
		//So we will apply filter with US bounding box so that we get tweets specific to US only.
		final FilterQuery filterQuery = new FilterQuery();

		//Bounding Box for United States.
		//For more info on how to get these coordinates, check:
		//http://www.quora.com/Geography/What-is-the-longitude-and-latitude-of-a-bounding-box-around-the-continental-United-States
		final double[][] boundingBoxOfUS = {{-124.848974, 24.396308},
				                                   {-66.885444, 49.384358}};
		filterQuery.locations(boundingBoxOfUS);
		this._twitterStream.filter(filterQuery);
	}

	@Override
	public final void nextTuple() {
		final Status status = _queue.poll();
		if (null == status) {
			//If _queue is empty sleep the spout thread so it doesn't consume resources.
			Utils.sleep(500);
        } else {
			//Emit the complete tweet to the Bolt.
			this._outputCollector.emit(new Values(status));
		}
	}

	@Override
	public final void close() {
		this._twitterStream.cleanUp();
		this._twitterStream.shutdown();
	}

	@Override
	public final void ack(final Object id) {
	}

	@Override
	public final void fail(final Object id) {
	}

	@Override
	public final void declareOutputFields(final OutputFieldsDeclarer outputFieldsDeclarer) {
		//For emitting the complete tweet to the Bolt.
		outputFieldsDeclarer.declare(new Fields("tweet"));
	}
}