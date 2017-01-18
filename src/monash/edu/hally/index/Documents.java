package monash.edu.hally.index;


import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import monash.edu.hally.constant.ModelConstants;
import monash.edu.hally.global.ModelVariables;
import monash.edu.hally.nlp.FilesUtil;
import monash.edu.hally.nlp.TopSimilarityComparable;

public class Documents {
	
	public ArrayList<Document> docs=new ArrayList<Document>();
		
	/**
	 * 作用：对所有文档索引化
	 */
	public void indexAllDocuments()
	{
		if(new File(ModelConstants.DOCUMENTS_PATH).listFiles().length==0){
			System.err.println("Original documents are null, please add documents.");
			System.exit(0);
		}
		System.out.println("Begin to extend dictionary and index documents.");
		
		ArrayList<String> tweetsData=FilesUtil.readDocument(
				ModelConstants.DOCUMENTS_PATH+ModelConstants.DOCUMENT_NAME);
		
		for (int i = 0; i < tweetsData.size(); i++) {
			System.out.println("Indexing document["+(i+1)+"]");
			Document document=new Document(tweetsData.get(i));
			document.indexDocument();
			ModelVariables.g_wordCount += document.docWords.length;
			docs.add(document);
		}
	}
	
	/**
	 * 学习每篇推文全文相关的hashtag集合
	 */
	public void extendRelatedHashtag()
	{
		System.out.println("Begin to extend related hashtags of all documents.");
		Map<String,ArrayList<Integer>> hashtag2RelateSetMap = getRelatedHashtagMap(getHashtag2vectorMap(),0.6);
		for (int i = 0; i < docs.size(); i++) {
			if(docs.get(i).hasHashtag){
				for (Integer hashtagIndex : docs.get(i).getOwnHashtagList()) {
					String hashtag = ModelVariables.g_hashtagDictionary.get(hashtagIndex);
					docs.get(i).getPotentialHashtagList().addAll(hashtag2RelateSetMap.get(hashtag));
				}
			}
		}
	}
	
	
	/**
	 * 获取hashtag对应的词向量
	 */
	private Map<String,double[]> getHashtag2vectorMap()
	{
		Map<String,double[]> candiHashtagsVec = FilesUtil.getCandiHashtagsVec();
		Map<String,double[]> hashtags2VecMap = new HashMap<>();
		for (String hashtag : ModelVariables.g_hashtagDictionary) {
			for (Entry<String,double[]> entry : candiHashtagsVec.entrySet()) {
				if(hashtag.equals(entry.getKey())){
					double vector[] = entry.getValue().clone();
					hashtags2VecMap.put(hashtag, vector);
					break;
				}
			}
		}
		return hashtags2VecMap;
	}
	
