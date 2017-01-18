package monash.edu.hally.model;

import java.util.Map.Entry;

import monash.edu.hally.global.ModelParameters;
import monash.edu.hally.global.ModelVariables;
import monash.edu.hally.index.Document;
import monash.edu.hally.index.Documents;
import monash.edu.hally.nlp.FilesUtil;

public class TUSLDAModel{

	private double alpha,gama,lambda,eta,tao;	//����(dirichlet�ֲ������Ĳ���)
	private double [][][]beta;	//***������Ϣ***ÿһ��Ϊ�����s,����k�ʹ�term��Ӧ��dirichlet����   ά�� S*K*V
	private double [][]betaSum;	//***������Ϣ***ÿһ��Ϊ�����s,����k�ʹ�term��Ӧ��dirichlet���κ�   ά�� S*K
	private int []termSenti;	//***������Ϣ***ÿһ��Ϊ����term����б�ǩ   ά�� V
	private int M,K,V,U,T,S;	//�ֱ��ʾ�ĵ������ĵ��ĸ���������������ֵ��еĴʻ����
	
//	private double[] phiB;	//�����ʷֲ�  ά��V
	private double[][][] phi;	//{���-����}���µĴʻ�ֲ�  ά�� S*K*V
	private double[][][] theta;	//{ʱ��-���}���µ�����ֲ�  ά�� T*S*K
	private double[][][] delta;	//{�û�-���}���µ�����ֲ�  ά�� U*S*K
	private double[][] pi; //�ض��ĵ��µ���зֲ�  ά��M*S

	private int[] ny;	//ÿһ��Ϊ��ѡ���ض������������ɵĴ���   ά�� 2
//	private int[] nx;	//ÿһ��Ϊ��ѡ���ض��ʻ��������ɵĴ���   ά�� 2
//	private int[] nb;	//ÿһ��Ϊ���ض������ʳ��ֵĴ���   ά�� V
//	private int nbSum;	//�����ʳ��ֵĴ�����
	private int[][] nms;	//ÿһ��Ϊ���ض��ĵ��µ��ض���г��ֵĴ���   ά�� M*S
	private int[] nmsSum;	//ÿһ��Ϊ���ض��ĵ��µ�������г��ֵĴ�����
	private int[][][] nskv;	//ÿһ��Ϊ��{���-����}���µ��ض��ʻ���ֵĴ���   ά�� S*K*V
	private int[][] nskvSum;	//ÿһ��Ϊ��{���-����}���µ����дʻ���ֵĴ�����
	private int[][][] ntsk;	//ÿһ��Ϊ��{ʱ��-���}���µ��ض�������ֵĴ���   ά�� T*S*K
	private int[][] ntskSum;	//ÿһ��Ϊ��{ʱ��-���}���µ�����������ֵĴ�����
	private int[][][] nusk;	//ÿһ��Ϊ��{�û�-���}���µ��ض�������ֵĴ���   ά�� U*S*K
	private int[][] nuskSum;	//ÿһ��Ϊ��{�û�-���}���µ�����������ֵĴ�����
	
	
	private int[] z;		//ÿһ��Ϊ���ض��ĵ��µ�����
	private int[] y;		//ÿһ��Ϊ���ض��ĵ��µ�����
	private int[][] s;		//ÿһ��Ϊ���ض��ĵ��ض����µ���б�ǩ  ά�� M*Nm
	private int[] s_d;		//ÿһ��Ϊ���ض��ĵ�����б�ǩ  ά�� M
//	private int[][] x;		//ÿһ��Ϊ���ض��ĵ��µ�ѡ��ʻ�ı���
	
	private int iterations;	//��������
	private int burn_in;	//burn-in ʱ��
	private int saveStep;	//burn-in ����ÿsaveStep�ε�������һ�ν��
	private int topNum;	//��ʾ�����¸�����ߵ�ǰtopNum����
	
	private Documents documents;	//�ĵ���
	
//	private int saveTime=0;	//���������������
	
	
	
	public TUSLDAModel(Documents documents)
	{
		this.documents=documents;
		setModelParameters();
	}
	
