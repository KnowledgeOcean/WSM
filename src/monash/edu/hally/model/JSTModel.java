package monash.edu.hally.model;

import monash.edu.hally.global.ModelParameters;
import monash.edu.hally.global.ModelVariables;
import monash.edu.hally.index.Document;
import monash.edu.hally.index.Documents;
import monash.edu.hally.nlp.FilesUtil;

public class JSTModel{

	private double alpha,lambda;	//����(dirichlet�ֲ������Ĳ���)
	private double [][][]beta;	//***������Ϣ***ÿһ��Ϊ�����s,����k�ʹ�term��Ӧ��dirichlet����   ά�� S*K*V
	private double [][]betaSum;	//***������Ϣ***ÿһ��Ϊ�����s,����k�ʹ�term��Ӧ��dirichlet���κ�   ά�� S*K
	private int []termSenti;	//***������Ϣ***ÿһ��Ϊ����term����б�ǩ   ά�� V
	private int M,K,V,S;	//�ֱ��ʾ�ĵ������ĵ��ĸ���������������ֵ��еĴʻ����
	private double []gama;
	private double[][][] phi;	//{���-����}���µĴʻ�ֲ�  ά�� S*K*V

	private double[][] pi; //�ض��ĵ��µ���зֲ�  ά��M*S

	private int[][] nms;	//ÿһ��Ϊ���ض��ĵ��µ��ض���г��ֵĴ���   ά�� M*S
	private int[] nmsSum;	//ÿһ��Ϊ���ض��ĵ��µ�������г��ֵĴ�����
	private int[][][] nskv;	//ÿһ��Ϊ��{���-����}���µ��ض��ʻ���ֵĴ���   ά�� S*K*V
	private int[][] nskvSum;	//ÿһ��Ϊ��{���-����}���µ����дʻ���ֵĴ�����
	
	private int[][][] nmsk;	//ÿһ��Ϊ��{ʱ��-���}���µ��ض�������ֵĴ���   ά�� T*S*K
	private int[][] nmskSum;	//ÿһ��Ϊ��{ʱ��-���}���µ�����������ֵĴ�����
	
	private int[][] s;		//ÿһ��Ϊ���ض��ĵ��ض����µ���б�ǩ  ά�� M*Nm
	private int[][] z;		//ÿһ��Ϊ���ض��ĵ��µ�ѡ��ʻ�ı���
	
	private int iterations;	//��������
	private int burn_in;	//burn-in ʱ��
	private int saveStep;	//burn-in ����ÿsaveStep�ε�������һ�ν��
	private int topNum;	//��ʾ�����¸�����ߵ�ǰtopNum����
	
	private Documents documents;	//�ĵ���
	
//	private int saveTime=0;	//���������������
	
	
	
	public JSTModel(Documents documents)
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
		S=3; //�������
		
		phi=new double[S][K][V];
		pi=new double[M][S];
		gama=new double[S];
		gama[0]=gama[1]=0.01;
		gama[2]=0.011;
		
		nmsk=new int[M][S][K];
		nmskSum=new int[M][S];
		
		nms=new int[M][S];
		nmsSum=new int[M];
		nskv=new int[S][K][V];
		nskvSum=new int[S][K];
		
		s=new int[M][];
		z=new int[M][];
	}
	
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
						
			Document document=documents.docs.get(m);				
			int Nm=document.docWords.length;	//��mƪ�ĵ��Ĵ��������ȣ�
		
			s[m]=new int[Nm];
			z[m]=new int[Nm];
			for (int n = 0; n < Nm; n++) {
				int smn;
				
				if(termSenti[document.docWords[n]]!=-1)
					smn = termSenti[document.docWords[n]];
				else
					smn=(int) (Math.random()*(S));
				
				int zmn=(int) (Math.random()*(K));	//�����������
				z[m][n]=zmn;
				s[m][n]=smn;
				nms[m][smn]++;
				nmsSum[m]++;
				nmsk[m][smn][zmn]++;
				nmskSum[m][smn]++;
				nskv[smn][zmn][document.docWords[n]]++;
				nskvSum[smn][zmn]++;
				
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
//					sampleForDoc(m);
					for (int n = 0; n < document.docWords.length; n++) {
						sampleForWord(m,n);
					}
				}
			}
		}
		System.out.println("Learn over!");
	}
	
	
	private void sampleForWord(int m, int n)
	{

		int termIndex=documents.docs.get(m).docWords[n];
		
		int oldSentiment=s[m][n];
		int oldTopic=z[m][n];
		nms[m][oldSentiment]--;
		nmsSum[m]--;
		nskv[oldSentiment][oldTopic][termIndex]--;
		nskvSum[oldSentiment][oldTopic]--;
		nmsk[m][oldSentiment][oldTopic]--;
		nmskSum[m][oldSentiment]--;
		
		double[][] p=new double[S][K];
		for (int s = 0; s < S; s++) {
	  	    for (int k = 0; k < K; k++) {	
  	    		p[s][k] = (nmsk[m][s][k]+gama[s])/(nmskSum[m][s]+K*0.01)*
 						(nms[m][s]+lambda)/(nmsSum[m]+S*lambda)*
 						(nskv[s][k][termIndex]+beta[s][k][termIndex])/(nskvSum[s][k]+betaSum[s][k]);
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
		z[m][n]=newTopic;
		s[m][n]=newSentiment;
		nskv[newSentiment][newTopic][termIndex]++;
		nskvSum[newSentiment][newTopic]++;
		nms[m][newSentiment]++;
		nmsSum[m]++;
		nmsk[m][newSentiment][newTopic]++;
		nmskSum[m][newSentiment]++;
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
		FilesUtil.saveCount(nms);
		FilesUtil.saveTopTopicWords(phi, topNum);
	}
	
}
