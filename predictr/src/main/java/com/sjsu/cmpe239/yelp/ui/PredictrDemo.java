package com.sjsu.cmpe239.yelp.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

public class PredictrDemo extends JFrame {

	private JButton buttonSelect = new JButton("Predict");
	private JButton buttonReset = new JButton("Reset");

	class Restaurant {
		String name;
		String categ;
		float rating;
		int revCnt;
		int priceRange;
		String state;
		String city;
		double score;
		String userReview = "0.0";

		public Restaurant(String r) {
			if (!r.startsWith("NA")) {
				try {
					String[] parts = r.split(":");
					name = parts[0] + (score > 0.6 ? "*" : "");
					categ = parts[1];
					rating = Float.parseFloat(parts[2]);
					revCnt = Integer.parseInt(parts[3]);
					priceRange = Integer.parseInt(parts[4]);
					city = parts[5];
					state = parts[6];
					score = Double.parseDouble(parts[7]);
					userReview = parts[8];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println(r);
					throw e;
				}
			} else {
				name = "NA";
				categ = "NA";
				rating = 0;
				revCnt = 0;
				priceRange = 0;
				city = "NA";
				state = "NA";
				score = 0.0;
				userReview = "0.0";
			}
		}

		public Restaurant(String r, double score) {
			this(r);
			this.score = score;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<b>Name: ");
			sb.append(name);
			sb.append("</b><br><b>Categories:</b> ");
			if (categ.length() > 30) {
				int half = categ.length() / 2;
				sb.append(categ.substring(0, half));
				sb.append("<br>");
				sb.append(categ.substring(half + 1));
			} else {
				sb.append(categ);
			}
			sb.append("<br><b>Stars:</b> ");
			sb.append(rating);
			sb.append("<br><b>Review Count:</b> ");
			sb.append(revCnt);
			sb.append("<br><b>Price Range:</b> ");
			sb.append(priceRange);
			sb.append("<br><b>City:</b> ");
			sb.append(city);
			sb.append("<br><b>State:</b> ");
			sb.append(state);
			if (!userReview.startsWith("0")) {
				sb.append("<br><b>User Stars:</b> ");
				sb.append(userReview);
			}
			if (score > 0) {
				sb.append("<br><b>Score: ");
				sb.append(score);
				sb.append("</b>");
			}
			return sb.toString();
		}
	}

	class User {
		String name;
		Restaurant top[];
		Restaurant prev[] = null;
		Map<String, Restaurant> targetRests;

		public User(String n, String t1, String t2) {
			name = n;
			top = new Restaurant[2];
			top[0] = new Restaurant(t1);
			top[1] = new Restaurant(t2);
			targetRests = new HashMap<String, Restaurant>();
		}

		public void addNewEntry(String line) {
			String[] parts = line.split("\t");
			targetRests.put(parts[1],
					new Restaurant(parts[3], Double.parseDouble(parts[8])));
			if (prev == null) {
				prev = new Restaurant[4];
				prev[0] = new Restaurant(parts[4], -1.0);
				prev[1] = new Restaurant(parts[5], -1.0);
				prev[2] = new Restaurant(parts[6], -1.0);
				prev[3] = new Restaurant(parts[7], -1.0);
			}
		}
	}

	class R implements Comparable<R> {
		String id;
		double score;

		public R(String i, double s) {
			id = i;
			score = s;
		}

		@Override
		public int compareTo(R o) {
			if (score > o.score) {
				return 1;
			} else if (score < o.score) {
				return -1;
			}
			return 0;
		}
	}