	/**
	 * ���ã�������ҪԤ��ָ���Ĳ���
	 */
	private void setModelParameters()
	{
		System.out.println("Read model parameters.");//��һ�ֶ�ȡ�����ķ�ʽ
		ModelParameters modelParameters=FilesUtil.readParametersFile();
	
		K=modelParameters.getK();
		alpha=0.1;	//һ��Ϊ 50/K
		tao=0.1;	//һ��Ϊ 0.1
		gama=0.1;
		lambda=eta=0.1;
		iterations=modelParameters.getIterations();
		burn_in=modelParameters.getBurn_in();
		saveStep=modelParameters.getSaveStep();
		topNum=modelParameters.getTopNum();
	}
	
	
	/**
	 * ���ã���ʼ��ģ�ͱ�������
	 */
	private void allocateMemoryForVariables()
	{
		M=documents.docs.size();
		V=ModelVariables.g_termDictionary.size();
		U=ModelVariables.g_usersSet.size();
		T=ModelVariables.g_timesSet.size();
		S=3; //�������
		
		phi=new double[S][K][V];
		theta=new double[T][S][K];
		delta=new double[U][S][K];
		pi=new double[M][S];
//		phiB=new double[V];
		
		ny=new int[2];
//		nx=new int[2];
//		nb=new int[V];
		nms=new int[M][S];
		nmsSum=new int[M];
		nskv=new int[S][K][V];
		nskvSum=new int[S][K];
		ntsk=new int[T][S][K];
		ntskSum=new int[T][S];
		nusk=new int[U][S][K];
		nuskSum=new int[U][S];
		
		z=new int[M];
		y=new int[M];
		s_d=new int[M];
		s=new int[M][];
//		x=new int[M][];
	}
	
	/**
	 * ���ã������ֵ��ȡ������Ϣ
	 */
	private void setPrior()
	{
		IntegratePrior integratePrior = new IntegratePrior(S, K, V);
		integratePrior.setSentiPrior();
//		integratePrior.print();
		beta=integratePrior.getBeta();
		betaSum=integratePrior.getBetaSum();
		termSenti=integratePrior.getTermSenti();
	}
	
	
	/**
	 * ���ã���ʼ��ģ��
	 * 1.��ʼ��ģ�Ͳ�����������Ҫѧϰ���ĵ����õ���
	 * 2.���ĵ��еĴʻ������������
	 */
	public void initialiseModel()
	{
		allocateMemoryForVariables();
		setPrior();
		System.out.println("Model begins learning.");
		
		for (int m = 0; m < M; m++) {
			
			int ym=(int) (Math.random()*(2));	//���
			y[m]=ym;
			int zm=(int) (Math.random()*(K));	//�����������
			z[m]=zm;
			int sm=(int) (Math.random()*(S));	//����������
			s_d[m]=sm;	
			nms[m][sm]++;
			nmsSum[m]++;
						
			Document document=documents.docs.get(m);				
			int Nm=document.docWords.length;	//��mƪ�ĵ��Ĵ��������ȣ�
			s[m]=new int[Nm];
			if(ym==0){
				ny[0]++;
				nusk[document.userIndex][sm][zm]++;
				nuskSum[document.userIndex][sm]++;
			}
			else{
				ny[1]++;
				ntsk[document.timeIndex][sm][zm]++;
				ntskSum[document.timeIndex][sm]++;
			}
			
			for (int n = 0; n < Nm; n++) {	
				
				int smn;
				if(termSenti[document.docWords[n]]!=-1)
					smn = termSenti[document.docWords[n]];
				else
					smn=(int) (Math.random()*(S));
				s[m][n]=smn;
						
				nms[m][smn]++;	
				nmsSum[m]++;
				nskv[smn][zm][document.docWords[n]]++;
				nskvSum[smn][zm]++;
				
			}		
		}
	}
	
	public void initialiseModel2(boolean isUser)
	{
		allocateMemoryForVariables();
		setPrior();
		System.out.println("Model begins learning.");
		
		for (int m = 0; m < M; m++) {
			
			int zm=(int) (Math.random()*(K));	//�����������
			z[m]=zm;
			int sm=(int) (Math.random()*(S));	//����������
			s_d[m]=sm;	
			nms[m][sm]++;
			nmsSum[m]++;
						
			Document document=documents.docs.get(m);				
			int Nm=document.docWords.length;	//��mƪ�ĵ��Ĵ��������ȣ�
			s[m]=new int[Nm];
			if(isUser){
				nusk[document.userIndex][sm][zm]++;
				nuskSum[document.userIndex][sm]++;
			}
			else{
				ntsk[document.timeIndex][sm][zm]++;
				ntskSum[document.timeIndex][sm]++;
			}
			
			for (int n = 0; n < Nm; n++) {	
				
				int smn;
				if(termSenti[document.docWords[n]]!=-1)
					smn = termSenti[document.docWords[n]];
				else
					smn=(int) (Math.random()*(S));
				s[m][n]=smn;
						
				nms[m][smn]++;	
				nmsSum[m]++;
				nskv[smn][zm][document.docWords[n]]++;
				nskvSum[smn][zm]++;
				
			}		
		}
	}
	
