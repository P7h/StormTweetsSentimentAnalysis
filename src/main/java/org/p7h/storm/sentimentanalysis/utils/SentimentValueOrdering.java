package org.p7h.storm.sentimentanalysis.utils;

import java.util.Map;

import com.google.common.collect.Ordering;

/**
 * Sorts sentiment map based on the descending order of sentiment values of the State.
 *
 * @author: Prashanth Babu
 */
public class SentimentValueOrdering extends Ordering<Map.Entry<String, Integer>> {
	@Override
	public int compare(final Map.Entry<String, Integer> status01,
	                   final Map.Entry<String, Integer> status02) {
		final int sentimentValue01 = status01.getValue();
		final int sentimentValue02 = status02.getValue();
		return (sentimentValue02 - sentimentValue01);
	}
}
