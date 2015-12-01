package com.sjsu.cmpe239.yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Created by poornima on 11/19/15.
 */
public class GetTrainingRestaurantIds
{
    static class RestaurantDate implements Comparable<RestaurantDate>
    {
        String date;
        String restaurantId;

        public RestaurantDate(String date, String restaurantId)
        {
            super();
            this.date = date;
            this.restaurantId = restaurantId;
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
        int att = 0;
        while (true)
        {
            int index = rand.nextInt(allrests.size());
            String res = allrests.get(index);
            if (!positiverests.contains(res))
            {
                return res;
            }
            else
            {
                att++;
            }
            if (att > 10)
            {
                return null;
            }
        }
    }

    public static void main(String[] args) throws IOException
    {
        Map<String, String> restaurants = new HashMap<String, String>();
        Map<String, List<String>> restList = new HashMap<String, List<String>>();
        BufferedReader br1 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/restaurantCities.txt")));
        String line = null;
        while ((line = br1.readLine()) != null)
        {
            String[] p = line.split("\t");
            restaurants.put(p[0], p[1]);
            List<String> cityRests = restList.get(p[1]);
            if (cityRests == null)
            {
                cityRests = new ArrayList<String>();
                restList.put(p[1], cityRests);
            }
            cityRests.add(p[0]);
        }
        br1.close();

        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(
                "/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_review.json")));

        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("/data/239/yelp/predOut/trainRestaurantIds.txt")));
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
                rests.add(new RestaurantDate(date, busId));
                restCnt++;
            }
            cnt++;
        }
        br2.close();
        System.out.println("Total Reviews:" + cnt);
        System.out.println("Restaurants:" + restCnt);
        int train = 0, rest = 0, minuscount = 0, restidcount = 0;
        for (String u : users.keySet())
        {
            List<RestaurantDate> rests = users.get(u);
            Set<String> positiverest = new HashSet<String>();

            if (rests.size() > 2)
            {
                Collections.sort(rests);
                Collections.reverse(rests);
                bw.write("1");
                bw.write("\t");
                bw.write(u);
                bw.write("\t");
                bw.write(rests.get(0).restaurantId);
                positiverest.add(rests.get(0).restaurantId);
                bw.write("\t");
                for (int i = 1; i <= 4; i++)
                {
                    if (rests.size() > i)
                    {
                        positiverest.add(rests.get(i).restaurantId);
                        bw.write(rests.get(i).restaurantId);
                        restidcount++;
                    }
                    else
                    {
                        bw.write("-1");
                        minuscount++;
                    }
                    bw.write("\t");

                }
                bw.write("\n");
                /* negative data set */
                int randomno;
                Random randomGenerator = new Random();
                randomno = randomGenerator.nextInt(10);
                String city = restaurants.get(rests.get(0).restaurantId);
                for (int j = 0; j < randomno; j++)
                {
                    String randomRest = getRandomNegativeRestaurant(restList.get(city), positiverest);
                    if (randomRest == null)
                    {
                        break;
                    }
                    bw.write("-1");
                    bw.write("\t");
                    bw.write(u);
                    bw.write("\t");
                    bw.write(randomRest);
                    positiverest.add(randomRest);
                    bw.write("\t");
                    for (int i = 1; i <= 4; i++)
                    {
                        if (rests.size() > i)
                        {
                            bw.write(rests.get(i).restaurantId);
                            restidcount++;
                        }
                        else
                        {
                            bw.write("-1");
                            minuscount++;
                        }
                        bw.write("\t");
                    }
                    bw.write("\n");
                }
                train++;
            }
            else
            {
                rest++;
            }
            if (train % 100000 == 0)
            {
                System.out.println("gen.." + train);
            }
        }
        System.out.println("Rest review Users<=2:" + rest);
        System.out.println("Rest review Users>2:" + train);
        System.out.println("Training restaurant ids:" + restidcount);
        System.out.println("Minus -1:" + minuscount);
        bw.close();
    }
}
