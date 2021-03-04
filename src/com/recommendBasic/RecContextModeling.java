package com.recommendBasic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.data.Constants;
import com.data.TestProject;
import com.data.TestReport;
import com.data.TestTask;
import com.dataProcess.ReportSegment;
import com.dataProcess.WordEmbeddingReader;


public class RecContextModeling {
	Date earliestTime;
	
	public RecContextModeling() {
		// TODO Auto-generated constructor stub
		try {
			SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			earliestTime = dateFormat.parse( "2015-01-01 00:00:00" );
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//ĳ��ʱ����ϣ�������Ա֮ǰ���л�Ļ��ܣ���WorkerActiveHistory����ʱ����н�ȡ
	public HashMap<String, HashMap<Date, ArrayList<String>>> modelActivenessContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<String>>> workerActiveHistory ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint ).getSubmitTime();
		
		HashMap<String, HashMap<Date, ArrayList<String>>> curActiveList = new HashMap<String, HashMap<Date, ArrayList<String>>>();
		for ( String workerId : workerActiveHistory.keySet() ){
			HashMap<Date, ArrayList<String>> activeHistory = workerActiveHistory.get( workerId );
			HashMap<Date, ArrayList<String>> curActive = new HashMap<Date, ArrayList<String>>();
			for ( Date date : activeHistory.keySet() ){
				if ( date.getTime() <= curTime.getTime() ){
					curActive.put( date, activeHistory.get( date));
				}
			}
			curActiveList.put( workerId, curActive );
		}
		
		return curActiveList;
	}
	
