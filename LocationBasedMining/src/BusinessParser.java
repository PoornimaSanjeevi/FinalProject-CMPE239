import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import org.json.simple.parser.ParseException;

public class BusinessParser {

	private static final String filePath = 
			"C:\\Users\\dsawla\\Downloads\\CmpE 239\\csv\\yelp_academic_dataset_business.csv";

	public static void main(String[] args) throws IOException, ParseException
	{
		HashMap<String,JSONArray> getBusinessCategory = new HashMap();
		ArrayList<String> pubsAndBars = new ArrayList();
		ArrayList<String> restaurants = new ArrayList();
		ArrayList<String> shopping = new ArrayList();
		FileReader reader = new FileReader(filePath);
		JSONParser  jsonParser = new JSONParser();
		JSONArray level1Obj = (JSONArray) jsonParser.parse(reader);

		for(int i=0; i<level1Obj.size();i++)
		{
			JSONObject obj = (JSONObject) level1Obj.get(i);
			String bid = (String) obj.get("business_id");
			JSONArray categoryList = (JSONArray) obj.get("categories");
			getBusinessCategory.put(bid,categoryList );

		}

		System.out.println(getBusinessCategory);
		//int numberOfBusinesses = getBusinessCategory.size();
		//System.out.println(numberOfBusinesses);

		for(Entry<String,JSONArray> mpEntry :getBusinessCategory.entrySet())
		{
			if(mpEntry.getValue().contains("Pubs") || mpEntry.getValue().contains("Bars"))
			{
				pubsAndBars.add(mpEntry.getKey());
			}

			if(mpEntry.getValue().contains("Restaurants") )
			{
				restaurants.add(mpEntry.getKey());
			}

			if(mpEntry.getValue().contains("Shopping") )
			{
				shopping.add(mpEntry.getKey());
			}
		}

		System.out.println("Total No. of pubs and bars: " + pubsAndBars.size());
		System.out.println("Total no. of Restaurants: " + restaurants.size());
		System.out.println("Total no. of Shopping places: " + shopping.size());
	}
}


