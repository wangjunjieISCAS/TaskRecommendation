package com.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/*
 * ֻͳ���ڵ�����ʵ������Ŀ��>1����Ա�����ڵ���û�в�����Ŀ�ģ���ͳ�ơ�
 * ���ͳ�ƵĻ���Ч������ܶࡣ��Ϊ��Щ�˻ᱻ�Ƽ�task����ʵ���ϲ�û�в��롣Ԥ���׼ȷ�ʱȽϵͣ��ٻ��ʺܸߡ�
 * 
 * !!!����ǱȽ�ԭʼ����ʽ���ӻ�����performanceFile�ж�ȡ������
 */
public class RecPerformance {
	String performanceFolder = "data/output/baseline/taskRec/performance";
	String sumPerformanceFolder = "data/output/baseline/taskRec";

	/*
	public void summarizeRecommendationPerformance ( ){
		LinkedHashMap<String, ArrayList<Double[]>> performanceList = new LinkedHashMap<String, ArrayList<Double[]>>();
		TaskRecommendationAndPerformance performanceTool = new TaskRecommendationAndPerformance();
		
		File projectsFolder = new File ( performanceFolder );
		if ( projectsFolder.isDirectory() ){
			String[] projectFileList = projectsFolder.list();
			for ( int i = 0; i< projectFileList.length; i++ ){
				String fileName = performanceFolder + "/" + projectFileList[i];
				
				//performance-80.csv
				String projectIndex = projectFileList[i].replace( "performance-", "");
				projectIndex = projectIndex.replace(".csv", "");
				Integer index = Integer.parseInt( projectIndex );
				if ( index == 67 )  //outlier
					continue;
				
				ArrayList<ArrayList<Double>> performanceDetailList = performanceTool.readRecommendationPerformance( fileName );
				
				//statistics
				Double[] precisionStatis = this.obtainStatistics( performanceDetailList.get(0));
				Double[] recallStatis = this.obtainStatistics( performanceDetailList.get(1));
				//Double[] FMeasureStatis = this.obtainStatistics( performanceDetailList.get(2));
				Double[] saveEffortStatis = this.obtainStatistics( performanceDetailList.get(2));
				
				ArrayList<Double[]> statisResult = new ArrayList<Double[]>();
				statisResult.add( precisionStatis );
				statisResult.add( recallStatis );
				//statisResult.add( FMeasureStatis );
				statisResult.add( saveEffortStatis );
				performanceList.put( projectFileList[i], statisResult );
			}				
		}	
		
		try {
			BufferedWriter writer = new BufferedWriter ( new FileWriter ( new File ( sumPerformanceFolder + "/performance-summarized.csv" ) ));
			String[] category = { "precision", "recall", "savedEffort"};
			String[] statis = { "min", "oneQuart", "median", "thirdQuart", "max"};
			writer.write(" " + ",");
			for ( int i =0; i < category.length; i++ ){
				for ( int j=0; j < statis.length; j++ ){
					writer.write( category[i]+"-"+statis[j] +",");
				}
			}
			writer.newLine();
			for ( String recGroup : performanceList.keySet() ){
				writer.write( recGroup + ",");
				ArrayList<Double[]> performance = performanceList.get( recGroup );
				for ( int i =0; i < performance.size(); i++ ){
					Double[] values = performance.get(i);
					for ( int j =0; j < values.length; j++ ){
						writer.write( values[j] + ",");
					}
				}
				writer.newLine();
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Double[] obtainStatistics ( ArrayList<Double> performanceList ){
		Collections.sort( performanceList );
		int size = performanceList.size();
		int medSize = (size+1)/2;
		int quartSize = (size+3)/4;
		Double min = performanceList.get(0);
		Double max = performanceList.get( size-1 );
		Double median = performanceList.get( medSize );
		Double oneQuart = performanceList.get( quartSize );
		Double thirdQuart = performanceList.get( size - quartSize );
		
		Double[] result = {min, oneQuart, median, thirdQuart, max };
		return result;
	}
	*/
	public void deriveRecommendationPerformance ( ){
		//recTime, <precisionList, recallList, saveEffortList>> һ���Ƽ�ʱ����ж���˵�����
		LinkedHashMap<String, ArrayList<ArrayList<Double>>> performanceList = new LinkedHashMap<String, ArrayList<ArrayList<Double>>>();
		PerformanceReader performanceTool = new PerformanceReader();
		
		File projectsFolder = new File ( performanceFolder );
		if ( projectsFolder.isDirectory() ){
			String[] projectFileList = projectsFolder.list();
			for ( int i = 0; i< projectFileList.length; i++ ){
				String fileName = performanceFolder + "/" + projectFileList[i];
				
				//performance-80.csv
				String projectIndex = projectFileList[i].replace( "performance-", "");
				projectIndex = projectIndex.replace(".csv", "");
				Integer index = Integer.parseInt( projectIndex );
				//if ( index == 53 || index == 61 || index == 62 ||index == 63 || index == 59
					//	 || index == 67 ||index == 68 || index == 78  || index == 79 ||index == 69 ||  index == 70 )  //outlier   //67 for 0:00; 65 for 8:00 12:00; 68 for 18:00 
					//continue;
				
				ArrayList<ArrayList<Double>> performanceDetailList = performanceTool.readRecommendationPerformance( fileName );
				if ( performanceDetailList.get(0).size() > 200 )
					continue;
				performanceList.put( projectFileList[i], performanceDetailList );
			}				
		}	
		
		PerformanceWriter performanceWriter = new PerformanceWriter();
		performanceWriter.writePerformance( sumPerformanceFolder + "/performance-total.csv", performanceList);
		performanceWriter.writePerformancePlot(sumPerformanceFolder + "/performance-total-plot.csv" , performanceList);
	}
	
	
	public static void main ( String[] args ){
		RecPerformance recPerformance = new RecPerformance();
		//recPerformance.summarizeRecommendationPerformance();
		recPerformance.deriveRecommendationPerformance();
	}
}
