package monash.edu.hally.nlp;

import java.util.ArrayList;

public class Test {

	public static void main(String[] args) {


		ArrayList<String> content = FilesUtil.readDocument("tweet2011_fre3_len3_wlen3_queryword_m_hashtag.data");
		
		FilesUtil.saveFile(content, "tweet2011_fre3_len3_queryword_hashtag_only_text.txt");
		
		
		
		
	}

}
