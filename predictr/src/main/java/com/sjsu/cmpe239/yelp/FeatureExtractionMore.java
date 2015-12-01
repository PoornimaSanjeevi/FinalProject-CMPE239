package com.sjsu.cmpe239.yelp;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.*;

public class FeatureExtractionMore
{
    static class Business
    {
        double latitude;
        double longitude;
        float avgStars;
        int reviewCount;
        String categories;
        String city;
        String alcohol;
        String noiseLevel;
        boolean goodForKids;
        int priceRange;
        boolean smoking;
        boolean outDoorSeating;
        boolean takesReservation;
        boolean waiterService;
        boolean wifi;
        boolean goodForGroups;

        public Business(JSONObject obj)
        {
            latitude = Double.parseDouble(obj.get("latitude").toString());
            longitude = Double.parseDouble(obj.get("longitude").toString());
            avgStars = Float.parseFloat(obj.get("stars").toString());
            reviewCount = Integer.parseInt(obj.get("review_count").toString());
            categories = obj.get("categories").toString();
            alcohol = obj.containsKey("Alcohol") ? obj.get("Alcohol").toString() : "none";
            noiseLevel = obj.containsKey("Noise Level") ? obj.get("Noise Level").toString() : "average";
            city = obj.get("city").toString();
            JSONObject attributes = (JSONObject) obj.get("attributes");
            if (attributes.containsKey("Price Range"))
            {
                priceRange = Integer.parseInt(attributes.get("Price Range").toString());
            }
            else
            {
                priceRange = 0;
            }
            goodForKids = getBool(attributes, "Good for Kids");
            smoking = getBool(attributes, "smoking");
            outDoorSeating = getBool(attributes, "Outdoor Seating");
            takesReservation = getBool(attributes, "Takes Reservations");
            waiterService = getBool(attributes, "Waiter Service");
            wifi = attributes.containsKey("Wi-Fi") ? !attributes.get("Wi-Fi").toString().equalsIgnoreCase("no") : false;
            goodForGroups = getBool(attributes, "Good For Groups");
        }

        private boolean getBool(JSONObject attributes, String k)
        {
            if (attributes.containsKey(k))
            {
                Boolean.parseBoolean(attributes.get(k).toString());
            }
            return false;
        }
    }

