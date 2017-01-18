package monash.edu.hally.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;


import monash.edu.hally.constant.ModelConstants;
import monash.edu.hally.global.ModelParameters;
import monash.edu.hally.global.ModelVariables;
import monash.edu.hally.index.Document;
import monash.edu.hally.index.Documents;



public class FilesUtil {
	
	
	public static double getCosineValue(double[] a,double[] b)
	{
		double pointMulti = 0;
		for (int i = 0; i < a.length; i++) {
			pointMulti += a[i] * b[i];
		}
		
		double sqrtMulti = 0,squareA=0,squareB=0;
		for (int i = 0; i < a.length; i++) {
			squareA += a[i] * a[i];
		}
		for (int i = 0; i < a.length; i++) {
			squareB += b[i] * b[i];
		}
		sqrtMulti = Math.sqrt(squareA * squareB);
		
		return pointMulti / sqrtMulti;
	}
	
	public static Map<String,double[]> getCandiHashtagsVec()
	{
		try {
			BufferedReader reader=new BufferedReader(new FileReader(new File(
					ModelConstants.WORDVECTOR_PATH+ModelConstants.WORDVECTOR_FILE)));
			String line;
			Map<String,double[]> candiHashtags2VecMap = new HashMap<>();
			while((line=reader.readLine())!=null)
			{
				if(line.startsWith("#")){
					String parts[] = line.split(" |\t");
					double vector[] = new double[parts.length-1];
					for (int i = 0; i < vector.length; i++) {
						vector[i] = Double.parseDouble(parts[i+1]);
					}
					candiHashtags2VecMap.put(parts[0], vector);
				}
			}
			reader.close();
			return candiHashtags2VecMap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * ���ã���һƪ�ĵ���һ������Ϊ��λ����ArrayList�У�Ŀ��Ϊ�˷ִʡ�
	 * @param documentName:Դ�ļ��ľ���·����
	 */
	public static ArrayList<String> readDocument(String documentName)
	{
		try {
			BufferedReader reader=new BufferedReader(new FileReader(new File(documentName)));
			String line;
			ArrayList<String> documentLines=new ArrayList<String>();
			while((line=reader.readLine())!=null)
			{
				documentLines.add(line.trim());
			}
			return documentLines;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void saveFile(ArrayList<String> strings, String fileName)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(fileName)));
			
			for (String string : strings) {
				writer.write(string + ModelConstants.CR_LF);
			}
			writer.close();
		} catch (IOException e) {
//			 Logger.getLogger(ModelConstants.class.getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
		}
	}
	
	/**
	 * @param tweetData: a tweet which contains time and user
	 * @return
	 */
	public static ArrayList<String> tokenize(String tweetData)
	{
		
		String items[]=tweetData.split(ModelConstants.LINK_FLAG);		
		ArrayList<String> tokens=new ArrayList<String>();
		tokens.add(items[0]); //add sentiment
		tokens.add(formalizeTweetTime(items[1])); //add time
		tokens.add(items[2]); //add user
		StringTokenizer tokenizer=new StringTokenizer(items[3]);
		while(tokenizer.hasMoreTokens())
		{
			String token=tokenizer.nextToken();	
			tokens.add(token.toLowerCase().trim());
		}	
		return tokens;
	}
	
	/**
	 * @param time: the time of a tweet, such as: Mon Apr 06 22:19:45 PDT 2009
	 * @return the formalized time, such as: 2009-Apr-06 
	 */
	public static String formalizeTweetTime(String time)
	{
		String items[]=time.split(" ");	
		String formalizedTime=items[5]+"-"+items[1]+"-"+items[2];
		return formalizedTime;	
	}
	
	/**
	 * ���ã��Ƴ����Ϲ��Ĵ�
	 * @param documentLines
	 */
	public static String removeWords(String line)
	{
		String newline="";
		StringTokenizer tokenizer=new StringTokenizer(line);
		
		while(tokenizer.hasMoreTokens())
		{
			String token=tokenModify(tokenizer.nextToken());	//ȥ���ʻ���󲿷ֵı�����	
			if(!isNoiseWord(token)&&!Stopwords.isContains(token)){//ȥ���ȥͣ�ô�
				newline += token.toLowerCase().trim()+"\t";
				
			}
		}	
		return newline;
	}
	
