package monash.edu.hally.main;

import monash.edu.hally.index.Documents;
import monash.edu.hally.model.TSLDAModel;
import monash.edu.hally.model.TUSLDAModel;
import monash.edu.hally.nlp.FilesUtil;

public class Main {
	
	public void start()
	{
		long startTime=System.currentTimeMillis();
		//�������������Ͽ��������ĵ�
		Documents docs=new Documents();
		docs.indexAllDocuments();
		docs.extendRelatedHashtag(); //ѧϰ������hashtag
		
		
		//ѵ��ģ��
		TSLDAModel tldaModel=new TSLDAModel(docs);
		tldaModel.initialiseModel();
		//�ƶϺͱ���ģ�͵�Ǳ�ڱ���
		tldaModel.inferenceModel();
		
		
		long endTime=System.currentTimeMillis();
		System.out.println("Runtime "+(endTime-startTime)/1000+"s.");
		
		FilesUtil.printSuccessMessage();
	}

	public static void main(String[] args) {
		
		new Main().start();
	}

}
