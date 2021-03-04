package com.recommendFeatureEngineering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import com.data.TestProject;
import com.data.TestReport;
import com.data.TestTask;
import com.recommendBasic.RecContextModeling;

//提供FeaturePreparationSemantic调用的基础功能
public class FeaturePreparationBasic {	
	/* 任务本身情况的指标
	 * 1.2.3.当前任务已经开始的小时数；和当前任务比较，比当前任务开始的早/晚的任务个数，
	 * 4.5.6.当前任务已经提交的报告个数；和当前任务相比，比当前任务提交报告数多/少的任务个数，
	 * 7. 共活跃的任务个数
	 * 对于这一组feature，对于不同的人是相同的
	 */
	public Integer[] obtainTaskRelatedAttributes ( TestProject curProject, int curRecPoint, ArrayList<TestProject> openProjectList, ArrayList<Integer> openProjectRecPointList ){
		Date curTime = curProject.getTestReportsInProj().get(curRecPoint).getSubmitTime();
		
		//当前任务已经开始的小时数；和当前任务比较，比当前任务开始的早/晚的任务个数，
		ArrayList<TestReport> curReportList = curProject.getTestReportsInProj();
		Date curBeginTime = curReportList.get(0).getSubmitTime();
		int beginHours =(int) ( ( curTime.getTime() - curBeginTime.getTime() ) /1000/60/60 );
		
		int beforeBeginNum = 0;
		for ( int i =0; i < openProjectList.size(); i++ ){
			TestProject project = openProjectList.get(i);
			ArrayList<TestReport> reportList = project.getTestReportsInProj();
			Date beginTime = reportList.get(0).getSubmitTime();
			if ( beginTime.before( curBeginTime )){
				beforeBeginNum++;
			}
		}
		int endBeginNum = openProjectList.size() - beforeBeginNum;
		
		//当前任务已经提交的报告个数；和当前任务相比，比当前任务提交报告数多/少的任务个数，
		int submitReports = curRecPoint;
		int moreReportsNum = 0;
		for ( int i =0; i < openProjectList.size(); i++ ){
			int recPoint = openProjectRecPointList.get( i );
			if ( curRecPoint > recPoint ){
				moreReportsNum++;
			}
		}
		int lessReportsNum = openProjectList.size() - moreReportsNum;
		
		int activeProject = openProjectList.size();
		
		Integer[] features = { beginHours, beforeBeginNum, endBeginNum, submitReports, moreReportsNum, lessReportsNum, activeProject };
		return features;
	}
	
	/*
	 * 将各种不同的feature 用workerID对齐
	 * expertiseFeature (8), preferenceFeature (8), largeExpertise (8), largePreference (8), taskFeatures (7)
	 */
	public HashMap<String, ArrayList<Double>> featureCombination ( HashMap<String, ArrayList<Double>> activeFeatureList, HashMap<String, ArrayList<Double>> expertiseFeatureList, HashMap<String, ArrayList<Double>> preferenceFeatureList, 
			HashMap<String, Integer[]> largeExpertiseList, HashMap<String, Integer[]> largePreferenceList, Integer[] taskFeatures ){
		ArrayList<Double> defaultExpertise = new ArrayList<Double>();
		for ( int i =0; i < 7; i++ ){
			defaultExpertise.add( 0.0);
		}
		ArrayList<Double> taskFeatureArray = new ArrayList<Double>();
		for ( int i =0; i < taskFeatures.length; i++ ){
			taskFeatureArray.add( taskFeatures[i] * 1.0);
		}
		
		HashMap<String, ArrayList<Double>> totalFeatureList = new HashMap<String, ArrayList<Double>>();
		for ( String workerId : activeFeatureList.keySet() ){
			ArrayList<Double> totalFeatures = new ArrayList<Double>();
			
			ArrayList<Double> activeFeatures = activeFeatureList.get( workerId );
			
			ArrayList<Double> expertiseFeatures = new ArrayList<Double>();
			if ( expertiseFeatureList.containsKey( workerId )){
				expertiseFeatures = expertiseFeatureList.get( workerId );
			}else{
				expertiseFeatures = defaultExpertise;
			}
			
			ArrayList<Double> preferenceFeatures = new ArrayList<Double>();
			if ( preferenceFeatureList.containsKey( workerId )){
				preferenceFeatures = preferenceFeatureList.get( workerId );
			}else{
				preferenceFeatures = defaultExpertise;
			}
			
			totalFeatures.addAll( activeFeatures );
			totalFeatures.addAll( expertiseFeatures );
			totalFeatures.addAll( preferenceFeatures );
			
			ArrayList<Double> largeExpertise = new ArrayList<Double>();
			if ( largeExpertiseList.containsKey( workerId )){
				Integer[] expertise = largeExpertiseList.get( workerId );
				for ( int i =0; i < expertise.length; i++ ){
					largeExpertise.add( expertise[i]*1.0);
				}
			}else{
				largeExpertise = defaultExpertise;
			}
			
			ArrayList<Double> largePreference = new ArrayList<Double>();
			if ( largePreferenceList.containsKey( workerId )){
				Integer[] preference = largePreferenceList.get( workerId );
				for ( int i =0; i < preference.length; i++ ){
					largePreference.add( preference[i] * 1.0 );
				}
			}else{
				largePreference = defaultExpertise;
			}
			
			totalFeatures.addAll( largeExpertise );
			totalFeatures.addAll( largePreference );
			
			totalFeatures.addAll( taskFeatureArray );
			
			totalFeatureList.put( workerId, totalFeatures );
		}
		
		return totalFeatureList;
	}
	
