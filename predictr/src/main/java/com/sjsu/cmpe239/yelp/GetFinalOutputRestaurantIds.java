package com.sjsu.cmpe239.yelp;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.*;

/**
 * Created by poornima on 11/19/15.
 */
public class GetFinalOutputRestaurantIds
{
    static class RestaurantDate implements Comparable<RestaurantDate>
    {
        String date;
        String restaurantId;
        String state;

        public RestaurantDate(String date, String restaurantId, String state)
        {
            super();
            this.date = date;
            this.restaurantId = restaurantId;
            this.state = state;
        }

        @Override
        public int compareTo(RestaurantDate o)
        {
            // TODO Auto-generated method stub
            int cmp = date.compareTo(o.date);
            if (cmp == 0)
            {

                return restaurantId.compareTo(o.restaurantId);
            }
            return cmp;
        }
    }

    static String getRandomNegativeRestaurant(List<String> allrests, Set<String> positiverests)
    {
        Random rand = new Random();
        while (true)
        {
            int index = rand.nextInt(allrests.size());
            String res = allrests.get(index);
            if (!positiverests.contains(res))
            {
                return res;
            }
        }
    }

    public static void main(String[] args) throws IOException
    {
        Map<String, String> restaurants = new HashMap<String, String>();
        BufferedReader br1 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/restaurantCities.txt")));
        String line = null;
        while ((line = br1.readLine()) != null)
        {
            String[] p = line.split("\t");
            restaurants.put(p[0], p[1]);
        }
        br1.close();

        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(
                "/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_review.json")));

        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("/data/239/yelp/predOut/allPredRestIDs.txt")));
        String json = null;
        int cnt = 0, restCnt = 0;
        Map<String, List<RestaurantDate>> users = new HashMap<String, List<RestaurantDate>>();
        while ((json = br2.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            String busId = obj.get("business_id").toString();
            if (restaurants.containsKey(busId))
            {
                String userId = obj.get("user_id").toString();
                String date = obj.get("date").toString();
                List<RestaurantDate> rests = users.get(userId);
                if (rests == null)
                {
                    rests = new ArrayList<RestaurantDate>();
                    users.put(userId, rests);
                }
                rests.add(new RestaurantDate(date, busId, restaurants.get(busId)));
                restCnt++;
            }
            cnt++;
        }
        br2.close();
        System.out.println("Total Reviews:" + cnt);
        System.out.println("Restaurants:" + restCnt);
        int train = 0, rest = 0, minuscount = 0, restidcount = 0;
        int allcnt = 0;
        for (String u : users.keySet())
        {
            List<RestaurantDate> rests = users.get(u);
            Set<String> prevStates = new HashSet<String>();

            if (rests.size() > 2 && Math.random() < 0.01)
            {
                Collections.sort(rests);
                Collections.reverse(rests);
                restidcount++;
                // bw.write("1");
                // bw.write("\t");
                StringBuilder sb = new StringBuilder();
                Set<String> alreadyVisRests = new HashSet<String>();
                for (int i = 0; i < 4; i++)
                {
                    sb.append("\t");
                    if (rests.size() > i)
                    {
                        prevStates.add(rests.get(i).state);
                        sb.append(rests.get(i).restaurantId);
                        alreadyVisRests.add(rests.get(i).restaurantId);
                    }
                    else
                    {
                        sb.append("-1");
                    }
                } String prevRests = sb.toString();
                for (String restId : restaurants.keySet())
                {
                    String state = restaurants.get(restId);
                    if (!alreadyVisRests.contains(restId) && prevStates.contains(state) && Math.random() < 0.05)
                    {
                        bw.write("1\t");
                        bw.write(u);
                        bw.write("\t");
                        bw.write(restId);
                        bw.write(prevRests);
                        bw.write("\n");
                        allcnt++;
                    }
                }
            }
            else
            {
                rest++;
            }
        } System.out.println("Rest review Users<=2:" + rest);
        System.out.println("Rest review Users>2:" + train);
        System.out.println("Training restaurant ids:" + restidcount);
        System.out.println("All Cnt -1:" + allcnt);
        bw.close();
    }
}