	//��ĳ��ʱ����ϣ�������Ա֮ǰ���л�Ļ��ܣ���workerExpertiseHistory����ʱ����н�ȡ
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> modelExpertiseRawContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint ).getSubmitTime();
		
		HashMap<String, HashMap<Date, ArrayList<List<String>>>> curExpertiseList = new HashMap<String, HashMap<Date, ArrayList<List<String>>>>();
		for ( String workerId : workerExpertiseHistory.keySet() ){
			HashMap<Date, ArrayList<List<String>>> expertiseHistory = workerExpertiseHistory.get( workerId );
			HashMap<Date, ArrayList<List<String>>> curExpertise = new HashMap<Date, ArrayList<List<String>>>();
			for ( Date date : expertiseHistory.keySet() ){
				if ( date.getTime() <= curTime.getTime() ){
					curExpertise.put( date, expertiseHistory.get( date ));
				}
			}
			curExpertiseList.put( workerId, curExpertise );
		}
		
		return curExpertiseList;
	}
	
	public HashMap<String, HashMap<Date, ArrayList<List<String>>>> modelPreferenceRawContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory ){
		return this.modelExpertiseRawContext(project, recTimePoint, workerPreferenceHistory);
	}
	
	public HashMap<String, HashMap<Date, Double[]>> modelExpertiseRawContextSemantic ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, Double[]>> workerExpertiseHistory ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint ).getSubmitTime();
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime( curTime );//�ѵ�ǰʱ�丳������
		calendar.add(Calendar.MONTH, -6);  //����Ϊǰ3��
		Date threeMonthTime = calendar.getTime();   //�õ�ǰ3�µ�ʱ��
		
		HashMap<String, HashMap<Date, Double[]>> curExpertiseList = new HashMap<String, HashMap<Date, Double[]>>();
		for ( String workerId : workerExpertiseHistory.keySet() ){
			HashMap<Date, Double[]> expertiseHistory = workerExpertiseHistory.get( workerId );
			HashMap<Date, Double[]> curExpertise = new HashMap<Date, Double[]>();
			for ( Date date : expertiseHistory.keySet() ){
				if ( date.getTime() <= curTime.getTime() && date.getTime() >= threeMonthTime.getTime() ){
					curExpertise.put( date, expertiseHistory.get( date ));
				}
			}
			curExpertiseList.put( workerId, curExpertise );
		}
		return curExpertiseList;
	}
	public HashMap<String, HashMap<Date, Double[]>> modelPreferenceRawContextSemantic ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, Double[]>> workerPreferenceHistory ){
		return this.modelExpertiseRawContextSemantic(project, recTimePoint, workerPreferenceHistory);
	}
	
	//ĳ��ʱ����ϣ�������Ա֮ǰ������bug report�ύ�����ݹ��ɵľ���
	public HashMap<String, HashMap<String, Integer>> modelExpertiseContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerExpertiseHistory ){
		Date curTime = project.getTestReportsInProj().get( recTimePoint ).getSubmitTime();
		
		//<workerId, <term, bugNum>>
		HashMap<String, HashMap<String, Integer>> curExpertiseList = new HashMap<String, HashMap<String, Integer>>();
		for ( String workerId: workerExpertiseHistory.keySet() ){
			HashMap<Date, ArrayList<List<String>>> history = workerExpertiseHistory.get( workerId );
			
			HashMap<String, Integer> curExpertise = new HashMap<String, Integer>();
			if ( curExpertiseList.containsKey( workerId )){
				curExpertise = curExpertiseList.get( workerId );
			}
			
			for ( Date date : history.keySet() ){
				if ( date.getTime() <= curTime.getTime() ){
					ArrayList<List<String>> reportsList = history.get( date );
					for ( int i =0; i <reportsList.size(); i++ ){
						List<String> termList = reportsList.get( i );
						for ( int j =0; j < termList.size(); j++ ){
							String term = termList.get( j );
							int bugNum = 1;
							if ( curExpertise.containsKey( term )){
								bugNum = curExpertise.get( term ) +1 ;
							}
							curExpertise.put( term, bugNum );
						}
					}
				}
			}
			
			curExpertiseList.put( workerId, curExpertise );
		}
		return curExpertiseList;
	}
	
	public HashMap<String, HashMap<String, Integer>> modelPreferenceContext ( TestProject project, int recTimePoint, HashMap<String, HashMap<Date, ArrayList<List<String>>>> workerPreferenceHistory ){
		return this.modelExpertiseContext(project, recTimePoint, workerPreferenceHistory);
		//<workerId, <term, reportNum>>
	}
	
	//ĳ��ʱ����ϣ���ǰ���������Ѿ��ύ��ȱ���������Щȱ����term�ϵķֲ����
	public HashMap<String, Double> modelTestContext ( TestProject project, int recTimePoint, TestTask task ){
		ArrayList<String> taskDescription = task.getTaskDescription();
		
		ArrayList<TestReport> submitReportList = new ArrayList<TestReport>();
		ArrayList<ArrayList<String>> submitTermsList = new ArrayList<ArrayList<String>>();
		for ( int i = 0; i <= recTimePoint && i < project.getTestReportsInProj().size(); i++ ){
			TestReport report = project.getTestReportsInProj().get( i);
			String bugTag = report.getTag();
			if ( bugTag.equals("��˲�ͨ��"))
				continue;
			submitReportList.add( report );
			
			//String[] termArray = segTool.segmentTestReport(report);
			String[] wordsDetail = report.getBugDetail().split( " ");
			String[] wordsSteps = report.getReproSteps().split( " ");
			ArrayList<String> termList = new ArrayList<String>();
			for ( int j=0; j < wordsDetail.length; j++ )
				termList.add( wordsDetail[j] );
			for( int j =0; j < wordsSteps.length; j++ ){
				termList.add( wordsSteps[j] );
			}
			
			submitTermsList.add( termList );
		}
		
		//<term, bug report with term/bug report> , terms are the these in the task descriptions
		HashMap<String, Double> termAdequacyList = new HashMap<String, Double>();
		for ( int i =0; i < taskDescription.size(); i++ ){
			String term = taskDescription.get( i );
			int bugWithTerm = 0;
			for ( int j =0; j < submitTermsList.size(); j++ ){
				ArrayList<String> termsList = submitTermsList.get( j );
				if ( termsList.contains( term )){
					bugWithTerm++;
				}
			}
			
			Double termAdeq = 0.0;
			if ( submitTermsList.size() != 0 ){
				termAdeq = (1.0*bugWithTerm) / submitTermsList.size();
			}
			
			termAdequacyList.put( term, termAdeq );
		}
		
		return termAdequacyList;
	}
	
	public HashMap<String, Double> modelTestContextSimple ( TestTask task ){
		ArrayList<String> testDescrip = task.getTaskDescription();
		
		HashMap<String, Double> termAdequacyList = new HashMap<String, Double>();
		for ( int i =0; i < testDescrip.size(); i++){
			termAdequacyList.put( testDescrip.get(i), 0.0 );
		}
		
		return termAdequacyList;
	}
	
	public Double[] modelTestContextSimpleSemantic ( TestTask task ){
		ArrayList<String> testDescrip = task.getTaskDescription();
		
		WordEmbeddingReader wordEmbReader = new WordEmbeddingReader();
		HashMap<String, Double[]> word2VecList = wordEmbReader.readWordEmbedding();
		
		int vecSize = Constants.WORD_EMBED_VECTOR_SIZE;
		Double[] sent2Vec = new Double[vecSize];
		for ( int i =0; i < sent2Vec.length; i++ ){
			sent2Vec[i] = 0.0;
		}
		
		for ( int i =0; i < testDescrip.size(); i++ ){
			String term = testDescrip.get(i);
			if ( !word2VecList.containsKey( term )){
				continue;
			}
			Double[] vecValues = word2VecList.get( term );
			for ( int j =0; j< vecValues.length; j++ ){
				sent2Vec[j] += vecValues[j];
			}
		}
		return sent2Vec;
	}
}
