package monash.edu.hally.nlp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;


public class Preprocessing {
	
	public static Map<String, Integer> term2CountMap=new HashMap<>();
	
	public static ArrayList<String> contentsList=new ArrayList<>();
	
	
	public static boolean isReservedTerms(String term)
	{
		if(term.matches(".*/N"))
			return true;
		if(term.matches(".*/A"))
			return true;
		if(term.matches(".*/V"))
			return true;
		if(term.matches(".*/#"))
			return true;
		if(term.matches(".*/E"))
			return true;
		if(term.matches(".*/G"))
			return true;
		
//		if(term.matches(".*/R"))
//			return true;
		
		if(term.matches(".*/\\^"))
			return true;
		if(term.matches(".*/S"))
			return true;
		return false;
		
	}
	
	/**
	 * ���ã������ı��еĴ�Ƶ
	 * @param contents: �ı�����
	 * @param splitSignal: �ı��д���ķָ����
	 */
	public static void countTerms(ArrayList<String> contents, String splitSignal)
	{
		for (String content : contents) {
			String tokens[] = content.split(splitSignal);
			for (int i = 0; i < tokens.length; i++) {
				if(term2CountMap.containsKey(tokens[i])){
					int count=term2CountMap.get(tokens[i]);
					term2CountMap.put(tokens[i], ++count);
				}
				else
					term2CountMap.put(tokens[i], 1);
			}
		}
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
		//filter ������
		if(token.matches("\\pP*")) 
			return true;
		//filter �����ַ�
		if(token.matches("[a-z]"))    
			return true;
		//filter ��������
		if(token.matches(".*[\u4E00-\u9FA5].*")) 
			return true;
		return false;
	}
	
	/**
	 * ���ã��ж��ǲ��ǵ�Ƶ��
	 * @param word: ��ǰ��
	 * @param lowFrequency: ��Ƶ��
	 */
	public static boolean isLowFrequencyWord(String word, int lowFrequency)
	{
		if(term2CountMap.get(word) <= lowFrequency)
			return true;
		return false;
	}
	
	
	/**
	 * ���ã����Դ��� ȥͣ�ô� �� ȥ�룬ȥ��Ƶ��
	 * @param content: �ı�����
	 * @param splitSignal: �ı���ÿ���ʷָ�ķ��ţ�һ���ǿո����\t
	 * @return �Ƴ���ͣ�ôʵ��ı�����
	 */
	public static String removeWord(String content, String splitSignal)
	{
		
		String newContent="";
		String tokens[] = content.split(splitSignal);
		for (int i = 0; i < tokens.length; i++) {
			if(Stopwords.isContains(tokens[i])) continue;  //�Ƿ����ͣ�ôʣ����Ժ�ȥ�����
			newContent += tokens[i]+splitSignal;
		}
		
		return newContent;
	}

	/**
	 * ���ã���CVS�ļ�������arraylist<string>��������
	 * @param fileName: �ļ���
	 */
	public static ArrayList<String> readCVS(String fileName, String splitFlag) throws IOException
	{
		ArrayList<String> contents=new ArrayList<>();
		String encode = "UTF-8"; 
        File file = new File(fileName);  
        BufferedReader reader=new BufferedReader(
        		new InputStreamReader(new FileInputStream(file), encode));
        String line="";
        while ((line=reader.readLine())!=null) {
        	String newLine="";
			String tokens[]=line.split("\",\"");
			if(tokens.length==1){
				newLine = tokens[0].substring(1, tokens[0].length()-1); //��������
				contents.add(newLine);
				continue;
			}
			for (int i = 0; i < tokens.length; i++) {
				
				if(i==0) newLine += tokens[i].substring(1, tokens[i].length())+splitFlag;
				else if(i==tokens.length-1) newLine += tokens[i].substring(0, tokens[i].length()-1);
				else newLine += tokens[i]+splitFlag;
			}
//			System.out.println(newLine);
			contents.add(newLine);
		}
        reader.close();
		return contents;  
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
				documentLines.add(line);
			}
			return documentLines;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * @param fileName: Ҫд�ص��ļ���
	 * @param contents: �ı�����
	 */
	public static void writeFile(String fileName, ArrayList<String> contents) throws IOException
	{
		BufferedWriter writer=new BufferedWriter(new FileWriter(new File(fileName)));
		for (String string : contents) {
			writer.write(string+"\r\n");
		}
		writer.close();
	}

		
	public static void main(String[] args) throws Exception {
		
		System.out.println(isReservedTerms("http://www.qld.gov.au/Eloods/donate.html/U"));
		
		
//		ArrayList<String> contents=readCVS("data/training.1600000.processed.noemoticon.csv","&&&");
//		writeFile("data/sentiment140.data", contents);
		
//		String fileName="data/sentiment140(all).data";
//		ArrayList<String> contents=readDocument(fileName);
//		countTerms(contents, "");
	}

}
