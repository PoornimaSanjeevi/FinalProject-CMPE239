package com.sjsu.cmpe239.yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Created by poornima on 11/19/15.
 */
public class GetRestaurantCity
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                "/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_business.json")));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                "/data/239/yelp/predOut/restaurantCities.txt")));
        String json = null;
        int cnt = 0, restCnt = 0;
        while ((json = br.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            JSONArray cat = (JSONArray) obj.get("categories");
            Iterator iter = cat.iterator();
            boolean isRest = false;
            while (iter.hasNext())
            {
                if (iter.next().toString().equals("Restaurants"))
                {
                    isRest = true;
                }
            }
            if (isRest)
            {
                bw.write(obj.get("business_id").toString());
                bw.write("\t");
                bw.write(obj.get("city").toString());
                bw.write("\n");
                restCnt++;
            }
            cnt++;
        }
        System.out.println("Total Business:" + cnt);
        System.out.println("Restaurants:" + restCnt);
        br.close();
        bw.close();
    }
}
