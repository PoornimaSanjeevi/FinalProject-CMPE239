

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ReviewParser {

	private static final String filePath = "C:\\Users\\dsawla\\Downloads\\CmpE 239\\csv\\xls\\review_small_1.json";

	public static void writeCSVFile(String fileName) throws Exception
	{
		FileReader reader = new FileReader(filePath);
		JSONParser  jsonParser = new JSONParser();
		JSONArray level1Obj = (JSONArray) jsonParser.parse(reader);

		ArrayList<UserReview> reviews = new ArrayList();
		HashMap<String,Integer> mp = new HashMap();
		HashMap<String,Integer> mpBusiness = new HashMap();
		int count=1;
		int countBusiness = 1;
		for(int i=0; i<level1Obj.size();i++)
		{
			JSONObject obj = (JSONObject) level1Obj.get(i);
			String bid = (String) obj.get("business_id");
			String uid = (String) obj.get("user_id");
			Long stars = (Long) obj.get("stars");
			UserReview userReview = new UserReview();
			userReview.setBusinessid(bid);
			userReview.setUserid(uid);

			if(!mp.containsKey(uid))
			{
				mp.put(uid, count);
				count = count + 1;
			}

			if(!mpBusiness.containsKey(bid))
			{
				mpBusiness.put(bid, countBusiness);
				countBusiness = countBusiness + 1;
			}
			userReview.setStars(stars);

			reviews.add(userReview);
		}

		System.out.println(mp);
		System.out.println(mpBusiness);


		FileWriter fileWriter = null;	
		//Once the review object is generated, we create a csv for it
		fileWriter = new FileWriter(fileName);

		for(UserReview ur : reviews)
		{
			String uid = ur.getUserid();
			fileWriter.append(String.valueOf(mp.get(uid)));
			fileWriter.append(",");
			String bid = ur.getBusinessid();
			fileWriter.append(String.valueOf(mpBusiness.get(bid)));
			fileWriter.append(",");
			fileWriter.append(String.valueOf(ur.getStars()));
			fileWriter.append("\n");
		}
		fileWriter.flush();
		fileWriter.close();
		System.out.println("CSV Generated successfully");
	}

	public static void main(String args[]) throws Exception
	{
		String fileName = "C:\\Users\\dsawla\\Downloads\\CmpE 239\\csv\\xls\\review.csv";
		System.out.println("Write File");
		writeCSVFile(fileName);
	}

}

