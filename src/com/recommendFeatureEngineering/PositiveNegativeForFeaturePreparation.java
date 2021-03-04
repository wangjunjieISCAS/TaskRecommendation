package com.recommendFeatureEngineering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.data.TestProject;
import com.data.TestReport;

public class PositiveNegativeForFeaturePreparation {
	//Ŀǰ��û�п����ظ�ȱ�ݵ���������Ƿ�����ȱ�ݣ�����Ϊ��positive
	public ArrayList<String> retrievePredictionLabel ( TestProject project, int recTimePoint ){
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		
		ArrayList<String> bugWorkerList = new ArrayList<String>();
		for ( int i = recTimePoint+1; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			if ( bugTag.equals( "���ͨ��") && !bugWorkerList.contains( report.getUserId()) ){
				bugWorkerList.add( report.getUserId() );
			}
		}
		
		return bugWorkerList;
	}
	
	//retrievePredictionLabel û�п����ظ������ֻҪ��bug����Ϊtrue
	//retrievePredictionLabeFirstBug �����ظ������ֻ��first bug����Ϊtrue
	public ArrayList<String> retrievePredictionLabeFirstBug ( TestProject project, int recTimePoint ){
		ArrayList<TestReport> reportList = project.getTestReportsInProj();
		
		HashSet<String> bugDupTag = new HashSet<String>();
		for ( int i =0; i < recTimePoint; i++ ){
			String bug = reportList.get( i ).getTag();
			if ( bug.equals( "���ͨ��")){
				bugDupTag.add( reportList.get(i).getDuplicate() );
			}
		}
		
		ArrayList<String> bugWorkerList = new ArrayList<String>();
		for ( int i = recTimePoint+1; i < reportList.size(); i++ ){
			TestReport report = reportList.get( i );
			String bugTag = report.getTag();
			String dupTag = report.getDuplicate();
			if ( bugTag.equals( "���ͨ��") && !bugWorkerList.contains( report.getUserId()) && !bugDupTag.contains( dupTag ) ){
				bugWorkerList.add( report.getUserId() );
			}
		}
		
		return bugWorkerList;
	}
	
	//���ѡ��������positiveWorkerList��workers
	public ArrayList<String> retrieveNegativeSampleTrainset ( HashMap<String, ArrayList<Double>> totalFeatureList, ArrayList<String> positiveWorkerList ){
		int negSize = positiveWorkerList.size() * 2;
		ArrayList<String> negativeWorkerList = new ArrayList<String>();
		
		ArrayList<String> allCandWorkers = new ArrayList<String>();
		for ( String worker : totalFeatureList.keySet() ){
			allCandWorkers.add( worker );
		}
		
		Random rand = new Random();
		for ( int i =0; i < negSize; ){
			int index = rand.nextInt( allCandWorkers.size() );
			String worker = allCandWorkers.get( index );
			if ( positiveWorkerList.contains( worker ) || negativeWorkerList.contains( worker )){
				continue;
			}
			
			negativeWorkerList.add( worker );
			i++;
		}
		return negativeWorkerList;
	}
	
	//Ŀǰֻ����com.exploration�У����ѡ��������
	public ArrayList<String> retrieveNegativeSampleTrainset ( ArrayList<String> candWorkerList, ArrayList<String> positiveWorkerList  ){
		int negSize = positiveWorkerList.size();
		ArrayList<String> negativeWorkerList = new ArrayList<String>();
		
		Random rand = new Random();
		for ( int i =0; i < negSize; ){
			int index = rand.nextInt( candWorkerList.size() );
			String worker = candWorkerList.get( index );
			if ( positiveWorkerList.contains( worker ) || negativeWorkerList.contains( worker )){
				continue;
			}
			
			negativeWorkerList.add( worker );
			i++;
		}
		return negativeWorkerList;
	}
	
	//trainset��testset�������ڣ�testset�����е�negative sample
	public ArrayList<String> retrieveNegativeSampleTestset ( HashMap<String, ArrayList<Double>> totalFeatureList, ArrayList<String> positiveWorkerList ){
		ArrayList<String> negativeWorkerList = new ArrayList<String>();
		
		for ( String worker : totalFeatureList.keySet() ){
			if ( !positiveWorkerList.contains( worker ) && !negativeWorkerList.contains( worker )){
				negativeWorkerList.add( worker );
			}			
		}
		return negativeWorkerList;
	}
}
