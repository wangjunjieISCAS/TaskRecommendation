package com.dataProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;


public class MultiTaskStatus {	
	SimpleDateFormat formatLine = new SimpleDateFormat ("yyyy/MM/dd HH:mm");
	 
	//RecommendationTime也有对于multiTaskStatus情况的统计，
	public void obtainMultiTaskStatus (ArrayList<TestProject> projectList ){
		//2015/1/5 16:56 --- 2016/8/7 19:48
		Date beginTime = null, endTime = null;
		try {
			beginTime = formatLine.parse( "2015/01/05 00:00");
			endTime = formatLine.parse( "2016/08/08 00:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LinkedHashMap<Date, ArrayList<TestProject>> projectListByDate = new LinkedHashMap<Date, ArrayList<TestProject>>();
		Date curTime = beginTime;
		while ( curTime.before( endTime) ){
			ArrayList<TestProject> projectsByDate = new ArrayList<TestProject>();
			for ( int i =0; i < projectList.size(); i++ ){
				TestProject project = projectList.get( i );
				ArrayList<TestReport> reportList = project.getTestReportsInProj();
				Date thisBegin = reportList.get(0).getSubmitTime();
				Date thisEnd = reportList.get( reportList.size()-1).getSubmitTime();
				
				if ( curTime.after( thisBegin) && curTime.before( thisEnd )){
					projectsByDate.add( project );
				}
			}
			
			projectListByDate.put( curTime, projectsByDate );
			
			Calendar cad = Calendar.getInstance();
			cad.setTime( curTime );
			cad.add( Calendar.DAY_OF_YEAR, 1);
			curTime = cad.getTime();
		}
        
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( "data/output/findings/multiTasks-final.csv" ) ));
			int index = 1;
			for ( Date curDate : projectListByDate.keySet() ) {
				if ( projectListByDate.get(curDate).size() < 2 )
					continue;
				writer.write( index++ + "," + formatLine.format(curDate) + "," + projectListByDate.get(curDate).size());
				writer.newLine();
			}
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//从文件中读取multiTask相关的信息，因为后来进行了过滤，只保留了=2的部分，以及>2的，所以需要根据从文件中读取的进行计算
	public void obtainMultiTaskWorkerStatus (ArrayList<TestProject> projectList ){
		LinkedHashMap<Date, Integer> multiTaskList = this.readMultiTasks();
		
		Date beginTime = null, endTime = null;
		try {
			beginTime = formatLine.parse( "2015/01/05 00:00");
			endTime = formatLine.parse( "2016/08/08 00:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LinkedHashMap<Date, ArrayList<TestProject>> projectListByDate = new LinkedHashMap<Date, ArrayList<TestProject>>();
		Date curTime = beginTime;
		while ( curTime.before( endTime) ){
			if ( multiTaskList.containsKey( curTime )) {   //为了基于文件保存的信息进行过滤
				ArrayList<TestProject> projectsByDate = new ArrayList<TestProject>();
				for ( int i =0; i < projectList.size(); i++ ){
					TestProject project = projectList.get( i );
					ArrayList<TestReport> reportList = project.getTestReportsInProj();
					Date thisBegin = reportList.get(0).getSubmitTime();
					Date thisEnd = reportList.get( reportList.size()-1).getSubmitTime();
					
					if ( curTime.after( thisBegin) && curTime.before( thisEnd )){
						projectsByDate.add( project );
					}
				}
				
				projectListByDate.put( curTime, projectsByDate );
			}			
			
			Calendar cad = Calendar.getInstance();
			cad.setTime( curTime );
			cad.add( Calendar.DAY_OF_YEAR, 1);
			curTime = cad.getTime();
		}
		
		//首先获取某一天，开放的项目有哪些；然后统计每一个人，共参与了多少个这些开放的项目（这里就不管是不是同一天了）
		LinkedHashMap<Date, HashMap<String, Integer>> workerMultiTask = new LinkedHashMap<Date, HashMap<String, Integer>>();
		LinkedHashMap<Date, HashMap<String, Double>> workerMultiTaskRatio = new LinkedHashMap<Date, HashMap<String, Double>>();
		LinkedHashMap<Date, HashMap<String, Double>> workerSucMultiTaskRatio = new LinkedHashMap<Date, HashMap<String, Double>>();
		//String[] selectTime = {"2015/9/6 0:00" , "2015/11/12 0:00", "2015/12/1 0:00", "2015/12/30 0:00", "2016/1/29 0:00",
			//	"2016/3/7 0:00", "2016/6/9 0:00", "2016/6/24 0:00", "2016/7/14 0:00", "2016/7/29 0:00" };
		String[] selectTime = {"2015/9/6 0:00" , "2015/11/12 0:00", "2015/12/1 0:00", "2015/11/26 0:00", "2016/1/29 0:00",
				"2015/12/3 0:00", "2016/6/9 0:00", "2015/12/23 0:00", "2016/7/14 0:00", "2015/12/16 0:00" };   //非典型的情况，

		Integer[] timeProjectNum = {3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
		
		ArrayList<Date> selectTimeDate = new ArrayList<Date>();
		for ( int i =0; i < selectTime.length; i++ ) {
			try {
				selectTimeDate.add( formatLine.parse( selectTime[i]));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for ( Date thisTime : projectListByDate.keySet() ){
			if ( !selectTimeDate.contains( thisTime) )
				continue;
			System.out.println ( projectListByDate.get(thisTime).size() );
			HashMap<String, Integer> workerTasks = new HashMap<String, Integer>();
			HashMap<String, Integer> workerSucTasks = new HashMap<String, Integer>();
			
			ArrayList<TestProject> curProjectList = projectListByDate.get( thisTime );
			for ( int i =0; i < curProjectList.size(); i++ ){
				ArrayList<TestReport> reportList = curProjectList.get( i ).getTestReportsInProj();
				
				HashSet<String> allWorkers = new HashSet<String>();   
				for ( int j=0; j < reportList.size() ; j++ ){
					String worker = reportList.get(j).getUserId();
					allWorkers.add( worker );
				}
				
				HashSet<String> workers = new HashSet<String>(); //一个人在一个项目里面提交多个报告，也算一次
				for ( int j=0; j < reportList.size() ; j++ ){
					/*
					if ( !reportList.get(j).getTag().equals("审核通过")) {
						continue;
					}
					*/
					String worker = reportList.get(j).getUserId();
					workers.add( worker );
				}
				
				HashSet<String> sucWorkers = new HashSet<String>(); //第一个unique bug
				HashSet<String> bugDupTag = new HashSet<String>();
				for ( int j=0; j < reportList.size() ; j++ ){
					if ( !reportList.get(j).getTag().equals("审核通过")) {
						continue;
					}
					
					if ( !bugDupTag.contains( reportList.get(j).getDuplicate() )){
						String worker = reportList.get(j).getUserId();
						sucWorkers.add( worker );
					}					
					bugDupTag.add( reportList.get(j).getDuplicate() );					
				}
				
				//allWorkers 参与任务的， worker提交bug的
				for ( String worker  : allWorkers ){
					int count = 0;
					if ( workers.contains( worker ))
						count = 1;
					if ( workerTasks.containsKey( worker )){
						count += workerTasks.get( worker) ;
					}
					workerTasks.put( worker , count );
				}
				
				for ( String worker : allWorkers ){
					int count = 0;
					if ( sucWorkers.contains( worker ))
						count = 1;
					if ( workerSucTasks.containsKey( worker )){
						count += workerSucTasks.get( worker );
					}
					workerSucTasks.put( worker, count );
				}
			}			
			
			HashMap<String, Double> workerRatioTasks = new HashMap<String, Double>();
			HashMap<String, Double> workerSucRatioTasks = new HashMap<String, Double>();
			for ( String worker : workerTasks.keySet() ) {
				int totalCount = projectListByDate.get( thisTime ).size();
				Double ratio = 1.0* workerTasks.get(worker) / totalCount;
				Double ratio2 = 1.0* workerSucTasks.get( worker) / totalCount;
				workerRatioTasks.put( worker, ratio );
				//workerSucRatioTasks.put( worker, ratio2 );
				workerSucRatioTasks.put( worker, workerSucTasks.get( worker) * 1.0 );
			}
			workerMultiTask.put( thisTime, workerTasks );
			workerMultiTaskRatio.put(thisTime, workerRatioTasks );
			workerSucMultiTaskRatio.put( thisTime, workerSucRatioTasks );
		}
		
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( "data/output/findings/multiTaskWorkers-1.csv" ) ));
			writer.write( " " + "," + "  ");
			writer.newLine();
			for ( Date thisTime : workerMultiTask.keySet() ){
				int index = selectTimeDate.indexOf(thisTime );
				for ( String worker : workerMultiTask.get( thisTime).keySet()) {
					writer.write( timeProjectNum[index] + "," + workerMultiTask.get( thisTime).get(worker ));
					writer.newLine();
				}
			}
			writer.flush();
			writer.close();
			
			writer = new BufferedWriter ( new FileWriter ( new File ( "data/output/findings/multiTaskWorkersRatio-1.csv" ) ));
			writer.write( " " + "," + "  ");
			writer.newLine();
			for ( Date thisTime : workerMultiTaskRatio.keySet() ){
				int index = selectTimeDate.indexOf(thisTime );
				for ( String worker : workerMultiTaskRatio.get( thisTime).keySet()) {
					writer.write( timeProjectNum[index] + "," + workerMultiTask.get( thisTime).get(worker ) + "," + 
							workerMultiTaskRatio.get( thisTime).get(worker ) + "," + workerSucMultiTaskRatio.get( thisTime).get(worker ));
					writer.newLine();
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LinkedHashMap<Date, Integer> readMultiTasks ( ){
		LinkedHashMap<Date, Integer> multiTaskList = new LinkedHashMap<Date, Integer>();
		try {
			BufferedReader reader = new BufferedReader ( new FileReader ( new File (  "data/output/findings/multiTasks-final.csv" )));
			String line = null;
			line = reader.readLine();
			while (  (line = reader.readLine()) != null ){				
				String[] temp = line.split(",");
				
				Date curDate = formatLine.parse( temp[1]);
				Integer value = Integer.parseInt( temp[2] ); 
				
				multiTaskList.put( curDate, value );
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return multiTaskList;
	}
	
	public static void main ( String[] args ){
		MultiTaskStatus multiTask = new MultiTaskStatus();
		
		TestProjectReader projectReader = new TestProjectReader();
		ArrayList<TestProject> projectList = projectReader.loadTestProjectAndTaskList( Constants.PROJECT_FOLDER, Constants.TASK_DES_FOLDER );
		
		//multiTask.obtainMultiTaskStatus(projectList);
		multiTask.obtainMultiTaskWorkerStatus(projectList);
	}
}