	/**
	 * ���ã�����Gibbs�����㷨�����ƶ�ģ�Ͳ���
	 */
	public void inferenceModel()
	{
		
		for (int currentIteration = 1; currentIteration <= iterations; currentIteration++) {
			System.out.println("Iteration "+currentIteration);
			if(currentIteration == iterations)
				saveLatentVariables();
			else if((currentIteration >= burn_in) && (currentIteration % saveStep==0))
				calLatentVariables(false);
			else
			{	//��ͣ�Ĳ�����ֱ������burn-inʱ��
				for (int m = 0; m < M; m++) {
					Document document=documents.docs.get(m);
//					sampleForDoc2(m, false);
					sampleForDoc(m);
					for (int n = 0; n < document.docWords.length; n++) {
						sampleForWord(m,n);
					}
				}
			}
		}
		System.out.println("Learn over!");
	}
	
	/**
	 * Gibbs�����㷨������ǰ�ʻ����·����µ�����
	 * @return �µ�����
	 */
	
	private double calCondiTionalPro(Document document, int s, int k)
	{
		double p1=1;
  		for (Entry<Integer, Integer> entry : document.indexToCountMap.entrySet()) {
  			if(entry.getValue()==1)
  				p1 = p1 *(nskv[s][k][entry.getKey()]+beta[s][k][entry.getKey()]);
  			else{
  				for (int i = 0; i < entry.getValue()-1; i++) {
  					p1 = p1 * (nskv[s][k][entry.getKey()]+i+beta[s][k][entry.getKey()]);
  				}
  			}
			
		}
  		double p2=1;
  		for (int i = 0; i < document.docWords.length-1; i++) {
			p2 = p2 * (betaSum[s][k]+nskvSum[s][k]+i);
		}
  		
  		return p1/p2;
	}
	
	private void sampleForDoc2(int m, boolean isUser)
	{
		Document document=documents.docs.get(m);
		int oldTopic=z[m];
		int Nm=document.docWords.length;	//��mƪ�ĵ��Ĵ��������ȣ�		
		for (int n = 0; n < Nm; n++) {
			nskv[s[m][n]][oldTopic][document.docWords[n]]--;
			nskvSum[s[m][n]][oldTopic]--;
		}

		int oldSentiment=s_d[m];
		nms[m][oldSentiment]--;
		nmsSum[m]--;
		
		if(isUser){
			nusk[document.userIndex][oldSentiment][oldTopic]--;
			nuskSum[document.userIndex][oldSentiment]--;
		}
		else{
			ntsk[document.timeIndex][oldSentiment][oldTopic]--;
			ntskSum[document.timeIndex][oldSentiment]--;
		}
				
		double[][] p=new double[S][K];
				
		for (int s = 0; s < S; s++) {
	  	    for (int k = 0; k < K; k++) {
	  	    	double pro=calCondiTionalPro(document,s,k); 
	  	    	if(isUser){
	  	    		p[s][k] = (nms[m][s]+lambda)/(nmsSum[m]+S*lambda)*
	 						(nusk[document.userIndex][s][k]+alpha)/
	 						(nuskSum[document.userIndex][s]+K*alpha)*pro;
	  	    	}
	  	    	else{
	  	    		p[s][k] = (nms[m][s]+lambda)/(nmsSum[m]+S*lambda)*
	 						(ntsk[document.timeIndex][s][k]+alpha)/
	 						(ntskSum[document.timeIndex][s]+K*alpha)*pro;
	  	    	}  	    	
			}
		}
	
		// accumulate multinomial parameters
		for (int s = 0; s < S; s++) {
			for (int k = 0; k < K; k++) {
				if (k==0) {
				    if (s==0) continue;
			        else p[s][k] += p[s-1][K-1];
				}
				else p[s][k] += p[s][k-1];
		    }
		}
		// probability normalization
		double u = Math.random()* p[S-1][K-1];
		int newSentiment = 0,newTopic = 0;
		
		boolean loopBreak=false;
		for (int s = 0; s < S; s++) {
			for (int k = 0; k < K; k++) {
			    if (p[s][k] > u) {
			    	newSentiment=s;
			    	newTopic=k;
			    	loopBreak=true;
			    	break;
			    }
			}
			if (loopBreak == true) {
				break;
			}
		}
		
		
		for (int n = 0; n < Nm; n++) {
			nskv[s[m][n]][newTopic][document.docWords[n]]++;
			nskvSum[s[m][n]][newTopic]++;
		}

		s_d[m]=newSentiment;
		z[m]=newTopic;

		nms[m][newSentiment]++;
		nmsSum[m]++;
		if(isUser){
			nusk[document.userIndex][newSentiment][newTopic]++;
			nuskSum[document.userIndex][newSentiment]++;

		}
		else{
			ntsk[document.timeIndex][newSentiment][newTopic]++;
			ntskSum[document.timeIndex][newSentiment]++;

		}
	}
	