	/**
	 * ���ã�ȥ���ʻ���󲿷ֵı�����	
	 */
	public static String tokenModify(String token)
	{
		String subToken=token.substring(0, token.length()-1);
		return subToken+token.substring(token.length()-1).replaceAll("\\pP|\\pS", "");
	}

	/**
	 * ���ã�ȥ��
	 * @param token
	 */
	public static boolean isNoiseWord(String token) {
		token = token.toLowerCase().trim();
		// filter @xxx and URL
		if(token.matches(".*www\\..*") || token.matches(".*\\.com.*") || 
				token.matches(".*http:.*") )
			return true;
		//filter mention
		if(token.matches("[@��][a-zA-Z0-9_]+"))
			return true;
		//filter space
		if(token.matches("\\s*"))
			return true;
		//filter digit
		if(token.matches("\\d*"))
			return true;
		//filter signal
		if(token.matches("\\pP*")) 
			return true;
		//filter w
		if(token.matches("[a-z]"))  
			return true;
		return false;
	}
	
	public static void saveSentiPrecision(double[][]pi, Documents documents)
	{
		int TP=0,TN=0,FP=0,FN=0;
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".precision")));
			for (int m = 0; m < pi.length; m++) {
				//4 ��ʾ��ʵ������  1��ʾ��ʵ�ĸ���
				String sentiDoc=documents.docs.get(m).getSentiment();
				if(sentiDoc.equals("0") && pi[m][1] > pi[m][2]) FP++;
				if(sentiDoc.equals("4") && pi[m][1] < pi[m][2]) FN++;
				if(sentiDoc.equals("4") && (pi[m][1] > pi[m][2])) TP++;	// p(pos) > p(neg)
				if(sentiDoc.equals("0") && (pi[m][1] < pi[m][2])) TN++;	
			}			
			double precision= (double) TP / (TP+FP);
			double recall= (double) TP / (TP+FN);
			double accuracy = (double) (TP+TN) / (TP+TN+FP+FN);
			double F1 = (double) (2*precision*recall) / (precision+recall);
			writer.write("PositiveNum + NegativeNum = "+(TP+TN+FP+FN)+"\t"+"PositiveNum = "+(TP+FN)+
					"\t"+"NegativeNum = "+(TN+FP)+ModelConstants.CR_LF);
			writer.write("TP="+TP+"\t"+"FN="+FN+"\t"+"TN="+TN+"\t"+"FP="+FP+"\t"+ModelConstants.CR_LF);
			writer.write("Accuracy: "+accuracy+ModelConstants.CR_LF);
			writer.write("Precision: "+precision+ModelConstants.CR_LF);
			writer.write("Recall: "+recall+ModelConstants.CR_LF);
			writer.write("F1: "+F1+ModelConstants.CR_LF);
			writer.close();		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveSentiDist(double[][]pi)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".dsdist")));
			for (int m = 0; m < pi.length; m++) {
				for (int s = 0; s < pi[0].length; s++) {
					writer.write(pi[m][s]+"\t");
				}
				writer.write(ModelConstants.CR_LF);
			}
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void saveCount(int nms[][])
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".senti_count")));
		
			for (int i = 0; i < nms.length; i++) {
				for (int j = 0; j < nms[0].length; j++) {
					writer.write(nms[i][j]+"\t");
				}
				writer.write(ModelConstants.CR_LF);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ���ã���������ֲ�
	 * @param delta: {�û�-���}���µ�����ֲ�
	 * @param theta: {ʱ��-���}���µ�����ֲ�
	 */
	public static void saveDistributions(double[][][]delta,double[][][]theta,double[][][]ou)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".userskdist")));
			String sentiLable[] = {"neutral","positive","negative"};
			for (int u = 0; u < delta.length; u++) {
				//����û���������С��5���Ͳ�������û�����Ϣ
				if(ModelVariables.g_userToCountMap.get(ModelVariables.g_usersSet.get(u)) < 5) continue; 
				for (int s = 0; s < delta[0].length; s++) {
					writer.write("["+ModelVariables.g_usersSet.get(u)+","+sentiLable[s]+"] : ");
					for (int k = 0; k < delta[0][0].length; k++) {
						writer.write(delta[u][s][k]+"\t");
					}
					writer.write(ModelConstants.CR_LF);
				}
			}		
			writer.close();
			
			writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".timeskdist")));
			for (int t = 0; t < theta.length; t++) {
				for (int s = 0; s < theta[0].length; s++) {
					writer.write("["+ModelVariables.g_timesSet.get(t)+","+sentiLable[s]+"] : ");
					for (int k = 0; k < theta[0][0].length; k++) {
						writer.write(theta[t][s][k]+"\t");
					}
					writer.write(ModelConstants.CR_LF);
				}
			}				
			writer.close();
			
			if(ou == null) return;
			writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".hashtagskdist")));
			for (int h = 0; h < ou.length; h++) {
				for (int s = 0; s < ou[0].length; s++) {
					writer.write("["+ModelVariables.g_hashtagDictionary.get(h)+","+sentiLable[s]+"] : ");
					for (int k = 0; k < ou[0][0].length; k++) {
						writer.write(ou[h][s][k]+"\t");
					}
					writer.write(ModelConstants.CR_LF);
				}
			}				
			writer.close();
			