    private static final int PREV_REST = 4;

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
        Map<String, Business> businessMap = new HashMap<String, FeatureExtractionMore.Business>();
        while ((json = br2.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            String businessId = obj.get("business_id").toString();
            Business b = new Business(obj);
            businessMap.put(businessId, b);
        }
        br2.close();

        BufferedReader br4 = new BufferedReader(new InputStreamReader(new FileInputStream(
                "/data/239/yelp/yelp_dataset_challenge_academic_dataset/yelp_academic_dataset_review.json")));
        json = null;
        Map<String, String> reviewMap = new HashMap<String, String>();
        while ((json = br4.readLine()) != null)
        {
            JSONObject obj = (JSONObject) JSONValue.parse(json);
            String businessId = obj.get("business_id").toString();
            String userId = obj.get("user_id").toString();
            reviewMap.put(userId + ":" + businessId, obj.get("stars").toString());
        }
        br4.close();
        // TODO change here
        String option = "PREDICT";
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
            sb.append(" 3:").append(getReviewCount(targetRest)).append(" 4:").append(targetRest.priceRange);
            int index = 5;
            Map<String, Integer> categCounts = new HashMap<String, Integer>();
            String[] targetCateg = targetRest.categories.substring(1,
                    targetRest.categories.length() - 1).toLowerCase().split(",");
            List<String> targetCategList = getList(targetCateg, null);
            for (int i = 0; i < PREV_REST; i++)
            {
                float dist = getDistance(targetRest, prevRests[i]);
                sb.append(" ").append(index).append(":").append(dist);
                index++;
                float rating = getRating(prevRests[i]);
                sb.append(" ").append(index).append(":").append(rating);
                index++;
                String userRating = reviewMap.get(temp[1] + ":" + temp[i + 3]);
                if (userRating == null)
                {
                    userRating = "-1.0";
                }
                sb.append(" ").append(index).append(":").append(userRating);
                index++;
                index = addCompFtrs(targetRest, prevRests[i], index, sb);
                index = addCategFtrs(targetCategList, prevRests[i], index, sb, categCounts);
                index = addClassFtrs(targetRest, prevRests[i], index, sb);
            }
            print = true;
            int totalMatch = 0;
            for (String s : targetCategList)
            {
                if (categCounts.containsKey(s))
                {
                    totalMatch += categCounts.get(s);
                }
            }
            sb.append(" ").append(index).append(":").append(totalMatch);
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

    private static int addCategFtrs(List<String> targetCategList, Business prevRest, int index, StringBuilder sb,
            Map<String, Integer> categCounts)
    {
        if (prevRest == null)
        {
            sb.append(" ").append(index).append(":0");
            index++;
            sb.append(" ").append(index).append(":0");
        }
        else
        {
            String[] prevCateg = prevRest.categories.substring(1, prevRest.categories.length() - 1).toLowerCase().split(
                    ",");

            double[] res = similarity(targetCategList, getList(prevCateg, categCounts));
            sb.append(" ").append(index).append(":").append(res[0]);
            index++;
            sb.append(" ").append(index).append(":").append(res[1]);
        }
        index++;
        return index;
    }

    private static List<String> getList(String[] targetCateg, Map<String, Integer> categCounts)
    {
        List<String> x = new ArrayList<String>();
        for (String s : targetCateg)
        {
            s = s.trim();
            if (!s.equals("\"restaurants\""))
            {
                x.add(s);
                if (categCounts != null)
                {
                    if (categCounts.containsKey(s))
                    {
                        categCounts.put(s, categCounts.get(s) + 1);
                    }
                    else
                    {
                        categCounts.put(s, 1);
                    }
                }
            }
        }
        return x;
    }

    private static double[] similarity(List<String> x, List<String> y)
    {
        double[] res = new double[2];
        if (x.size() == 0 || y.size() == 0)
        {
            res[0] = res[1] = 0.0;
            return res;
        }
        Set<String> unionXY = new HashSet<String>(x);
        unionXY.addAll(y);

        Set<String> intersectionXY = new HashSet<String>(x);
        intersectionXY.retainAll(y);
        res[0] = (double) intersectionXY.size() / (double) unionXY.size();
        res[1] = intersectionXY.size();
        return res;
    }

    private static int addCompFtrs(Business targetRest, Business prevRest, int index, StringBuilder sb)
    {
        sb.append(" ").append(index).append(":").append(
                prevRest == null ? targetRest.reviewCount : targetRest.reviewCount - prevRest.reviewCount);
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? targetRest.priceRange : targetRest.priceRange - prevRest.priceRange);
        index++;
        return index;
    }

    static boolean print = false;

    private static int addClassFtrs(Business targetRest, Business prevRest, int index, StringBuilder sb)
    {
        if (!print)
        {
            System.out.println("Start:" + index);
        }
        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.alcohol.equals(prevRest.alcohol) ? 1 : 2));
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.noiseLevel.equals(prevRest.noiseLevel) ? 1 : 2));
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.city.equals(prevRest.city) ? 1 : 2));
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.goodForKids == prevRest.goodForKids) ? 1 : 2);
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.smoking == prevRest.smoking) ? 1 : 2);
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.outDoorSeating == prevRest.outDoorSeating) ? 1 : 2);
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.takesReservation == prevRest.takesReservation) ? 1 : 2);
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.waiterService == prevRest.waiterService) ? 1 : 2);
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.wifi == prevRest.wifi) ? 1 : 2);
        index++;

        sb.append(" ").append(index).append(":").append(
                prevRest == null ? 0 : (targetRest.goodForGroups == prevRest.goodForGroups) ? 1 : 2);
        if (!print)
        {
            System.out.println("End:" + index);
        }
        index++;
        return index;
    }

    private static int getReviewCount(Business targetRest)
    {
        if (targetRest == null)
        {
            return -1;
        }
        return targetRest.reviewCount;
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
