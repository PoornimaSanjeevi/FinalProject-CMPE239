import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
//swing packages

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class LocationMining extends JPanel {

	private String inputFile;
	private String outputFile;
	private String[][] arr = null;
	private String[][] brr = null;
	private static int count = 0;
	private boolean flag[] = null;
	private WritableCellFormat timesBoldUnderline;
	private HashSet<String> zipcodes = new HashSet<String>();
	private List<String> headers = new ArrayList<String>();
	private HashMap<String, Integer> headermap = new HashMap<String, Integer>();
	private int index[] = new int[106];
	private int brr_rows = 0, arr_rows = 0;
	WritableCellFormat times;
	HashMap<String, String> data = new HashMap<String, String>();
	double similarity[]=null;
	double weighted_avg[]=null;
	double success = 0;
	double success2 = 0;
	double success3 = 0;
	private String location = null;

	//below code for swing

	private JTextField[] fields;

	// Create a form with the specified labels, tooltips, and sizes.
	public LocationMining(String[] labels, char[] mnemonics, int[] widths,
			String[] tips) {
		super(new BorderLayout());
		JPanel labelPanel = new JPanel(new GridLayout(labels.length, 1));
		JPanel fieldPanel = new JPanel(new GridLayout(labels.length, 1));
		add(labelPanel, BorderLayout.WEST);
		add(fieldPanel, BorderLayout.CENTER);
		fields = new JTextField[labels.length];
		setSize(2000, 2000);        // "super" Frame sets initial window size

		for (int i = 0; i < labels.length; i += 1) {
			fields[i] = new JTextField();
			if (i < tips.length)
				fields[i].setToolTipText(tips[i]);
			if (i < widths.length)
				fields[i].setColumns(widths[i]);

			JLabel lab = new JLabel(labels[i], JLabel.RIGHT);
			lab.setLabelFor(fields[i]);
			if (i < mnemonics.length)
				lab.setDisplayedMnemonic(mnemonics[i]);

			labelPanel.add(lab);
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(fields[i]);
			fieldPanel.add(p);
		}

		loadFormData(fields);
	}

	public void loadFormData(JTextField field[])
	{
		fields[0].setText("15104");
		fields[1].setText("none");
		fields[2].setText("true");
		fields[3].setText("true");
		fields[4].setText("true");
		fields[5].setText("true");
		fields[6].setText("true");
		fields[7].setText("true");
		fields[8].setText("true");
		fields[9].setText("true");
		fields[10].setText("true");
		fields[11].setText("free");
		fields[12].setText("1");
		fields[13].setText("average");
		fields[14].setText("outdoor");
		fields[15].setText("true");
		fields[16].setText("true");
	}

	public String getText(int i) {
		return (fields[i].getText());
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void read() throws IOException {
		File inputWorkbook = new File(inputFile);
		Workbook w;

		try {
			w = Workbook.getWorkbook(inputWorkbook);
			// Get the first sheet
			Sheet sheet = w.getSheet(0);
			// Loop over first 10 column and lines
			arr = new String[sheet.getRows()][sheet.getColumns()];
			flag = new boolean[sheet.getRows()];

			int r = sheet.getRows();
			int c = sheet.getColumns();

			for (int i = 0; i < r; i++) {
				if(sheet.getCell(9, i).getContents().trim().contains("restaurant") || sheet.getCell(9, i).getContents().trim().contains("Restaurant")) {
					flag[i] = true;
					count++;
					flag[i] = true;

					for (int j = 0; j < c; j++) {
						Cell cell = sheet.getCell(j, i);

						if(cell.getContents().trim().equals("")) {
							arr[i][j] = "";
						}
						else {
							arr[i][j] = cell.getContents().trim();
						}
					}
				}
			}
		}
		catch(Exception e)
		{
//			e.printStackTrace();
		}

		loadHeaders();
	}

	public void loadHeaders() {
		File inputWorkbook = new File(inputFile);
		Workbook w;
		Sheet sheet = null;

		try {
			w = Workbook.getWorkbook(inputWorkbook);
			// Get the first sheet
			sheet = w.getSheet(0);
		}
		catch(Exception e)
		{
		}

		for(int i=0; i<sheet.getColumns(); i++) {
			headers.add(sheet.getCell(i, 0).getContents().trim());
			headermap.put(sheet.getCell(i, 0).getContents().trim(), i);
		}

		System.out.println("*****Headers loaded*****");
	}

	public static void main(String[] args) throws Exception {
		//below code for swing

		String[] labels = {"Location", "Alcohol", "Classy Ambience", "Parking", "Touristy Ambience",
				"Waiter Service", "Good for Dinner", "Good for breakfast", "Accepts CC", "Good for lunch", "Good for Kids",
				"Wi-fi", "Price Range", "Noise Level", "Smoking", "Good for Groups", "Romantic Ambience",
				"Algorithm 1", "Algorithm 2", "Algorithm 3"};

		char[] mnemonics = { 'a','b','c','d','e','f','g','h','i','j','k','l','m',
				'n','o','p','q','r','s'};

		int[] widths = {15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15,15};

		String[] descs = { "First Name", "Middle Initial", "Last Name", "Age" };

		final LocationMining form = new LocationMining(labels, mnemonics, widths, descs);

		JButton submit = new JButton("Submit Form");

		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
/*				System.out.println(form.getText(0) + " " + form.getText(1)
				+ " " + form.getText(2) + " " + form.getText(3) + " " + form.getText(4) + " " + form.getText(5)
				+ " " + form.getText(6) + " " + form.getText(7) + " " + form.getText(8) + " " + form.getText(9)
				+ " " + form.getText(10) + " " + form.getText(11));
*/
				String inputFile = "C:\\Users\\dsawla\\Downloads\\CmpE 239\\csv\\xls\\yelp_academic_dataset_business.xls";
				String outputFile = "C:\\Users\\dsawla\\Downloads\\CmpE 239\\csv\\xls\\yelp_academic_dataset_business_rest.xls";
				String outputFile2 = "C:\\Users\\dsawla\\Downloads\\CmpE 239\\csv\\xls\\yelp_academic_dataset_business_similar_rest.xls";

				form.headers.clear();
				count = 0;

				//				ReadExcel test = new ReadExcel();
				try {
					String url = "https://www.zipcodeapi.com/rest/WMmzh10VVY5rjw5J26YAOrsJ58wOHhwD3SckUcfPblhuRneTtcuiDX1J5xyNO4qS/radius.json";
					//WMmzh10VVY5rjw5J26YAOrsJ58wOHhwD3SckUcfPblhuRneTtcuiDX1J5xyNO4qS
					form.loadData();
//					System.out.println("Location: **********************************" + form.location);
					url = form.makeURL(url, form.location, "30", "mile");

					try {
						form.findZip(url);
					}
					catch(Exception e2)
					{
//						System.out.println(e2.toString());
					}

					if(form.zipcodes.size() > 0)
					{
						//cut pasted the below lines here
						form.setInputFile(inputFile);
						form.read();
//						System.out.println("Count: " + count);

						form.setOutputFile(outputFile);
						form.write();
						//cut pasted the above lines here						
						form.findZipcodeinCSV(outputFile2);
						//generated a csv file of all the similar businesses

						//now find the jaccard similarity of all the attributes.
						String[] attributes = new String[106];
						form.loadAttributes(attributes);
						form.findJaccardSimilarity(attributes, outputFile2, attributes);
					}
					else
					{
						System.out.println("No nearby restaurants found to compare... Please put an appropriate zip code");
					}
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		JFrame f = new JFrame("Location Based Mining");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(form, BorderLayout.NORTH);
		JPanel p = new JPanel();
		p.add(submit);
		f.getContentPane().add(p, BorderLayout.SOUTH);
		f.pack();
		f.setVisible(true);
	}
	
	private void loadData() {
		// TODO Auto-generated method stub
		location = fields[0].getText();
		data.put("attributes.Alcohol", fields[1].getText()); //full_bar beer_and_wine
		data.put("attributes.Ambience.classy", fields[2].getText());			//	true false
		data.put("attributes.Parking.lot", fields[3].getText());				//	true false
		data.put("attributes.Ambience.touristy", fields[4].getText());		//	true false
		data.put("attributes.Waiter Service", fields[5].getText());			//	true false
		data.put("attributes.Good For.dinner", fields[6].getText());			//	true false
		data.put("attributes.Good For.breakfast", fields[7].getText());		//	true false
		data.put("attributes.Accepts Credit Cards", fields[8].getText());	//	true false
		data.put("attributes.Good For.lunch", fields[9].getText());			//  true false
		data.put("attributes.Good For Kids", fields[10].getText());			//  true false
		data.put("attributes.Wi-Fi", fields[11].getText());				    //  no free paid
		data.put("attributes.Price Range", fields[12].getText());				//  1 2 3
		data.put("attributes.Noise Level", fields[13].getText());			//	quiet average
		data.put("attributes.Smoking", fields[14].getText());				//  outdoor
		data.put("attributes.Good For Groups", fields[15].getText());			//  true false
		data.put("attributes.Ambience.romantic", fields[16].getText());		//	true false
	}

	private void loadAttributes(String[] attributes) {
		// TODO Auto-generated method stub
		attributes[0]="attributes.Alcohol";
		attributes[1]="attributes.Ambience.classy";
		attributes[2]="attributes.Parking.lot";
		attributes[3]="attributes.Ambience.touristy";
		attributes[4]="attributes.Waiter Service";
		attributes[5]="attributes.Good For.dinner";
		attributes[6]="attributes.Good For.breakfast";
		attributes[7]="attributes.Accepts Credit Cards";
		attributes[8]="attributes.Good For.lunch";
		attributes[9]="attributes.Good For Kids";
		attributes[10]="attributes.Wi-Fi";
		attributes[11]="attributes.Price Range";
		attributes[12]="attributes.Noise Level";
		attributes[13]="attributes.Smoking";
		attributes[14]="attributes.Good For Groups";
		attributes[15]="attributes.Ambience.romantic";
	}

	public void findJaccardSimilarity(String attr[], String outputFile2, String attributes[])
	{
		int exceptioncounter=0;
		File inputWorkbook = new File(outputFile2);
		Workbook w;
		Sheet inputsheet = null;

		try {
			w = Workbook.getWorkbook(inputWorkbook);
			// Get the first sheet
			inputsheet = w.getSheet(0);
		}
		catch(Exception e)
		{

		}

		//identify rows of the attributes
		for (int i=0; i<attributes.length; i++)
		{
			if(headermap.containsKey(attr[i])) {
				index[headermap.get(attr[i])] = 1;
			}
		}

		similarity=new double[brr_rows];
		weighted_avg=new double[brr_rows];

		System.out.println("*****Checking rules*****");
		System.out.println(brr_rows);
		/*System.out.println(brr[0][0] + brr[0][1] + brr[0][1]);
		System.out.println(brr[1][0] + brr[1][1] + brr[1][2]);
		 */


		for (int i=1; i<brr_rows; i++)
		{
			int count1=0;
			for (int j=0; j<105; j++)
			{
				if(data.containsKey(brr[0][j]))
				{
					count++;
					double temp = 0;
					try {
						temp = checkRule(brr[0][j], brr[i][j]);
					}
					catch(Exception e)
					{
//						exceptioncounter--;
					}

					similarity[i-1] += temp;
				}
			}

			try {
				if(brr[i][65] != null) {
					weighted_avg[i-1] = similarity[i-1] * Double.parseDouble(brr[i][65]);
					success3 += Double.parseDouble(brr[i][65]);
				}
				else
					weighted_avg[i-1] = 0;
			}
			catch(Exception e)
			{
				weighted_avg[i-1] = 0;
				e.printStackTrace();
			}

//			System.out.println("Jaccard for i[" + (i-1) + "]: " + similarity[i-1] + " * " + brr[i][65] + "--> " + weighted_avg[i-1] + " for ID: " + brr[i][16]);
			success+=weighted_avg[i-1];
			success2+=similarity[i-1];
		}

		success = success/(brr_rows-1);
		System.out.println("Algorithm 1, Success Factor: " + success + " %");

		success2 = (success2/(brr_rows-1)/16)*100;

		System.out.println("Algorithm 2, Success Factor: " + success2 + " %");

		success3 = (success3/(brr_rows-1)/5)*100;
		System.out.println("Algorithm 3, Success Factor: " + success3 + " %");

		fields[17].setText(success+"%");
		fields[18].setText(success2+"%");
		fields[19].setText(success3+"%");
		
		double error1 = 0;
		double error2 = 0;
		
		error1 = Math.abs(success2-success);
		error2 = Math.abs(success3-success);
		
		System.out.println("Error difference between Algorithm 1 and Algorithm 2: " + error1+" %");
		System.out.println("Error difference between Algorithm 1 and Algorithm 3: " + error2+" %");
		
	}

	private double checkRule(String string, String currentdata) {
		// TODO Auto-generated method stub
		String value = data.get(string);

		if(string.equals("attributes.Alcohol"))
		{
			//			System.out.println("attributes.Alcohol");
			if(value.equals(currentdata))
				return 1;

			if((value.equals("full_bar") || value.equals("beer_and_wine")) && (currentdata.equals("full_bar") || currentdata.equals("beer_and_wine")))
				return 0.5;

			return 0;
		}

		else if(string.equals("attributes.Wi-Fi"))
		{
			//			System.out.println("attributes.Wi-Fi");
			if(value.equals(currentdata))
				return 1;

			if((value.equals("free") || value.equals("paid")) && (currentdata.equals("free") || currentdata.equals("paid")))
				return 0.5;

			return 0;
		}

		else if(string.equals("attributes.Price Range"))
		{
//			System.out.println("attributes.Price Range");
//			System.out.println(value + ", " + currentdata + ".");

			if(value == " " || currentdata == " " || value == "" || currentdata == "" || value == null || currentdata == null)
				return 0;

			if(value.equals(currentdata))
				return 1;

			double v1=0, v2=0;

			try {
				v1 = Double.parseDouble(value);
				v2 = Double.parseDouble(currentdata);
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
				return 0;
			}

			return 1/Math.abs(v1-v2);
		}

		else if(string.equals("attributes.Noise Level"))
		{
			if(value == " " || currentdata == " ")
				return 0;

			if(value.equals(currentdata))
				return 1;

			int v1=0, v2=0;

			if(currentdata.equals("quite"))
				v1 = 1;
			if(currentdata.equals("average"))
				v1 = 2;
			if(currentdata.equals("loud"))
				v1 = 3;
			if(currentdata.equals("very_loud"))
				v1 = 4;

			if(value.equals("quite"))
				v2 = 1;
			if(value.equals("average"))
				v2 = 2;
			if(value.equals("loud"))
				v2 = 3;
			if(value.equals("very_loud"))
				v2 = 4;

			if(Math.abs(v1-v2) > 2)
				return 0;
			else
				return 1/Math.abs(v1-v2);
		}

		else if(string.equals("attributes.Smoking"))
		{
			//			System.out.println("attributes.Smoking");
			if(value.equals(currentdata))
				return 1;

			if((value.equals("yes") || value.equals("outdoor")) && (currentdata.equals("yes") || currentdata.equals("outdoor")))
				return 0.5;

			return 0;
		}

		else
		{
			if(value.equals(currentdata))
				return 1;
		}
		return 0;
	}

	private void findZipcodeinCSV(String outputFile2) throws Exception {
		// TODO Auto-generated method stub

		File inputWorkbook = new File(outputFile);
		Workbook w;
		Sheet inputsheet = null;

		try {
			w = Workbook.getWorkbook(inputWorkbook);
			// Get the first sheet
			inputsheet = w.getSheet(0);
		}
		catch(Exception e)
		{

		}

		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(true);

		File file = new File(outputFile2);
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("en", "EN"));
		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Report", 0);
		WritableSheet outputsheet = workbook.getSheet(0);
		//		System.out.println("Writing into similar file...");

		int r = inputsheet.getRows();
		int c = inputsheet.getColumns();

		brr = new String[r][c];

		int row = 0;

		for(int i=0; i<headers.size(); i++)
		{
			Cell cell = outputsheet.getCell(i, 0);
			//			System.out.print(headers.get(i) + " ");
			brr[0][i] = headers.get(i);
			addLabel(outputsheet, i, row, headers.get(i));
		}

		row = 1;

		for (int i = 1; i < r; i++) {
			String address = inputsheet.getCell(46, i).getContents().trim();
			String zip = address.substring(address.length() - 5, address.length());

			if(zipcodes.contains(zip))
			{
				if(arr[i][16]!=null) {
					for (int j = 0; j < c; j++) {
						brr[row][j] = arr[i][j];
						addLabel(outputsheet, j, row, arr[i][j]);
					}
					brr_rows++;
					row++;
				}
			}
		}

		workbook.write();
		workbook.close();
	}

/*	public void display()
	{
		for(int i=0; i<50; i++)
		{
			System.out.print("i: " + i);

			for(int j=0; j<arr[0].length; j++)
			{
				System.out.print(arr[i][j]);
			}
			System.out.println();
		}
		
		Iterator<String> itr = zipcodes.iterator();

		while(itr.hasNext())
		{
			System.out.println(itr.next());
		}
	}
*/
	public void write() throws IOException, WriteException {
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(true);

		File file = new File(outputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("en", "EN"));
		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Report", 0);
		WritableSheet sheet = workbook.getSheet(0);
		//		System.out.println("Writing file...");
		createContent(sheet);

		workbook.write();
		workbook.close();
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
		//		System.out.print(s + " ");
	}

	private void createContent(WritableSheet sheet) throws WriteException,
	RowsExceededException {

		int r = arr.length;
		int c = arr[0].length;
		int row = 0;
		for (int i = 0; i < r; i++) {
			if(flag[i]) {
				for (int j = 0; j < c; j++) {
					addLabel(sheet, j, row, arr[i][j]);
				}
				row++;
			}
		}
	}

	public String makeURL(String url, String zipcode, String dist, String unit)
	{
		StringBuilder sb = new StringBuilder(url + "/" + zipcode + "/" + dist + "/" + unit);
		return sb.toString();
	}

	public void findZip(String urlString)
	{
		URL url = null;
		HttpURLConnection conn = null;
		BufferedReader rd = null;
		StringBuilder result = new StringBuilder();

		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}

		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}

		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}

		try {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}

		String line = null;

		try {
			while((line = rd.readLine()) != null) {
				result.append(line);
			}

			String result2 = result.substring(13, result.length() -1);
			JSONParser jsonParser = new JSONParser();

			JSONArray array = (JSONArray) jsonParser.parse(result2);

			for(int i=0; i<array.size(); i++)
			{
				JSONObject jsonObj = (JSONObject) array.get(i);
				zipcodes.add(jsonObj.get("zip_code").toString());
			}

			rd.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//			e.printStackTrace();
		}
	}
}