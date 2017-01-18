package monash.edu.hally.nlp;

import java.util.Comparator;
import java.util.Map;

public class TopSimilarityComparable implements Comparator<String> {
	
	private Map<String,Double> hashtag2ScoreMap;

	public TopSimilarityComparable(Map<String,Double> hashtag2ScoreMap)
	{
		this.hashtag2ScoreMap=hashtag2ScoreMap;
	}

	@Override
	public int compare(String o1, String o2) {
		if(hashtag2ScoreMap.get(o1)>hashtag2ScoreMap.get(o2)) return -1;
		if(hashtag2ScoreMap.get(o1)<hashtag2ScoreMap.get(o2)) return 1;
		return 0;
	}	

}
