package monash.edu.hally.main;

import monash.edu.hally.index.Documents;
import monash.edu.hally.model.TSLDAModel;
import monash.edu.hally.model.TUSLDAModel;
import monash.edu.hally.nlp.FilesUtil;

public class Main {
	
	public void start()
	{
		long startTime=System.currentTimeMillis();
		//索引化所有语料库中所有文档
		Documents docs=new Documents();
		docs.indexAllDocuments();
		docs.extendRelatedHashtag(); //学习关联的hashtag
		
		
		//训练模型
		TSLDAModel tldaModel=new TSLDAModel(docs);
		tldaModel.initialiseModel();
		//推断和保存模型的潜在变量
		tldaModel.inferenceModel();
		
		
		long endTime=System.currentTimeMillis();
		System.out.println("Runtime "+(endTime-startTime)/1000+"s.");
		
		FilesUtil.printSuccessMessage();
	}

	public static void main(String[] args) {
		
		new Main().start();
	}

}
