package com.sjsu.cmpe239.yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class FeatureExtraction
{
    static class Business
    {
        double latitude;
        double longitude;
        float avgStars;

        public Business(JSONObject obj)
        {

            latitude = Double.parseDouble(obj.get("latitude").toString());
            longitude = Double.parseDouble(obj.get("longitude").toString());
            avgStars = Float.parseFloat(obj.get("stars").toString());
        }

    }

    static final int PREV_REST = 4;

    public static void main(String[] args) throws IOException
    {
        BufferedReader br1 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/users.csv")));

        Map<String, String> userAvgStars = new HashMap<String, String>();
        String line = null;
        while ((line = br1.readLine()) != null)
        {
            String[] temp = line.split(",");
            userAvgStars.put(temp[0], temp[2]);
        }
        br1.close();
        BufferedReader br2 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/business.json")));
        String json = null;
        Map<String, Business> businessMap = new HashMap<String, FeatureExtraction.Business>();
        while ((json = br2.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            String businessId = obj.get("business_id").toString();
            Business b = new Business(obj);
            businessMap.put(businessId, b);
        }
        br2.close();
        // TODO change here
        String option = "TRAIN";
        String fn, out, out2;
        if (option.equals("TRAIN"))
        {
            // this is for training feature generation
            fn = "/data/239/yelp/predOut/trainRestaurantIds.txt";
            out = "/data/239/yelp/predOut/features.txt";
            out2 = "/data/239/yelp/predOut/featuresWithInput.txt";
        }
        else
        {
            // for scoring random users and restaurants for final output demo
            fn = "/data/239/yelp/predOut/allPredRestIDs.txt";
            out = "/data/239/yelp/predOut/allfeatures.txt";
            out2 = "/data/239/yelp/predOut/allfeaturesWithInput.txt";
        }

        BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));

        BufferedWriter bw = new BufferedWriter(new FileWriter(out));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(out2));

        int totCnt = 0;

        while ((line = br3.readLine()) != null)
        {
            String[] temp = line.split("\t");
            String label = temp[0].equals("-1") ? "0" : temp[0];
            String as = userAvgStars.get(temp[1]);
            Business targetRest = businessMap.get(temp[2]);
            Business[] prevRests = new Business[PREV_REST];
            for (int i = 0; i < PREV_REST; i++)
            {
                prevRests[i] = businessMap.get(temp[i + 3]);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(label).append(" 1:").append(as).append(" 2:").append(getRating(targetRest));
            int index = 3;
            float sum = 0.0F;
            int cnt = 0;
            for (int i = 0; i < PREV_REST; i++)
            {
                float dist = getDistance(targetRest, prevRests[i]);
                sb.append(" ").append(index).append(":").append(dist);
                index++;
                float rating = getRating(prevRests[i]);
                if (rating > 0)
                {
                    sum += rating;
                    cnt++;
                }
                sb.append(" ").append(index).append(":").append(rating);
                index++;
            }
            sb.append(" ").append(index).append(":").append((float) (sum / (float) cnt));
            String ftrs = sb.toString();
            bw.write(ftrs);
            bw.write("\n");
            bw2.write(line.replace('\t', ',').substring(2));
            bw2.write("\t");
            bw2.write(ftrs.replaceAll(" \\d+:", ",").substring(2));
            bw2.write("\n");
            totCnt++;
        }
        bw.close();
        bw2.close();
        br3.close();
        System.out.println("Done.." + totCnt);
    }

    private static float getDistance(Business targetRest, Business business)
    {
        if (targetRest == null || business == null)
        {
            return -1.0F;
        }
        return (float) (Math.sqrt((targetRest.latitude - business.latitude) * (targetRest.latitude - business.latitude)
                + (targetRest.longitude - business.longitude) * (targetRest.longitude - business.longitude)));
    }

    private static float getRating(Business rest)
    {
        if (rest == null)
        {
            return -1.0F;
        }
        else
            return rest.avgStars;
    }
}