	/**
	 * ���ã�����ǰ�ĵ�m����
	 * @param m: �ĵ�����
	 */
	private void sampleForDoc(int m)
	{
		Document document=documents.docs.get(m);
		int oldTopic=z[m];
		int Nm=document.docWords.length;	//��mƪ�ĵ��Ĵ��������ȣ�		
		for (int n = 0; n < Nm; n++) {
			nskv[s[m][n]][oldTopic][document.docWords[n]]--;
			nskvSum[s[m][n]][oldTopic]--;
		}

		int oldSentiment=s_d[m];
		nms[m][oldSentiment]--;
		nmsSum[m]--;
		if(y[m]==0){
			nusk[document.userIndex][oldSentiment][oldTopic]--;
			nuskSum[document.userIndex][oldSentiment]--;
			ny[0]--;
		}
		else{
			ntsk[document.timeIndex][oldSentiment][oldTopic]--;
			ntskSum[document.timeIndex][oldSentiment]--;
			ny[1]--;
		}
		
		double[][][] p=new double[2][S][K];
		
		for (int y = 0; y < 2; y++) {
			for (int s = 0; s < S; s++) {
		  	    for (int k = 0; k < K; k++) {
		  	    	double pro=calCondiTionalPro(document,s,k);  
		  	    	if(y==0){
		  	    		p[y][s][k] = (ny[y]+gama)/(ny[0]+ny[1]+2*gama)*
		 						(nms[m][s]+lambda)/(nmsSum[m]+S*lambda)*
		 						(nusk[document.userIndex][s][k]+alpha)/
		 						(nuskSum[document.userIndex][s]+K*alpha)*pro;
		  	    	}
		  	    	else{
		  	    		p[y][s][k] = (ny[y]+gama)/(ny[0]+ny[1]+2*gama)*
		 						(nms[m][s]+lambda)/(nmsSum[m]+S*lambda)*
		 						(ntsk[document.timeIndex][s][k]+alpha)/
		 						(ntskSum[document.timeIndex][s]+K*alpha)*pro;
		  	    	}
				}
			}
		}
		
		// accumulate multinomial parameters
		for (int y = 0; y < 2; y++) {
			for (int s = 0; s < S; s++) {
				for (int k = 0; k < K; k++) {
					if(y==0){
						if (k==0) {
						    if (s==0) continue;
					        else p[y][s][k] += p[y][s-1][K-1];
						}
						else p[y][s][k] += p[y][s][k-1];
					}
					else {
						if (k==0) {
						    if (s==0) 	p[y][0][0] += p[y-1][S-1][K-1];
					        else p[y][s][k] += p[y][s-1][K-1];
						}
						else p[y][s][k] += p[y][s][k-1];
					}
			    }
			}
		}
		// probability normalization
		double u = Math.random()* p[1][S-1][K-1];
		int newSentiment = 0,newTopic = 0,newY=0;
		boolean loopBreak=false;
		for (int y = 0; y < 2; y++) {
			for (int s = 0; s < S; s++) {
				for (int k = 0; k < K; k++) {
				    if (p[y][s][k] > u) {
				    	newY=y;
				    	newSentiment=s;
				    	newTopic=k;
				    	loopBreak=true;
				    	break;
				    }
				}
				if(loopBreak==true)
					break;
			}
			if(loopBreak==true)
				break;
		}
		
		for (int n = 0; n < Nm; n++) {
			nskv[s[m][n]][newTopic][document.docWords[n]]++;
			nskvSum[s[m][n]][newTopic]++;
		}

		s_d[m]=newSentiment;
		z[m]=newTopic;
		y[m]=newY;
		nms[m][newSentiment]++;
		nmsSum[m]++;
		if(y[m]==0){
			nusk[document.userIndex][newSentiment][newTopic]++;
			nuskSum[document.userIndex][newSentiment]++;
			ny[0]++;
		}
		else{
			ntsk[document.timeIndex][newSentiment][newTopic]++;
			ntskSum[document.timeIndex][newSentiment]++;
			ny[1]++;
		}
		
	}
	
