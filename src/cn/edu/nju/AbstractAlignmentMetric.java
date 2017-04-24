package cn.edu.nju;


/**
 * Abstract class for metrics that evaluate alignment quality. 
 * */
public abstract class AbstractAlignmentMetric {
	
	public abstract void addSentence(AlignmentTable result, AlignmentTable reference);
	
	public abstract double computeScore();
	
	public abstract String printScores();
	
	public AbstractAlignmentMetric initialMetric()//how to use this format
	{
		if(this.getClass().equals(AER.class))
		{
			return new AER();
		}
		else
			return null;
	}
	
	public String computeScoreOfFile(String fileResult, String fileReference, String src, String trg)
	{
		return computeScoreOfFile(fileResult, fileReference, src, trg, null)[0];
	}

	public String computeScoreOfFile(String fileResult, String fileReference)
	{
		return computeScoreOfFile(fileResult, fileReference, null, null, null)[0];
	}
	
	public String[] computeScoreOfFile(String fileResult, String fileReference, int[] separateIndexes)
	{
		return computeScoreOfFile(fileResult, fileReference, null, null, separateIndexes);
	}
	public String[] computeScoreOfFile(String fileResult, String fileReference, String src, String trg, int[] separateIndexes)
	{
		String[] result;
		AbstractAlignmentMetric[] metricList;
		if(separateIndexes == null)//in my program,this command run once time.
		{
			result = new String[1];
			metricList = new AbstractAlignmentMetric[1];
			metricList[0] = initialMetric();
		}
		else//unused
		{
			result = new String[separateIndexes.length+1];
			metricList = new AbstractAlignmentMetric[separateIndexes.length+1];
			for(int i=0;i<separateIndexes.length+1;i++)
				metricList[i] = initialMetric();
		}

		RandomFileReader rfr = null;
		//if the src and trg files are not provided, the default length of src and trg sentence will be set to 200.
		if(src!=null)
			rfr = new RandomFileReader(new String[]{fileResult, fileReference, src, trg});
		else
			rfr = new RandomFileReader(new String[]{fileResult, fileReference});//in my program,this command run once time.
		int count = 0;
		int currentPart = 0;
		while(rfr.hasNext())//read by line
		{
			count++;//count is the count of line
			if(separateIndexes!=null&&currentPart<separateIndexes.length&&count>separateIndexes[currentPart])
			{
				currentPart++;//unused
			}
			String[] fields = rfr.readNext();
			
			//default sentence length (won't affect the score, unless the actual length is larger than default)
			int srcLength = 200, trgLength = 200;
			if(fields.length==4)
			{
				srcLength = fields[2].split(" ").length;
				trgLength = fields[3].split(" ").length;
			}
			AlignmentTable atRef = new AlignmentTable(srcLength,trgLength);
			atRef.FillMatrix_Aligntable(fields[1]);
			AlignmentTable atRes = new AlignmentTable(srcLength,trgLength);
			atRes.FillMatrix_Aligntable(fields[0]);
			metricList[currentPart].addSentence(atRes,atRef);
		}
		for(int i = 0;i<metricList.length&&i<=currentPart;i++)
		{//until now,metricList.length=1,currentPart=0
			metricList[i].computeScore();
			result[i] = metricList[i].printScores();
		}
		return result;
	}
}
