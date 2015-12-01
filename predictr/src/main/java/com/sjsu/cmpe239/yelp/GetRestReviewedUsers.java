package com.sjsu.cmpe239.yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Created by poornima on 11/19/15.
 */
public class GetRestReviewedUsers
{
    public static void main(String[] args) throws IOException
    {
        Set<String> restaurants = new HashSet<String>();
        BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(
                "/data/239/yelp/predOut/restaurantIds.txt")));
        String line = null;
        while ((line = br1.readLine()) != null)
        {
            restaurants.add(line);
        }
        br1.close();

        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(
                "/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_review.json")));

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                "/data/239/yelp/predOut/restRevUsers.txt")));
        String json = null;
        int cnt = 0, restCnt = 0;
        Map<String, List<String>> users = new HashMap<String, List<String>>();
        while ((json = br2.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            String busId = obj.get("business_id").toString();
            if (restaurants.contains(busId))
            {
                String userId = obj.get("user_id").toString();
                List<String> rests = users.get(userId);
                if (rests == null)
                {
                    rests = new ArrayList<String>();
                    users.put(userId, rests);
                }
                rests.add(busId);
                restCnt++;
            }
            cnt++;
        }
        br2.close();
        System.out.println("Total Reviews:" + cnt);
        System.out.println("Restaurants:" + restCnt);
        int train = 0, rest = 0;
        for (String u : users.keySet())
        {
            List<String> rests = users.get(u);
            if (rests.size() > 2)
            {
                for (String r : rests)
                {
                    bw.write(u);
                    bw.write("\t");
                    bw.write(r);
                    bw.write("\n");
                }
                train++;
            }
            else
            {
                rest++;
            }
        }
        System.out.println("Rest review Users<=2:" + rest);
        System.out.println("Rest review Users>2:" + train);
        bw.close();
    }
}