	/**
	 * ���ã�����ǰ�ʲ���
	 */
	private void sampleForWord(int m, int n)
	{

		int termIndex=documents.docs.get(m).docWords[n];
		
		int oldSentiment=s[m][n];
		nms[m][oldSentiment]--;
		nskv[oldSentiment][z[m]][termIndex]--;
		nskvSum[oldSentiment][z[m]]--;
		nmsSum[m]--;
				
		
		double[] p=new double[S];
		
//		for (int s = 0; s < S; s++) {		
//			double prior=1;
//			if(termSenti[termIndex]!=-1)
//				prior=(termSenti[termIndex]==s)?0.8:0.2;
//			
//			p[s]=((nms[m][s]+lambda)/(nmsSum[m]+S*lambda))*
//					((nskv[s][z[m]][termIndex]+beta[s][z[m]][termIndex])/
//					(nskvSum[s][z[m]]+betaSum[s][z[m]]))*prior;
//			
//		}
		
		for (int s = 0; s < S; s++) {					
			p[s]=((nms[m][s]+lambda)/(nmsSum[m]+S*lambda))*
					((nskv[s][z[m]][termIndex]+beta[s][z[m]][termIndex])/
					(nskvSum[s][z[m]]+betaSum[s][z[m]]));
		}
		
		//ģ������µ�����
		//���ڸ�������ĸ����Ѿ����ɣ����������̸��ʵķ�ʽ,�ж����������ĸ������䣬�������������(0,p[1]),��ôK=1.
		for (int s= 1; s < S; s++) {
			p[s]+=p[s-1];
		}
		double u= Math.random()*p[S-1];
		int newSenti = 0;
		for (int s = 0; s < S; s++) {
			if(p[s]>u){
				newSenti=s;
				break;
			}
		}
		
		s[m][n]=newSenti;
		nskv[newSenti][z[m]][termIndex]++;
		nskvSum[newSenti][z[m]]++;
		nms[m][newSenti]++;
		nmsSum[m]++;
	}
	
	/**
	 * ���ã����ݼ�������������ģ�ͱ���
	 * @param isFinalIteration �Ƿ������һ�ε���������Ǿ�Ҫ��ǰ�漸�α���Ľ����ƽ��
	 */
	private void calLatentVariables(boolean isFinalIteration)
	{
		// �����ĵ���зֲ�
		for (int m = 0; m < M; m++) {
			for (int s = 0; s < S; s++) {
				pi[m][s] += (nms[m][s]+lambda)/(nmsSum[m]+S*lambda);
				if(isFinalIteration)
					pi[m][s] = pi[m][s] / ((iterations-burn_in) / saveStep + 1); //saveTime;
			}
		}
		
		// ����{���-����}���µĴʻ�ֲ�
		for (int s = 0; s < S; s++) {
			for (int k = 0; k < K; k++) {
				for (int v = 0; v < V; v++) {
					phi[s][k][v] += (nskv[s][k][v]+beta[s][k][v])/(nskvSum[s][k]+betaSum[s][k]);
					if(isFinalIteration)
						phi[s][k][v] = phi[s][k][v] / ((iterations-burn_in) / saveStep + 1);//saveTime;
				}
			}
		}
		
		// ����{�û�-���}���µ�����ֲ�
		for (int u = 0; u < U; u++) {
			for (int s = 0; s < S; s++) {
				for (int k = 0; k < K; k++) {
					delta[u][s][k] += (nusk[u][s][k]+alpha)/(nuskSum[u][s]+K*alpha);
					if(isFinalIteration)
						delta[u][s][k] = delta[u][s][k] / ((iterations-burn_in) / saveStep + 1);//saveTime;
				}
			}
		}
		
		// ����{ʱ��-���}���µ�����ֲ�
		for (int t = 0; t < T; t++) {
			for (int s = 0; s < S; s++) {
				for (int k = 0; k < K; k++) {
					theta[t][s][k] += (ntsk[t][s][k]+alpha)/(ntskSum[t][s]+K*alpha);
					if(isFinalIteration)
						theta[t][s][k] = theta[t][s][k] / ((iterations-burn_in) / saveStep + 1);//saveTime;
				}
			}
		}
		
	}
	
	/**
	 * ���ã����浱ǰ��������ѧϰ����ģ�ͱ���
	 * @param currentIterition�� ��ǰ��������
	 */
	private void saveLatentVariables()
	{
		System.out.println("Save results at iteration ("+iterations+").");
		calLatentVariables(true);
		FilesUtil.saveSentiDist(pi);
		FilesUtil.saveSentiPrecision(pi, documents);
//		FilesUtil.saveCount(nms);
		FilesUtil.saveDistributions(delta, theta,null);
//		FilesUtil.saveTopicAssignment(documents, z);
		FilesUtil.saveTopTopicWords(phi, topNum);
	}
	
}
