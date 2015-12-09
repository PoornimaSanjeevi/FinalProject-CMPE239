import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class tipParser {

private static final String filePath = "C:\\Users\\dsawla\\Downloads\\CmpE 239\\csv\\xls\\tip.json";

public static void main(String[] args)
{
	try
	{
		HashMap<String,Integer> getTipCount = new HashMap();
		ArrayList<String> topBusinessList = new ArrayList();
		ArrayList<String> lowBusinessList = new ArrayList();
		FileReader reader = new FileReader(filePath);
		JSONParser  jsonParser = new JSONParser();
		JSONArray level1Obj = (JSONArray) jsonParser.parse(reader);
		
		for(int i=0; i<level1Obj.size();i++)
		{
			JSONObject obj = (JSONObject) level1Obj.get(i);
			String bid = (String) obj.get("business_id");
			
			if(! getTipCount.containsKey(bid))
			{
				getTipCount.put(bid, 1);
			}
			
			else
			{
				getTipCount.put(bid,getTipCount.get(bid) + 1);
			}
		}
		
		System.out.println(getTipCount);
		int maxValue = Collections.max(getTipCount.values());
		System.out.println("Max Value of tips : " +  maxValue);
		
		//Min value of checkins
		int minValue = Collections.min(getTipCount.values());
		System.out.println("Minimum value of tips : " + minValue);
		
		//Get key for max value
		for(Entry<String,Integer> mpEntry : getTipCount.entrySet())
		{
			if (mpEntry.getValue() == maxValue)
			{
				topBusinessList.add(mpEntry.getKey());
			}
		}
		
		//Get key for min value
		for(Entry<String,Integer> mpEntry : getTipCount.entrySet())
		{
			if (mpEntry.getValue() == minValue)
			{
				lowBusinessList.add(mpEntry.getKey());
			}
		}
		
		System.out.println("Top business are: " + topBusinessList + "\n" + " Number :" + topBusinessList.size());
		
		//System.out.println("Low businesses are: " + lowBusinessList + "\n" + " Number :" + lowBusinessList.size());
		
		
		
		DefaultPieDataset dataset = new DefaultPieDataset( );
		for(Entry<String, Integer> mpEntry : getTipCount.entrySet())
		{
			dataset.setValue(mpEntry.getKey(), new Double( mpEntry.getValue()) );
		}
			
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	
}
	
}