	//找到curTime对应的recTimePoint
	public Integer findRecPointByTime ( TestProject project, Date curTime ){
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		
		int recPoint = 0;
		for ( int j =0; j < reportList.size()-1; j++ ){
			Date reportTime = reportList.get(j).getSubmitTime();
			Date nextTime = reportList.get(j+1).getSubmitTime();
			if ( ( curTime.after( reportTime)|| curTime.equals( reportTime)) && (curTime.before(nextTime) || curTime.equals( nextTime)) ){
				recPoint = j;
				break;
			}
		}
		return recPoint;
	}
	
	public void outputFeaturesWeka ( String outFile, HashMap<String, ArrayList<Double>> totalFeatureList, ArrayList<String> positiveWorkerList, 
			ArrayList<String> negativeWorkerList  ){
		File file = new File ( outFile );
		if ( !file.exists() ){
			this.generateWekaHeader( outFile );        
		}
		
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( outFile ), true ));
			for ( int k =0; k < positiveWorkerList.size(); k++ ){
				String workerId = positiveWorkerList.get(k);
				ArrayList<Double> samples = totalFeatureList.get( workerId );
				for ( int i =0; i < samples.size(); i++ ){
					writer.write( samples.get( i ) + ",");
				}
				writer.write( "yes");
				writer.newLine();
			}
			for ( int k =0; k < negativeWorkerList.size(); k++ ){
				String workerId = negativeWorkerList.get(k);
				ArrayList<Double> samples = totalFeatureList.get( workerId );
				for ( int i =0; i < samples.size(); i++ ){
					writer.write( samples.get( i ) + ",");
				}
				writer.write( "no");
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	/*
	 * active (12), expertiseFeature (8), preferenceFeature (8), largeExpertise (8), largePreference (8), taskFeatures (7)
	 */
	public void generateWekaHeader ( String fileName ){
		String[] active = { "a-durationLastBug", "a-durationLastReport", "a-bugsLast8hours", "a-bugsLast24hours", "a-bugsLast1week", 
			"a-bugsLast2week", "a-bugsLastPast", "a-reportsLast8hours", "a-reportsLast24hours", "a-reportsLast1week", "a-reportsLast2week", 
			"a-reportsLastPast"};
		String[] expertise = { "e-probSim", "e-cosSim", "e-eucSim", "e-jacSim0.0" , "e-jacSim0.1",  "e-jacSim0.3", "e-jacSim0.5" };
		String[] preference = {	"p-probSim", "p-cosSim", "p-eucSim", "p-jacSim0.0" , "p-jacSim0.1", "p-jacSim0.3", "p-jacSim0.5"	};
		String[] largeExpertise = { "le-probSim", "le-cosSim", "le-eucSim", "le-jacSim0.0" , "le-jacSim0.1", "le-jacSim0.3", "le-jacSim0.5" };
		String[] largePreference = { "lp-probSim", "lp-cosSim", "lp-eucSim", "lp-jacSim0.0" , "lp-jacSim0.1", "lp-jacSim0.3", "lp-jacSim0.5" };
		String[] task = {"t-beginHours", "t-beforeTasks", "t-afterTasks", "t-reportNum", "t-moreReports", "t-lessReports", "activeTasks"};
		
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File (  fileName ) ));
			for ( int i =0; i <active.length; i++){
				writer.write( active[i] +",");
			}
			for ( int i =0; i < expertise.length; i++){
				writer.write( expertise[i] +",");
			}
			for ( int i=0; i < preference.length; i++ ){
				writer.write( preference[i] + ",");
			}
			for ( int i =0; i < largeExpertise.length; i++){
				writer.write( largeExpertise[i] + ",");
			}
			for ( int i =0; i < largePreference.length; i++){
				writer.write( largePreference[i] + ",");
			}
			for ( int i =0; i < task.length; i++){
				writer.write( task[i] + ",");
			}
			writer.write("category");
			writer.newLine();
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateWekaHeaderDCWDS ( String fileName ){
		String[] features = {"active-2", "simQuality-1", "simQuality-2", "taskDuration", "openTasks"}; // "active-1", 
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File (  fileName ) ));
			for ( int i =0; i <features.length; i++){
				writer.write( features[i] +",");
			}
			
			writer.write("category");
			writer.newLine();
			writer.flush();
			writer.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void generateWekaHeaderTaskRec ( String fileName ){
		String[] features = {"profile1", "profile2", "preference"}; // "active-1", 
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File (  fileName ) ));
			for ( int i =0; i <features.length; i++){
				writer.write( features[i] +",");
			}
			
			writer.write("category");
			writer.newLine();
			writer.flush();
			writer.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public ArrayList<String> prepareLearningFeatures ( TestProject curProject, int curRecPoint, String outFile, Boolean isTrain, 
			ArrayList<TestProject> openProjectList, 
			HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory, 
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory ){
		//for workerExpertiseHistory, workerPreferenceHistory, <worker, <Date, word embedding for all the reports submitted in the time>>, 
		Date curTime = curProject.getTestReportsInProj().get( curRecPoint ).getSubmitTime();   //推荐点是recTimePoint这个report提交结束的点
		TestTask curTask = curProject.getTestTask();
		
		RecContextModeling contextTool = new RecContextModeling ();
		HashMap<String, Double> testContext = contextTool.modelTestContextSimple( curTask );   //**** contextTool.modelTestContext( curProject, curRecPoint, curTask); 
		HashMap<String, HashMap<Date, ArrayList<String>>> curActiveList = contextTool.modelActivenessContext( curProject, curRecPoint, workerActiveHistory);
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList = contextTool.modelExpertiseRawContext(  curProject, curRecPoint, workerExpertiseHistory);
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> curPreferenceList = contextTool.modelPreferenceRawContext( curProject, curRecPoint, workerPreferenceHistory);
				
		FeatureRetrievalActive activeFeatureTool = new FeatureRetrievalActive ();
		FeatureRetrievalExpertise expertiseFeatureTool = new FeatureRetrievalExpertise ();
		HashMap<String, ArrayList<Double>> activeFeatureList = activeFeatureTool.retrieveActiveFeatures(curActiveList, curTime);
		HashMap<String, ArrayList<Double>> expertiseFeatureList = expertiseFeatureTool.retrieveExpertiseFeatures( curProject, curTask, curRecPoint, curExpertiseList, testContext );
		HashMap<String, ArrayList<Double>> preferenceFeatureList = expertiseFeatureTool.retrieveExpertiseFeatures( curProject, curTask, curRecPoint, curPreferenceList, testContext );
		
		//获取正在open的其他项目的情况，人员和这些项目的mapping情况
		ArrayList<HashMap<String, ArrayList<Double>>> allExpertiseFeatureList = new ArrayList<HashMap<String, ArrayList<Double>>>();
		ArrayList<HashMap<String, ArrayList<Double>>> allPreferenceFeatureList = new ArrayList<HashMap<String, ArrayList<Double>>>();
		ArrayList<Integer> openProjectRecPointList = new ArrayList<Integer>(); 
		for ( int i =0; i < openProjectList.size(); i++){
			TestProject project = openProjectList.get(i);
			TestTask task= project.getTestTask();
			
			int recPoint = this.findRecPointByTime(project, curTime);			
			openProjectRecPointList.add( recPoint );
			HashMap<String, Double> thisTestContext = contextTool.modelTestContextSimple ( task );
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> thisExpertiseList = contextTool.modelExpertiseRawContext( project, recPoint, workerExpertiseHistory);
			HashMap<String, HashMap<Date, ArrayList<List<String>>>> thisPreferenceList = contextTool.modelPreferenceRawContext( project, recPoint, workerPreferenceHistory);
			
			HashMap<String, ArrayList<Double>> thisExpertiseFeatureList = expertiseFeatureTool.retrieveExpertiseFeatures( project, task, recPoint, thisExpertiseList, thisTestContext );
			HashMap<String, ArrayList<Double>> thisPreferenceFeatureList = expertiseFeatureTool.retrieveExpertiseFeatures( project, task, recPoint, thisPreferenceList, thisTestContext );
			
			allExpertiseFeatureList.add( thisExpertiseFeatureList );
			allPreferenceFeatureList.add( thisPreferenceFeatureList );
		}
		
		//和当前任务相比，相关性比当前任务多/少的任务个数
		HashMap<String, Integer[]> largeExpertiseList = new HashMap<String, Integer[]>();
		HashMap<String, Integer[]> largePreferenceList = new HashMap<String, Integer[]>();
		for ( String workerId : expertiseFeatureList.keySet() ){
			ArrayList<Double> expertiseValues = expertiseFeatureList.get( workerId );
			
			Integer[] largeExpertiseArray = new Integer[expertiseValues.size()];                     //new Integer[8];
			for ( int i=0; i < largeExpertiseArray.length; i++){
				largeExpertiseArray[i] = 0;
			}
			for ( int j =0; j < allExpertiseFeatureList.size(); j++ ){
				HashMap<String, ArrayList<Double>> thisExpertiseFeatureList = allExpertiseFeatureList.get( j );
				if ( !thisExpertiseFeatureList.containsKey( workerId )){
					continue;
				}
				ArrayList<Double> thisExpertiseValues = thisExpertiseFeatureList.get( workerId );
				for ( int k =0; k < expertiseValues.size(); k++ ){
					if ( expertiseValues.get(k) < thisExpertiseValues.get(k)){
						largeExpertiseArray[k]++;
					}
				}
			}
			largeExpertiseList.put( workerId, largeExpertiseArray );
		}
		for ( String workerId : preferenceFeatureList.keySet() ){
			ArrayList<Double> preferenceValues = preferenceFeatureList.get( workerId );
			
			Integer[] largePreferenceArray = new Integer[preferenceValues.size()];
			for ( int i =0; i < largePreferenceArray.length; i++ ){
				largePreferenceArray[i] = 0;
			}
			for ( int j =0; j < allPreferenceFeatureList.size(); j++ ){
				HashMap<String, ArrayList<Double>> thisPreferenceFeatureList = allPreferenceFeatureList.get( j );
				if ( !thisPreferenceFeatureList.containsKey( workerId )){
					continue;
				}
				ArrayList<Double> thisPreferenceValues = thisPreferenceFeatureList.get( workerId );
				for ( int k =0; k < preferenceValues.size(); k++ ){
					if ( preferenceValues.get(k) < thisPreferenceValues.get(k)){
						largePreferenceArray[k]++;
					}
				}
			}
			largePreferenceList.put( workerId, largePreferenceArray );
		}
		
		Integer[] taskFeatures = this.obtainTaskRelatedAttributes(curProject, curRecPoint, openProjectList, openProjectRecPointList);
		
		HashMap<String, ArrayList<Double>> totalFeatureList = this.featureCombination(activeFeatureList, expertiseFeatureList, preferenceFeatureList, 
				largeExpertiseList, largePreferenceList, taskFeatures);
		
		PositiveNegativeForFeaturePreparation posNegFeaturePrepare = new PositiveNegativeForFeaturePreparation();
		ArrayList<String> positiveWorkerList = posNegFeaturePrepare.retrievePredictionLabel( curProject, curRecPoint );
				//posNegFeaturePrepare.retrievePredictionLabeFirstBug( curProject, curRecPoint );
		ArrayList<String> negativeWorkerList = new ArrayList<String>();
		if ( isTrain ){
			negativeWorkerList = posNegFeaturePrepare.retrieveNegativeSampleTrainset(totalFeatureList, positiveWorkerList);
		}else{
			negativeWorkerList = posNegFeaturePrepare.retrieveNegativeSampleTestset(totalFeatureList, positiveWorkerList);
		}		
		
		ArrayList<String> featureWorkers = new ArrayList<String>();
		featureWorkers.addAll( positiveWorkerList );
		featureWorkers.addAll( negativeWorkerList );

		this.outputFeaturesWeka ( outFile, totalFeatureList, positiveWorkerList, negativeWorkerList);
		return featureWorkers;
	}
	
	public static void main ( String[] args ){
		FeaturePreparationBasic featurePrepareTool = new FeaturePreparationBasic();
		featurePrepareTool.generateWekaHeader( "");
	}
}
