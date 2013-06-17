package org.p7h.storm.sentimentanalysis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BingMapsLookup {
	private static final Logger LOGGER = LoggerFactory.getLogger(BingMapsLookup.class);
	private static final String BING_MAPS_URL_START = "http://dev.virtualearth.net/REST/v1/Locations/";
	private static final String BING_MAPS_URL_MIDDLE_JSON = "?o=json&key=";

	//Dummy and temporary method for Unit testing Bing Maps API.
	public static void main(String[] args) {
		double latitude = 33.98067209;
		double longitude= -84.07507873;
		Optional<String> state = reverseGeocodeFromLatLong(latitude, longitude);
		if(state.isPresent()) {
			LOGGER.info("State==>{}" + state.get());
		} else {
			LOGGER.info("Could not find State!!");
		}
	}

	/**
	 * Reverse geocodes the State from the Latitude and Longitude received.
	 *
	 * @param latitude Latitude present in the tweet
	 * @param longitude Longitude present in the tweet
	 * @return State of the Tweet as got from Bing Maps API reponse.
	 */
	public final static Optional<String> reverseGeocodeFromLatLong(final double latitude, final double longitude) {
		final StringBuilder bingMapsURL = new StringBuilder();
		bingMapsURL
				.append(BING_MAPS_URL_START)
				.append(latitude)
				.append(",")
				.append(longitude)
				.append(BING_MAPS_URL_MIDDLE_JSON)
				.append(Constants.BING_MAPS_API_KEY_VALUE);
		LOGGER.debug("BingMapsURL==>{}", bingMapsURL.toString());

		HttpURLConnection httpURLConnection;
		InputStream inputStream = null;
		try {
			final URL url = new URL(bingMapsURL.toString());
			httpURLConnection = (HttpURLConnection)url.openConnection();

			if(HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()){
				inputStream = httpURLConnection.getInputStream();
				return getStateFromJSONResponse(inputStream);
			}
		} catch (final Throwable throwable) {
			LOGGER.error(throwable.getMessage(), throwable);
			throwable.printStackTrace();
		} finally{
			if(null != inputStream) {
				try {
					inputStream.close();
				} catch (final IOException ioException) {
					LOGGER.error(ioException.getMessage(), ioException);
					ioException.printStackTrace();
				}
			}
			httpURLConnection = null;
		}
		return Optional.absent();
	}

	/**
	 * Gets the State of the Tweet by checking the InputStream.
	 * For a sample Bing Maps API response, please check the snippet at the end of this file.
	 *
	 * @param inputStream Bing Maps API response.
	 * @return State of the Tweet as got from Bing Maps API reponse.
	 */
	@SuppressWarnings("unchecked")
	private final static Optional<String> getStateFromJSONResponse(InputStream inputStream) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			//final Map<String,Object> bingResponse = (Map<String, Object>) mapper.readValue(new File("C:/BingMaps_JSON_Response.json"), Map.class);
			final Map<String,Object> bingResponse = (Map<String, Object>) mapper.readValue(inputStream, Map.class);
			if(200 == Integer.parseInt(String.valueOf(bingResponse.get("statusCode")))) {
				final List<Map<String, Object>> resourceSets = (List<Map<String, Object>>) bingResponse.get("resourceSets");
				if(resourceSets != null && resourceSets.size() > 0){
					final List<Map<String, Object>> resources = (List<Map<String, Object>>) resourceSets.get(0).get("resources");
					if(resources != null && resources.size() > 0){
						final Map<String, Object> address = (Map<String, Object>) resources.get(0).get("address");
						LOGGER.debug("State==>{}", address.get("adminDistrict"));
						return Optional.of((String) address.get("adminDistrict"));
					}
				}
			}
		} catch (final IOException ioException) {
			LOGGER.error(ioException.getMessage(), ioException);
			ioException.printStackTrace();
		}
		return Optional.absent();
	}
}
//========================================================================================================
//Sample JSON response from Bing Maps API
/*
{
    "authenticationResultCode": "ValidCredentials",
    "brandLogoUri": "http://dev.virtualearth.net/Branding/logo_powered_by.png",
    "copyright": "Copyright © 2013 Microsoft and its suppliers. All rights reserved. This API cannot be accessed and the content and any results may not be used, reproduced or transmitted in any manner without express written permission from Microsoft Corporation.",
    "resourceSets": [
        {
            "estimatedTotal": 1,
            "resources": [
                {
                    "__type": "Location:http://schemas.microsoft.com/search/local/ws/rest/v1",
                    "bbox": [
                        47.6367076845747,
                        -122.137017091469,
                        47.6444331197161,
                        -122.121730456396
                    ],
                    "name": "1 Microsoft Way, Redmond, WA 98052",
                    "point": {
                        "type": "Point",
                        "coordinates": [
                            47.6405704021454,
                            -122.129373773932
                        ]
                    },
                    "address": {
                        "addressLine": "1 Microsoft Way",
                        "adminDistrict": "WA",
                        "adminDistrict2": "King Co.",
                        "countryRegion": "United States",
                        "formattedAddress": "1 Microsoft Way, Redmond, WA 98052",
                        "locality": "Redmond",
                        "postalCode": "98052"
                    },
                    "confidence": "Medium",
                    "entityType": "Address",
                    "geocodePoints": [
                        {
                            "type": "Point",
                            "coordinates": [
                                47.6405704021454,
                                -122.129373773932
                            ],
                            "calculationMethod": "Interpolation",
                            "usageTypes": [
                                "Display",
                                "Route"
                            ]
                        }
                    ],
                    "matchCodes": [
                        "Good"
                    ]
                }
            ]
        }
    ],
    "statusCode": 200,
    "statusDescription": "OK",
    "traceId": "423cefc66e6d4e858661ee14d4f85c5e|BAYM000169|02.00.139.700|BY2MSNVM001074, BY2IPEVM000006"
}
 */
//========================================================================================================
