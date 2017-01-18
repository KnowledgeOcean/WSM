package monash.edu.hally.model;

import java.io.File;
import java.util.ArrayList;

import monash.edu.hally.constant.ModelConstants;
import monash.edu.hally.global.ModelVariables;
import monash.edu.hally.nlp.FilesUtil;

public class IntegratePrior {
	
	private double [][][]beta;	//***������Ϣ***ÿһ��Ϊ�����s,����k�ʹ�term��Ӧ��dirichlet����   ά�� S*K*V
	private double [][]omega;	//***������Ϣ***ÿһ��Ϊ�����s�ʹ�term��Ӧ��dirichlet����   ά�� S*V
	private double [][]betaSum;	//***������Ϣ***ÿһ��Ϊ�����s,����k�ʹ�term��Ӧ��dirichlet���κ�   ά�� S*K
	
	private int []termSenti;	//***������Ϣ***ÿһ��Ϊ����term����б�ǩ   ά�� V
	
	private int S,K,V;
	
	private boolean isExitDictionary=true;
	
	int size=0;
	
	public IntegratePrior(int S, int K, int V)
	{
		this.S=S;
		this.K=K;
		this.V=V;
		setDefaultPrior();
	}
		
	private void setDefaultPrior()
	{
		beta=new double[S][K][V];
		betaSum=new double[S][K];
		for (int s = 0; s < S; s++) {
			for (int k = 0; k < K; k++) {
				for (int v = 0; v < V; v++) {
					beta[s][k][v] = 0.01;
					betaSum[s][k] += beta[s][k][v];
				}
			}
		}
		omega=new double[S][V];
		for (int s = 0; s < S; s++) {	
			for (int v = 0; v < V; v++) {
				omega[s][v]=1;
			}	
		}
		termSenti=new int[V];
		for (int v = 0; v < V; v++) {
			termSenti[v]=-1;
		}	
	}
	
	/**
	 * ����������ֵ䣬�൱��ʹ��Ĭ���������ֵ
	 */
	public void setSentiPrior()
	{
		integration();
		if(isExitDictionary){
			for (int s = 0; s < S; s++) {
				for (int k = 0; k < K; k++) {
					betaSum[s][k]=0;
					for (int v = 0; v < V; v++) {
						beta[s][k][v] = beta[s][k][v] * omega[s][v];
						betaSum[s][k] += beta[s][k][v];
					}
				}
			}
		}
	}
	
	
	//sentiItems���飺term 0, neutral 1, positive 2, negative 3
	//termSenti������0��ʾ���ԣ�1��ʾ������2��ʾ����
	private void setTermSentiPrior(String sentiItems[], int termIndex)
	{
//		System.out.println(++size);
		if(Double.valueOf(sentiItems[1])>0.8){
			omega[1][termIndex]=0;
			omega[2][termIndex]=0;
			termSenti[termIndex]=0;	
		}
		else if(Double.valueOf(sentiItems[2])>0.8){
			omega[0][termIndex]=0;
			omega[2][termIndex]=0;
			termSenti[termIndex]=1;	
		}
		else if(Double.valueOf(sentiItems[3])>0.8){
			omega[0][termIndex]=0;
			omega[1][termIndex]=0;
			termSenti[termIndex]=2;	
		}
	}
	
	/**
	 * �ں������Ϣ
	 */
	private void integration()
	{
		if(!new File(ModelConstants.SENTIMENT_DICTIONARY).exists()){
			System.err.println("You don't set sentiment dictionary.");
			isExitDictionary=false;
			return;
		}
		//����д��ֵ�
		ArrayList<String> sentiDictionary=FilesUtil.readDocument(
				ModelConstants.SENTIMENT_DICTIONARY);
		//�������ֵ�
		ArrayList<String> emoticonDictionary=FilesUtil.readDocument(
				ModelConstants.SENTIMENT_EMOTICONS);
		
		sentiDictionary.addAll(emoticonDictionary);
		
		for (int i=0; i<ModelVariables.g_termDictionary.size(); i++) {
			
			String term=ModelVariables.g_termDictionary.get(i);
			for (String senti : sentiDictionary) {
				String sentiItems[]=senti.split("\t");
				if(term.equals(sentiItems[0])){
					setTermSentiPrior(sentiItems, i);
				}	
			}
		}
	}
	
	public double[][][] getBeta() {
		return beta;
	}
	public double[][] getOmega() {
		return omega;
	}
	public double[][] getBetaSum() {
		return betaSum;
	}
	public int[] getTermSenti() {
		return termSenti;
	}
	
	public void print()
	{
		for (int v = 0; v < V; v++) {
			System.out.print(termSenti[v]+"\t");
			System.out.print(ModelVariables.g_termDictionary.get(v)+"\t");
			
			for (int s = 0; s < S; s++) {
				System.out.print(beta[s][0][v]+"\t");
			}
			System.out.println();
		}
	}

}