	/**
	 * 得到hashtag全文相关的hashtag集合。
	 * @param hashtags2VecMap
	 * @param threshold: 关联度阈值，默认为0.6
	 * @return hashtag相关的集合，例如 #happy [1,3,5] 其中数字为hashtag字典中的索引
	 */
	private Map<String, ArrayList<Integer>> getRelatedHashtagMap(Map<String,double[]> hashtags2VecMap, double threshold)
	{
		Map<String,ArrayList<Integer>> hashtag2SetMap = new HashMap<>();
		for (Entry<String,double[]> entry1 : hashtags2VecMap.entrySet()) {
			ArrayList<String> arrayList=new ArrayList<String>();
			Map<String,Double> hashtag2ScoreMap = new HashMap<>();
			for (Entry<String,double[]> entry2 : hashtags2VecMap.entrySet()) {
				double cosinValue = FilesUtil.getCosineValue(entry1.getValue(), entry2.getValue());
				if(cosinValue >= threshold){ //默认0.6
					arrayList.add(entry2.getKey());
					hashtag2ScoreMap.put(entry2.getKey(), cosinValue);
				}
			}
			ArrayList<Integer> relatedHashtags = new ArrayList<Integer>();
			Collections.sort(arrayList,new TopSimilarityComparable(hashtag2ScoreMap));
			int min = Math.min(3, arrayList.size());
			for (int i = 0; i < min; i++) {
				relatedHashtags.add(ModelVariables.g_hashtagToIndexMap.get(arrayList.get(i)));
			}
			hashtag2SetMap.put(entry1.getKey(), relatedHashtags);
		}
		return hashtag2SetMap;
	}
	

	
	/**
	 * 学习每篇推文潜在的hashtag集合
	 */
//	public void extendPotentialHashtag()
//	{
//		System.out.println("Begin to extend potential hashtags of all documents.");
//		for (int i = 0; i < docs.size(); i++) {
//			if(docs.get(i).hasHashtag){
//				extendHashtagAhead(docs, i);
//			}
//		}
//		
//		// 设置PotentialHashtagList
//		for (int i = 0; i < docs.size(); i++) {
//			if(docs.get(i).hasHashtag){
//				docs.get(i).setPotentialHashtagList(docs.get(i).potentialHashtagSet);
//			}
//		}
//		
//	}
//	
//	private void extendHashtagAhead(ArrayList<Document> docs,int currentIndex)
//	{
//		Document currentDoc = docs.get(currentIndex);
//		for (int i = 0; i < currentIndex; i++) {
//			if(docs.get(i).hasHashtag){
//				boolean needUpdate = false;
//				for (String hashtag : docs.get(i).potentialHashtagSet) {
//					if(currentDoc.potentialHashtagSet.contains(hashtag)){
//						needUpdate = true;
//						break;
//					}	
//				}
//				// 更新索引为i的文档的potentialHashtagSet
//				if(needUpdate){
//					currentDoc.potentialHashtagSet.addAll(docs.get(i).potentialHashtagSet);
//					docs.get(i).potentialHashtagSet.addAll(currentDoc.potentialHashtagSet);
//				}
//			}
//		}
//	}

	
	
	public static void main(String[] args) throws IOException {
		
		Documents docs=new Documents();
		
		docs.indexAllDocuments();
		System.out.println("DocsSize:\t"+docs.docs.size()
		+"\tAverageWordLen:\t"+(double)(ModelVariables.g_wordCount / (docs.docs.size()+0.0))
		+"\tUserSize:\t"+ModelVariables.g_usersSet.size()
		+"\tTimeSize:\t"+ModelVariables.g_timesSet.size()
		+"\tTermSize:\t"+ModelVariables.g_termDictionary.size()
		+"\tWordSize:\t"+ModelVariables.g_wordCount
		+"\tAverageHashtagLen:\t"+(double)(ModelVariables.g_hashtagCount / (docs.docs.size()+0.0))
		+"\tHashtagSize:\t"+ModelVariables.g_hashtagsSet.size());
		
//		docs.extendPotentialHashtag();
//		docs.extendRelatedHashtag();
		
		
		int m=0;
		
//		BufferedWriter writer=new BufferedWriter(new FileWriter(new File("hashtag.txt")));
//		for (Document document : docs.docs) {
//			m++;
//			if(document.hasHashtag){
//				writer.write(m+"\t:");
//				for (Integer index : document.getHashtags()) {
//					writer.write(ModelVariables.g_hashtagDictionary.get(index)+"["+index+"]\t");
//				}
//				writer.write(document.potentialHashtagSet.size()+"-"+document.potentialHashtagSet.toString()+"\t");
//				writer.write(document.getPotentialHashtagList().toString());
//				writer.write("\n");
//			}
//			
//		}
//		writer.close();
//		for (Document document : docs.docs) {
//			m++;
//			if(document.hasHashtag){
//				System.out.print(m+"\t:");
//				for (Integer index : document.getOwnHashtagList()) {
//					System.out.print(ModelVariables.g_hashtagDictionary.get(index)+"["+index+"]\t");
//				}
////				System.out.print(document.potentialHashtagSet.size()+"-"+document.potentialHashtagSet.toString()+"\t");
//				System.out.print(document.getPotentialHashtagList().toString());
//				System.out.println();
//			}
//			
//		}
//		
//		for (Entry<String, Integer> entry : ModelVariables.g_hashtagToIndexMap.entrySet()) {
//			System.out.println(entry.getKey()+"\t"+entry.getValue());
//		}
		
		
//		for (Entry<String,double[]> entry : docs.getWord2vectorMap().entrySet()) {
//			System.out.print(entry.getKey()+"\t");
//			for (double d : entry.getValue()) {
//				System.out.print(d+" ");
//			}
//			System.out.println();
//		}

	}

}
	