	public PredictrDemo() throws IOException {
		super("Yelp Predictor Demo");
		// setLayout(new GridLayout());
		setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				new FileInputStream("/data/239/yelp/predOut/topRests.txt")));

		final Map<String, User> userNames = new HashMap<String, User>();
		String line = null;
		List<R> ulst = new ArrayList<R>();
		while ((line = br1.readLine()) != null) {
			String[] temp = line.split("\t");
			User u = new User(temp[1], temp[2], temp[3]);
			userNames.put(temp[0], u);
			ulst.add(new R(temp[0], u.top[0].score));
		}
		Collections.sort(ulst, Collections.reverseOrder());
		br1.close();
		BufferedReader br2 = new BufferedReader(new InputStreamReader(
				new FileInputStream("/data/239/yelp/predOut/dbInput.txt")));
		int nullCnt = 0;
		while ((line = br2.readLine()) != null) {
			String[] temp = line.split("\t");
			User u = userNames.get(temp[0]);
			if (u == null) {
				nullCnt++;
			} else {
				u.addNewEntry(line);
			}
		}
		br2.close();

		final JComboBox<String> userList = new JComboBox<String>();
		for (R u : ulst) {
			userList.addItem(u.id);
		}
		// same font but bold
		Font boldFont = new Font("Arial", Font.BOLD, 14);
		final JLabel un = new JLabel("User ID");
		un.setFont(boldFont);
		final JLabel rn = new JLabel("Target Restaurant ID");
		rn.setFont(boldFont);
		final JLabel output = new JLabel("");
		final JComboBox<String> restList = new JComboBox<String>();

		// customize some appearance:
		userList.setForeground(Color.BLUE);
		userList.setFont(new Font("Arial", Font.BOLD, 14));
		userList.setMaximumRowCount(10);

		userList.setEditable(false);

		// add an event listener for the combo box
		userList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JComboBox<String> combo = (JComboBox<String>) event.getSource();
				String selectedUser = (String) combo.getSelectedItem();
				Map<String, Restaurant> targ = userNames.get(selectedUser).targetRests;

				java.util.List<R> rests = new ArrayList<R>();

				restList.removeAllItems();
				for (String rid : targ.keySet()) {
					rests.add(new R(rid, targ.get(rid).score));
				}
				Collections.sort(rests, Collections.<R> reverseOrder());
				for (R r : rests) {
					restList.addItem(r.id);
				}
				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo
						.getModel();

				int selectedIndex = model.getIndexOf(selectedUser);
				if (selectedIndex < 0) {
					// if the selected book does not exist before,
					// add it into this combo box
					model.addElement(selectedUser);
				}

			}
		});

		// customize some appearance:
		restList.setForeground(Color.BLUE);
		restList.setFont(new Font("Arial", Font.BOLD, 14));
		restList.setMaximumRowCount(10);

		// make the combo box editable so we can add new item when needed
		restList.setEditable(false);

		// add an event listener for the combo box
		restList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JComboBox<String> combo = (JComboBox<String>) event.getSource();
				String selectedRest = (String) combo.getSelectedItem();

				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo
						.getModel();

				int selectedIndex = model.getIndexOf(selectedRest);
				if (selectedIndex < 0) {
					// if the selected book does not exist before,
					// add it into this combo box
					model.addElement(selectedRest);
				}

			}
		});

		// add event listener for the button Select
		buttonSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String uid = (String) userList.getSelectedItem();
				String rid = (String) restList.getSelectedItem();
				User u = userNames.get(uid);
				if (u != null) {
					StringBuilder sb = new StringBuilder();
					sb.append("<html><table><tr><b>Name: </b>");
					sb.append(u.name);
					sb.append("</tr><tr></tr><tr><b>Previous Visits</b></tr><tr><td>");
					sb.append(u.prev[0].toString());
					sb.append("</td>");
					sb.append(u.prev[1].toString());
					sb.append("</td>");
					sb.append(u.prev[2].toString());
					sb.append("</td>");
					sb.append(u.prev[3].toString());
					sb.append("</td></tr><tr></tr><tr><td><b>Top Restaurants</b></td><td></td><td><b>Target Restaurant</b></td></tr><tr><td>");
					sb.append(u.top[0].toString());
					sb.append("</td>");
					sb.append(u.top[1].toString());
					sb.append("</td>");
					sb.append(u.targetRests.get(rid).toString());
					sb.append("</td></tr></table></html>");
					output.setText(sb.toString());
				}
			}
		});

		// add event listener for the button Select
		buttonReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				output.setText("");
			}
		});

		// add components to this frame
		add(un);
		add(userList);
		add(rn);
		add(restList);
		add(buttonSelect);
		add(buttonReset);
		add(output);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					JFrame j = new PredictrDemo();
					j.setExtendedState(JFrame.MAXIMIZED_BOTH);
					j.setVisible(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
