package com.dataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.data.TestProject;

public class ProjectRankTimeSeries {

	public ProjectRankTimeSeries() {
		// TODO Auto-generated constructor stub
	}
	
	public static ArrayList<String> obtainProjectRankTimeSeries ( ) {
		ArrayList<String> projectRank = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader ( new FileReader ( new File ( "data/input/projectRankTimeSeries.csv")));
			String line = "";
			while ( (line = reader.readLine() ) != null ) {
				projectRank.add( line.trim() );
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return projectRank;
	}
	
	
	/*
	 * ֮����Ҫ���������������Ϊ���ܲ�ͬ�����ж�ȡ����projectList˳���Ǳ仯�ģ�Ϊ��ȷ��˳����һ�µģ����Һ��ڻ��������µ���projectRank��˳��
	 */
	public static ArrayList<TestProject> reRankProjectList ( ArrayList<TestProject> projectList ) {
		ArrayList<String> projectRank = obtainProjectRankTimeSeries();
		
		ArrayList<TestProject> reRankedProjectList = new ArrayList<TestProject>();
		for ( int i =0; i < projectRank.size(); i++ ) {
			String projectName = projectRank.get( i );
			
			for ( int j =0; j < projectList.size(); j++ ) {
				String inProjectName = projectList.get( j).getProjectName();
				if ( projectName.equals( inProjectName )) {
					reRankedProjectList.add( projectList.get( j ));
					break;
				}
			}
		}
		return reRankedProjectList;
	}
	
	public static ArrayList<TestProject> randomRankProjectList ( ArrayList<TestProject> projectList ){
		ArrayList<TestProject> reRankedProjectList = new ArrayList<TestProject>();
				
		for ( int i =0; i < projectList.size(); i++ ){
			TestProject project = projectList.get( i );
			reRankedProjectList.add( project );
		}
		
		long t = System.currentTimeMillis();
		Random rand = new Random(t);
		for ( int i =0; i < reRankedProjectList.size(); i++ ){
			int randValue = rand.nextInt( reRankedProjectList.size() );
			TestProject tempProject = reRankedProjectList.get( i );
			reRankedProjectList.set(i, reRankedProjectList.get(randValue ));
			reRankedProjectList.set( randValue, tempProject );
		}
		rand = null;
		
		return reRankedProjectList;
	}
}

