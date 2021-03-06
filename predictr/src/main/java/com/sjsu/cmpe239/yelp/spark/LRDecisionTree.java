package com.sjsu.cmpe239.yelp.spark;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.DecisionTree;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.util.MLUtils;

import scala.Tuple2;

public class LRDecisionTree {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("Decision Tree Example");
		SparkContext sc = new SparkContext(conf);
		String path = "/data/239/yelp/predOut/features.txt";
		JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sc, path)
				.toJavaRDD();

		// Split initial RDD into two... [60% training data, 40% testing data].
		JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[] { 0.7,
				0.3 }, 11L);
		JavaRDD<LabeledPoint> training = splits[0].cache();
		JavaRDD<LabeledPoint> test = splits[1];

		// Set parameters.
		// Empty categoricalFeaturesInfo indicates all features are continuous.
		Map<Integer, Integer> categoricalFeaturesInfo = new HashMap<Integer, Integer>();
		int start[] = { 12, 29, 46, 63 };
		int end[] = { 21, 38, 55, 72 };
		for (int i = 0; i < 4; i++)
		{
			for (int j = start[i] - 1; j < end[i]; j++)
			{
				categoricalFeaturesInfo.put(j, 3);
			}
		}
		String impurity = "variance";
		Integer maxDepth = 5;
		Integer maxBins = 32;

		// Train a DecisionTree model.
		final DecisionTreeModel model = DecisionTree.trainRegressor(
				training, categoricalFeaturesInfo, impurity, maxDepth,
				maxBins);
		JavaRDD<Tuple2<Object, Object>> predictionAndLabels = test
				.map(new Function<LabeledPoint, Tuple2<Object, Object>>() {
					public Tuple2<Object, Object> call(LabeledPoint p) {
						Double prediction = model.predict(p.features());
						return new Tuple2<Object, Object>(prediction, p.label());
					}
				});
		// Get evaluation metrics.
	    BinaryClassificationMetrics metrics = new BinaryClassificationMetrics(predictionAndLabels.rdd());
	    JavaRDD<Tuple2<Object, Object>> prc = metrics.pr().toJavaRDD();
	    prc.saveAsTextFile("/tmp/t-dec/pr");
	    JavaRDD<Tuple2<Object, Object>> roc = metrics.roc().toJavaRDD();
	    roc.saveAsTextFile("/tmp/t-dec/roc");
//	    System.out.println("Precision-recall curve: " + prc.toArray());

		// model.save(sc,
		// "/data/239/yelp/predOut/models/logReg/LBFGS");
		// LogisticRegressionModel sameModel = LogisticRegressionModel.load(sc,
		// "myModelPath");
	}
}
