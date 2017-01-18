package monash.edu.hally.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import monash.edu.hally.constant.ModelConstants;
import monash.edu.hally.global.ModelVariables;
import monash.edu.hally.nlp.FilesUtil;

public class Document {
	
	public int docWords[];
	public Map<Integer, Integer> indexToCountMap=new HashMap<Integer, Integer>();
	private String tweetData;
	
	private String user;
	private String time;
	private String sentiment;
	private ArrayList<Integer> ownHashtagList=new ArrayList<>();
	
	public int userIndex;
	public int timeIndex;
	
	public boolean hasHashtag = false;
//	public HashSet<String> potentialHashtagSet = new HashSet<>();
	
	private ArrayList<Integer> potentialHashtagList = new ArrayList<>();
	
//	public void setPotentialHashtagList(HashSet<String> potentialHashtagSet){
//		for (String hashtag : potentialHashtagSet) {
//			int index=ModelVariables.g_hashtagToIndexMap.get(hashtag);	
//			potentialHashtagList.add(index);
//		}
//	}
	
	public ArrayList<Integer> getPotentialHashtagList(){
		return potentialHashtagList;
	}
	
	
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	
	public String getSentiment() {
		return sentiment;
	}

	public void setSentiment(String sentiment) {
		this.sentiment = sentiment;
	}

	public Document(String tweetData)
	{
		this.tweetData = tweetData;
	}
	
	public ArrayList<Integer> getOwnHashtagList() {
		return ownHashtagList;
	}
	
	private void indexHashtag(String hashtagsData)
	{
		StringTokenizer tokenizer=new StringTokenizer(hashtagsData);
		while(tokenizer.hasMoreTokens())
		{
			String hashtag=tokenizer.nextToken();
			if(!hashtag.startsWith("#"))	continue;
//			potentialHashtagSet.add(hashtag);
			int index;
			ModelVariables.g_hashtagCount++;
			if(!ModelVariables.g_hashtagToIndexMap.containsKey(hashtag)){
				index=ModelVariables.g_hashtagsSet.size();
				ModelVariables.g_hashtagToIndexMap.put(hashtag, index);
				
				ModelVariables.g_hashtagsSet.add(hashtag);
				ModelVariables.g_hashtagDictionary.add(hashtag);
			}
			else{
				index=ModelVariables.g_hashtagToIndexMap.get(hashtag);	
			}
			if(!ownHashtagList.contains(index)){
				ownHashtagList.add(index);
			}
		}
		if(ownHashtagList.size() > 0)
			hasHashtag = true;
	}

	
	/**
	 * 作用：保存当前文档的时间和用户信息
	 */
	private void indexUserAndTime(ArrayList<String> tokens)
	{
		setSentiment(tokens.get(0));
		setTime(tokens.get(1));
		setUser(tokens.get(2));
		if(!ModelVariables.g_userToIndexMap.containsKey(getUser())){
			int index=ModelVariables.g_usersSet.size();
			ModelVariables.g_userToIndexMap.put(getUser(), index);
			userIndex=index;
			ModelVariables.g_usersSet.add(getUser());
			ModelVariables.g_userToCountMap.put(getUser(),1);
		}
		else{
			int count = ModelVariables.g_userToCountMap.get(getUser());
			ModelVariables.g_userToCountMap.put(getUser(), ++count);
			userIndex = ModelVariables.g_userToIndexMap.get(getUser());
		}
		if(!ModelVariables.g_timeToIndexMap.containsKey(getTime())){
			int index=ModelVariables.g_timesSet.size();
			ModelVariables.g_timeToIndexMap.put(getTime(), index);
			timeIndex=index;
			ModelVariables.g_timesSet.add(getTime());
		}
		else
			timeIndex = ModelVariables.g_timeToIndexMap.get(getTime());
		
	}
	
	/**
	 * 作用：统计token出现在文档中的次数
	 */
	private void wordCount(String token)
	{
		int gIndex=ModelVariables.g_termToIndexMap.get(token);
		if(!indexToCountMap.containsKey(gIndex))
			indexToCountMap.put(gIndex, 1);
		else{
			int count=indexToCountMap.get(gIndex);
			indexToCountMap.put(gIndex, ++count);
		}
	}
			
	
	/**
	 * 作用：将文档中的词汇添加到字典中。
	 * 文档中的每一个词汇索引n，用docWords[n]来表示在字典中对应的词项索引
	 */
	public void indexDocument()
	{
		ArrayList<String> tokens = FilesUtil.tokenize(tweetData);
		if(tweetData.split(ModelConstants.LINK_FLAG).length > 4){
			String hashtagsData = tweetData.split(ModelConstants.LINK_FLAG)[4];
			indexHashtag(hashtagsData);
		}
		docWords=new int[tokens.size()-3]; //exclude time and user
		indexUserAndTime(tokens);
		
		for (int n = 3; n < tokens.size(); n++) {
			String token=tokens.get(n);
			if(!ModelVariables.g_termToIndexMap.keySet().contains(token))// dictionary.contains(token))
			{
				int dictionarySize=ModelVariables.g_termDictionary.size();
				ModelVariables.g_termToIndexMap.put(token, dictionarySize);
				docWords[n-3]=dictionarySize;
				ModelVariables.g_termDictionary.add(token);
			}
			else
				docWords[n-3]=ModelVariables.g_termToIndexMap.get(token);
			wordCount(token);
		}
	}

	public static void main(String[] args) {
		

	}

}
