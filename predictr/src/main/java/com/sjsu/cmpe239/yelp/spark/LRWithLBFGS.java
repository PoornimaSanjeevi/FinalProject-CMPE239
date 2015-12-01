package com.sjsu.cmpe239.yelp.spark;

import scala.Tuple2;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;

public class LRWithLBFGS {
  public static void main(String[] args) {
    SparkConf conf = new SparkConf().setAppName("SVM Classifier Example");
    SparkContext sc = new SparkContext(conf);
    String path = "/data/239/yelp/predOut/features.txt";
    JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sc, path).toJavaRDD();

    // Split initial RDD into two... [60% training data, 40% testing data].
    JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[] {0.7, 0.3}, 11L);
    JavaRDD<LabeledPoint> training = splits[0].cache();
    JavaRDD<LabeledPoint> test = splits[1];

    // Run training algorithm to build the model.
    final LogisticRegressionModel model = new LogisticRegressionWithLBFGS()
      .setNumClasses(2)
      .run(training.rdd());
    model.clearThreshold();

    // Compute raw scores on the test set.
    JavaRDD<Tuple2<Object, Object>> predictionAndLabels = test.map(
      new Function<LabeledPoint, Tuple2<Object, Object>>() {
        public Tuple2<Object, Object> call(LabeledPoint p) {
          Double prediction = model.predict(p.features());
          return new Tuple2<Object, Object>(prediction, p.label());
        }
      }
    );

    BinaryClassificationMetrics metrics = new BinaryClassificationMetrics(predictionAndLabels.rdd());
    // Precision by threshold
//    JavaRDD<Tuple2<Object, Object>> precision = metrics.precisionByThreshold().toJavaRDD();
//    System.out.println("Precision by threshold: " + precision.toArray());
//
//    // Recall by threshold
//    JavaRDD<Tuple2<Object, Object>> recall = metrics.recallByThreshold().toJavaRDD();
//    System.out.println("Recall by threshold: " + recall.toArray());
    // Precision-recall curve
    JavaRDD<Tuple2<Object, Object>> prc = metrics.pr().toJavaRDD();
    prc.saveAsTextFile("/tmp/t-lbfgs/pr");
    JavaRDD<Tuple2<Object, Object>> roc = metrics.roc().toJavaRDD();
    roc.saveAsTextFile("/tmp/t-lbfgs/roc");
//    System.out.println("Precision-recall curve: " + prc.toArray());
    // Get evaluation metrics.
//    MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels.rdd());
//    double precision = metrics.precision();
//    double precisionL1 = metrics.precision(1);
//    System.out.println("Precision = " + precision);
//    System.out.println("PrecisionL1 = " + precisionL1);

    // Save and load model
//    model.save(sc, "/data/239/yelp/predOut/models/logReg/LBFGS");
//    LogisticRegressionModel sameModel = LogisticRegressionModel.load(sc, "myModelPath");
  }
}
