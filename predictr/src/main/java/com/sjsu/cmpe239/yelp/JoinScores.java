package com.sjsu.cmpe239.yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class JoinScores
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader br1 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/allfeaturesWithInput.txt")));
        String line = null;
        String[] inputs = new String[5286731];
        String[] feats = new String[5286731];
        int i = 0;
        while ((line = br1.readLine()) != null)
        {
            String[] p = line.split("\t");
            inputs[i] = p[0];
            feats[i] = p[1];
            i++;
        }
        br1.close();

        BufferedReader br2 = new BufferedReader(
                new InputStreamReader(new FileInputStream("/data/239/yelp/predOut/allScores.txt")));
        System.err.println("Loaded inputs...");
        i = 0;
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("/data/239/yelp/predOut/joinScores.txt")));
        int mismatch = 0;
        while ((line = br2.readLine()) != null)
        {
            String[] parts = line.split("\\],\\[");
            String k = parts[1].replaceAll("\\]\\)\\)", "");
            String v = parts[0].split(",")[0].substring(1);
            bw.write(inputs[i]);
            bw.write(",");
            bw.write(v);
            bw.write("\n");
            i++;
            if (i % 10000 == 0)
            {
                System.err.println("i:" + i + " mm:" + mismatch);
            }
        }
        br2.close();
        bw.close();
        System.out.println("mismatch: " + mismatch);
    }
}
