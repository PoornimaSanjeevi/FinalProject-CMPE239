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

public class GetUserData {
	public static void main(String[] args) throws IOException {
		Set<String> user = new HashSet<String>();
		BufferedReader br1 = new BufferedReader(
				new InputStreamReader(new FileInputStream(
						"/data/239/yelp/predOut/restRevUsers.txt")));
		String line = null;
		while ((line = br1.readLine()) != null) {
			user.add(line.split("\t")[0]);
		}
		br1.close();

		BufferedReader br2 = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(
								"/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_user.json")));

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(
						"/data/239/yelp/predOut/users.csv")));
		String json = null;
		int cnt = 0, restCnt = 0;
		while ((json = br2.readLine()) != null) {
			JSONObject obj = (JSONObject) JSONValue.parse(json);
			String userId = obj.get("user_id").toString();
			
			if (user.contains(userId)) {
				bw.write(userId);
				bw.write(",");
				String reviewCount = obj.get("review_count").toString();
				bw.write(reviewCount);
				bw.write(",");
				String avgStars = obj.get("average_stars").toString();
				bw.write(avgStars);
				bw.write(",");
				String yelpingSince = obj.get("yelping_since").toString();
				bw.write(yelpingSince);
				bw.write(",");
				bw.write(obj.get("name").toString());

				// // String userId = obj.get("user_id").toString();
				// JSONObject hour = (JSONObject) obj.get("hours");
				// for (String d : daysofweek) {
				// String[] hr = getOpenCloseHours(hour, d);
				// bw.write("\t");
				// bw.write(hr[0]);
				// bw.write("\t");
				// bw.write(hr[1]);

				// }
				bw.write("\n");
				restCnt++;
			}
			cnt++;
		}
		br2.close();
		System.out.println("Total Reviews:" + cnt);
		System.out.println("Users:" + restCnt);
		bw.close();
	}

}
