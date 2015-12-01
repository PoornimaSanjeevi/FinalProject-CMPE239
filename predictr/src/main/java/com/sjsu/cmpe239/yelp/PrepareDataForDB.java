package com.sjsu.cmpe239.yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class PrepareDataForDB
{
    static final int PREV_REST = 4;

    static class Business implements Comparable<Business>
    {
        public Business(String id, double score)
        {
            super();
            this.id = id;
            this.score = score;
        }

        String id;
        double score;

        @Override
        public int compareTo(Business o)
        {
            if (score > o.score)
            {
                return 1;
            }
            else if (score < o.score)
            {
                return -1;
            }
            return 0;
        }

    }

    public static void main(String[] args) throws IOException
    {
        BufferedReader br1 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/users.csv")));

        Map<String, String> userNames = new HashMap<String, String>();
        String line = null;
        while ((line = br1.readLine()) != null)
        {
            String[] temp = line.split(",");
            userNames.put(temp[0], temp[4]);
        }
        br1.close();

        BufferedReader br4 = new BufferedReader(new InputStreamReader(new FileInputStream(
                "/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_review.json")));
        String json = null;
        Map<String, String> reviewMap = new HashMap<String, String>();
        while ((json = br4.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            String businessId = obj.get("business_id").toString();
            String userId = obj.get("user_id").toString();
            reviewMap.put(userId + ":" + businessId, obj.get("stars").toString());
        }
        br4.close();
        BufferedReader br2 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/business.json")));
        json = null;
        Map<String, String> businessMap = new HashMap<String, String>();
        while ((json = br2.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            String businessId = obj.get("business_id").toString();
            String name = obj.get("name").toString();
            String category = obj.get("categories").toString();
            String reviewCount = obj.get("review_count").toString();
            JSONObject attr = (JSONObject) obj.get("attributes");
            String priceRange = attr.containsKey("Price Range") ? attr.get("Price Range").toString() : "0";
            String stars = obj.get("stars").toString();
            String city = obj.get("city").toString();
            String state = obj.get("state").toString();
            StringBuilder sb = new StringBuilder();
            sb.append(name.replace(':', ';'));
            sb.append(":");
            sb.append(category.replace(':', ';'));
            sb.append(":");
            sb.append(stars.replace(':', ';'));
            sb.append(":");
            sb.append(reviewCount.replace(':', ';'));
            sb.append(":");
            sb.append(priceRange.replace(':', ';'));
            sb.append(":");
            sb.append(city.replace(':', ';'));
            sb.append(":");
            sb.append(state.replace(':', ';'));
            businessMap.put(businessId, sb.toString());
        }
        br2.close();
        String fn = "/data/239/yelp/predOut/joinScores.txt";
        String out = "/data/239/yelp/predOut/dbInput.txt";
        String out2 = "/data/239/yelp/predOut/topRests.txt";

        BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));

        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        int totCnt = 0, minus1 = 0;

        Map<String, List<Business>> topBusiness = new HashMap<String, List<Business>>();
        while ((line = br3.readLine()) != null)
        {
            String[] temp = line.split(",");
            bw.write(temp[0]);
            bw.write("\t");
            bw.write(temp[1]);
            bw.write("\t");
            bw.write(userNames.get(temp[0]));
            List<Business> hotelList = topBusiness.get(temp[0]);
            if (hotelList == null)
            {
                hotelList = new ArrayList<PrepareDataForDB.Business>();
                topBusiness.put(temp[0], hotelList);
            }
            hotelList.add(new Business(temp[1], Double.parseDouble(temp[PREV_REST + 2])));
            for (int i = 1; i <= PREV_REST + 1; i++)
            {
                bw.write("\t");
                if (temp[i].equals("-1"))
                {
                    bw.write("NA");
                    minus1++;
                }
                else
                {
                    String userReview = reviewMap.get(temp[0] + ":" + temp[i]);
                    bw.write(businessMap.get(temp[i]) + ":0.0:" + (userReview == null ? "0" : userReview));
                }
            }
            bw.write("\t");
            bw.write(temp[PREV_REST + 2]);
            bw.write("\n");
            totCnt++;
        }
        bw.close();
        br3.close();

        BufferedWriter bw2 = new BufferedWriter(new FileWriter(out2));
        for (String userId : topBusiness.keySet())
        {
            List<Business> hotels = topBusiness.get(userId);
            Collections.sort(hotels, Collections.reverseOrder());
            bw2.write(userId);
            bw2.write("\t");
            bw2.write(userNames.get(userId));
            bw2.write("\t");
            bw2.write(businessMap.get(hotels.get(0).id) + ":" + hotels.get(0).score + ":0");
            bw2.write("\t");
            if (hotels.size() > 1)
            {
                bw2.write(businessMap.get(hotels.get(1).id) + ":" + hotels.get(0).score+ ":0");
            }
            else
            {
                bw2.write("NA");
            }
            bw2.write("\n");
        }
        bw2.close();
        System.out.println("Done.." + totCnt + " -1:" + minus1);
    }
}
