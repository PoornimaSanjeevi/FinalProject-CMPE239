package com.sjsu.cmpe239.yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Created by poornima on 11/19/15.
 */
public class GetRestaurantData {

	static String[] getOpenCloseHours(JSONObject obj, String day) {
		JSONObject days = (JSONObject) obj.get(day);
		String[] open = new String[2];
		try {
			open[0] = days.get("open").toString();
			open[1] = days.get("close").toString();
		} catch (NullPointerException e) {
			open[0] = "-1";
			open[1] = "-1";
		}
		return open;
	}

	public static void main(String[] args) throws IOException {
		Set<String> restaurants = new HashSet<String>();
		BufferedReader br1 = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(
								"/data/239/yelp/predOut/restaurantIds.txt")));
		String line = null;
		while ((line = br1.readLine()) != null) {
			restaurants.add(line);
		}
		br1.close();

		BufferedReader br2 = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(
								"/data/239/yelp/predOut/business.json")));

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(
						"/data/239/yelp/predOut/restData.txt")));
		String json = null;
		int cnt = 0, restCnt = 0;
		String[] daysofweek = { "Monday", "Tuesday", "Wednesday", "Thursday",
				"Friday", "Saturday", "Sunday" };
		while ((json = br2.readLine()) != null) {
			JSONObject obj = (JSONObject) JSONValue.parse(json);
			String busId = obj.get("business_id").toString();
			if (restaurants.contains(busId)) {
				bw.write(busId);
				
				// String userId = obj.get("user_id").toString();
				JSONObject hour = (JSONObject) obj.get("hours");
				for (String d : daysofweek) {
					String[] hr = getOpenCloseHours(hour, d);
					bw.write("\t");
					bw.write(hr[0]);
					bw.write("\t");
					bw.write(hr[1]);

				}
				bw.write("\n");
				restCnt++;
			}
			cnt++;
		}
		br2.close();
		System.out.println("Total Reviews:" + cnt);
		System.out.println("Restaurants:" + restCnt);
		bw.close();
	}
}
