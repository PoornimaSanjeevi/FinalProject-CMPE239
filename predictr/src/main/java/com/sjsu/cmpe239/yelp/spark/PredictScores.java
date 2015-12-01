package com.sjsu.cmpe239.yelp.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.RandomForestModel;
import org.apache.spark.mllib.util.MLUtils;

import scala.Tuple2;

public class PredictScores {
	public static void main(String[] args) {
		SparkConf conf = new SparkConf().setAppName("Random Forest Example");
		SparkContext sc = new SparkContext(conf);
		String path = "/data/239/yelp/predOut/allfeatures.txt";
		JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sc, path)
				.toJavaRDD();

	    final RandomForestModel model = RandomForestModel.load(sc, "/tmp/t-rfc/model");
		JavaRDD<Tuple2<Object, Object>> predictionAndLabels = data
				.map(new Function<LabeledPoint, Tuple2<Object, Object>>() {
					public Tuple2<Object, Object> call(LabeledPoint p) {
						Double prediction = model.predict(p.features());
						return new Tuple2<Object, Object>(prediction, p.features());
					}
				});
		
		predictionAndLabels.saveAsTextFile(path + ".out");
	}
}
