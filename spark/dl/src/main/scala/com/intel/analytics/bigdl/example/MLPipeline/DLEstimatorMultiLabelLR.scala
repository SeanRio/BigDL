/*
 * Copyright 2016 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.analytics.bigdl.example.MLPipeline

import com.intel.analytics.bigdl.nn.{Linear, LogSoftMax, MultiLabelSoftMarginCriterion, Sequential}
import com.intel.analytics.bigdl.utils.Engine
import org.apache.spark.SparkContext
import org.apache.spark.ml.DLEstimator
import org.apache.spark.sql.SQLContext

/**
 *  Multi-label Logistic Regression with BigDL layers and DLEstimator
 */
object DLEstimatorMultiLabelLR {

  def main(args: Array[String]): Unit = {
    val conf = Engine.createSparkConf()
      .setAppName("DLEstimatorMultiLabelLR")
      .set("spark.task.maxFailures", "1")
    val sc = new SparkContext(conf)
    val sqlContext = SQLContext.getOrCreate(sc)
    Engine.init

    val model = new Sequential[Float]().add(Linear[Float](2, 2)).add(LogSoftMax[Float])
    val criterion = MultiLabelSoftMarginCriterion[Float]()
    val estimator = new DLEstimator[Float](model, criterion, Array(2), Array(2))
      .setBatchSize(4)
      .setMaxEpoch(10)
    val data = sc.parallelize(Seq(
      (Array(0.0, 1.0), Array(1.0, 0.0)),
      (Array(1.0, 0.0), Array(0.0, 1.0)),
      (Array(0.0, 1.0), Array(1.0, 0.0)),
      (Array(1.0, 0.0), Array(0.0, 1.0))))
    val df = sqlContext.createDataFrame(data).toDF("features", "label")
    val dlModel = estimator.fit(df)
    dlModel.transform(df).show(false)
  }

}