//			writer=new BufferedWriter(new FileWriter(new File(
//					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".timescount")));
//			for (int t = 0; t < theta.length; t++) {
//				double docsOfTime = ModelVariables.g_timeToCountMap.get(ModelVariables.g_timesSet.get(t));
//				
//					writer.write(ModelVariables.g_timesSet.get(t)+"\t"+docsOfTime+ModelConstants.CR_LF);
//				
//			}				
//			writer.close();
//			
//			writer=new BufferedWriter(new FileWriter(new File(
//					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".timeskcount")));
//			for (int t = 0; t < theta.length; t++) {
//				double docsOfTime = ModelVariables.g_timeToCountMap.get(ModelVariables.g_timesSet.get(t));
//				for (int s = 0; s < theta[0].length; s++) {
//					writer.write("["+ModelVariables.g_timesSet.get(t)+","+sentiLable[s]+"] : ");
//					for (int k = 0; k < theta[0][0].length; k++) {
//						writer.write(theta[t][s][k] * docsOfTime +"\t");
//					}
//					writer.write(ModelConstants.CR_LF);
//				}
//			}				
//			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param phiB: �����ʷֲ�
	 * @param topNum: ����ǰtopNum����
	 */
	public static void saveTopBGWords(double[]phiB,int topNum)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".bg_topwords")));
		
			ArrayList<Integer> arrayList=new ArrayList<Integer>();
			for (int v = 0; v < phiB.length; v++) {
				arrayList.add(new Integer(v));				
			}
			Collections.sort(arrayList,new TopWordComparable(phiB));
			for (int i = 0; i < topNum; i++) {
				writer.write(ModelVariables.g_termDictionary.get(arrayList.get(i))+"\t");
				writer.write(phiB[arrayList.get(i)]+"\t");
			}
			writer.write(ModelConstants.CR_LF);	
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ã��������������£�������ߵ�TOP_NUM��Ĭ��Ϊ10��������
	 * @param phi {��У�����}�µĴʻ�ֲ�
	 */
	public static void saveTopTopicWords(double[][][]phi,int topNum)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".sk_topwords")));
			String sentiLable[] = {"neutral","positive","negative"};
			for (int s = 0; s < phi.length; s++) {
				for (int k = 0; k < phi[0].length; k++) {
					ArrayList<Integer> arrayList=new ArrayList<Integer>();
					for (int v = 0; v < phi[0][0].length; v++) {
						arrayList.add(new Integer(v));				
					}
					Collections.sort(arrayList,new TopWordComparable(phi[s][k]));
					
					writer.write(sentiLable[s]+" topic_"+k+" : ");
					for (int i = 0; i < topNum; i++) {
						writer.write(ModelVariables.g_termDictionary.get(arrayList.get(i))+"\t");
//						writer.write(phi[s][k][arrayList.get(i)]+"\t");
					}
					writer.write(ModelConstants.CR_LF);
				}
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveSentiment(ArrayList<Document> docs)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".senti")));
		
			for (Document doc : docs) {
				writer.write(doc.getSentiment());
				writer.write(ModelConstants.CR_LF);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveDictionary()
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".dictionary")));
		
			for (String term : ModelVariables.g_termDictionary) {
				writer.write(term+ModelConstants.CR_LF);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ã������ĵ��Ĵʻ�index
	 */
	public static void saveDocWordIndex(ArrayList<Document> docs)
	{
		try {
			BufferedWriter writer=new BufferedWriter(new FileWriter(new File(
					ModelConstants.RESULTS_PATH+ModelConstants.MODEL_NAME+".doc_wordindex")));
				
			for (Document doc : docs) {
				for (int index : doc.docWords) {
					writer.write(index+" ");
				}
				writer.write(ModelConstants.CR_LF);
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ã���ӡ�ɹ�����Ϣ
	 */
	public static void printSuccessMessage()
	{
		String resultPath=System.getProperty("user.dir")+"\\data\\results";
//		JOptionPane.showMessageDialog(null, "Results are reserved in "+resultPath);
		System.out.println("Results are reserved in "+resultPath);
		try {	
			int choice=JOptionPane.showConfirmDialog(null, "Results are reserved in "+resultPath+
					"\nDo you want to open the dir of results ?", "Make a choice", JOptionPane.YES_NO_OPTION);
			if(choice==JOptionPane.OK_OPTION)
				java.awt.Desktop.getDesktop().open(new File(resultPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���ã�����Ĭ�ϵĲ����ļ� �����ļ��ж����������ڷ�ͼ�λ����棩
	 */
	private static void createParametersFile()
	{
		File file=new File(ModelConstants.PARAMETERS_PATH+ModelConstants.PARAMETERS_FILE);
		try {
			file.createNewFile();
			PrintWriter writer=new PrintWriter(file);
			
			writer.print("K (Number of topics):"+ModelConstants.SPLIT_FLAG+"5"+ModelConstants.CR_LF);
			writer.print("Top number:"+ModelConstants.SPLIT_FLAG+"10"+ModelConstants.CR_LF);
			writer.print("Iterations:"+ModelConstants.SPLIT_FLAG+"100"+ModelConstants.CR_LF);
			writer.print("Burn_in:"+ModelConstants.SPLIT_FLAG+"80"+ModelConstants.CR_LF);
			writer.print("SaveStep:"+ModelConstants.SPLIT_FLAG+"10"+ModelConstants.CR_LF);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * ���ã��Ӳ����ļ��ж�ȡģ�Ͳ��������ļ��ж����������ڷ�ͼ�λ����棩
	 */
	public static ModelParameters readParametersFile()
	{
		File file=new File(ModelConstants.PARAMETERS_PATH+ModelConstants.PARAMETERS_FILE);
		ModelParameters modelParameters=new ModelParameters();
		if(!file.exists())
			createParametersFile();
		
		ArrayList<String> lines = readDocument(file.getAbsolutePath());
		for (String line : lines) {
			String[] para=line.split(ModelConstants.SPLIT_FLAG);
			switch (para[0]) {
			case "Iterations:":
				modelParameters.setIterations(Integer.valueOf(para[1]));
				break;
			case "Burn_in:":
				modelParameters.setBurn_in(Integer.valueOf(para[1]));
				break;
			case "SaveStep:":
				modelParameters.setSaveStep(Integer.valueOf(para[1]));
				break;
			case "Top number:":
				modelParameters.setTopNum(Integer.valueOf(para[1]));
				break;
			default:
				modelParameters.setK(Integer.valueOf(para[1]));
				break;
			}
		}
		return modelParameters;
	}
	
}
