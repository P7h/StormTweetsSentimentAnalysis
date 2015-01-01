package org.p7h.storm.sentimentanalysis.utils;

import com.google.common.collect.Ordering;

import java.util.Map;

/**
 * Sorts sentiment map based on the descending order of sentiment values of the State.
 *
 * @author: Prashanth Babu
 */
public final class SentimentValueOrdering extends Ordering<Map.Entry<String, Integer>> {
	@Override
	public int compare(final Map.Entry<String, Integer> status01,
	                   final Map.Entry<String, Integer> status02) {
		final int sentimentValue01 = status01.getValue();
		final int sentimentValue02 = status02.getValue();
		return (sentimentValue02 - sentimentValue01);
	}
}
