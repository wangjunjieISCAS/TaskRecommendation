This is the source code for paper 'Context-aware Personalized Crowdtesting Task Recommendation'.

Category Structure
------
* models: basic model for natural language processing
  
* src: source code
  * com/data: basic data structure, for example the data structure for crowdtesting report, testing task;
  * com/dataProcess: utilities for data preprocessing, for example the test project reader, and the method for obtaining the inverse document frequency
  * com/evaluation: from the raw recommendation results derive the evaluation metrics
  * com/recommendBasic: the testing context modeling by organizing the historical testing records of crowdworkers and current test status
  * com/recommendFeatureEngineering: retrieve corresponding features and prepare the learning data
  * com/taskRecommendation: learning-based task recommendation

* lib: the required library for running the approach 

* data: data for experiment

* how to run the approach
  * com/taskRecommendation/TrainAndPredict contains method for training the model (trainModel()) and conducting the prediction (conductPrediction())
  * we have already prepare the training data, i.e., data/output/train_predict/train-total, one can directly run the com/taskRecommendation/TrainAndPredict/conductPrediction() to conduct the task recommendation; the detailed recommendation results will be generated and stored in data/output/train_predict/result-total, and the approach performance will be generated and stored in data/output/train_predict/performance-total