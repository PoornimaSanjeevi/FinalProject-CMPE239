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
public class GetCheckinData {

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
								"/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_checkin.json")));

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(
						"/data/239/yelp/predOut/checkin.csv")));
		String json = null;
		int cnt = 0, restCnt = 0;

		while ((json = br2.readLine()) != null) {
			JSONObject obj = (JSONObject) JSONValue.parse(json);
			String busId = obj.get("business_id").toString();
			if (restaurants.contains(busId)) {
				bw.write(busId);

				// String userId = obj.get("user_id").toString();
				JSONObject checkininfo = (JSONObject) obj.get("checkin_info");
				int[] weekSum = new int[7];
				for (int i = 0; i < 7; i++) {

					weekSum[i] = 0;
					for (int j = 0; j < 24; j++) {
						String key = j + "-" + i;
						try {
							weekSum[i] += Integer.parseInt(checkininfo.get(key)
									.toString());
						} catch (Exception e) {
						}
					}

					bw.write("," + weekSum[i]);
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
