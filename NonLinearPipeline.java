import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class NonLinearPipeline {

	static int numberOfResources;
	static int numberOfTimeSlices;
	static int[][] initialReservationTable;
	static ArrayList<Integer> initialForbiddenLatencies;
	static ArrayList<Integer> initialAllowedTransitions;
	static ArrayList<Integer> initialCollisionVector;
	private static int initialCollisionVectorSize;
	static ArrayList<ArrayList<Integer>> initialSimpleCycleArray;
	static ArrayList<Integer> initialGreedyCycle;
	static boolean collisionExists = false;
	static float initialMAL;
	static float currentMAL;
    
	public static StringBuilder outputFile = new StringBuilder();

	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub
		initialMAL=currentMAL=(float) 0.0;
		int[] inputArray = getArrayOfElementsFromInputFile("input.txt");
		if(inputArray==null) return;

		System.out.println("\n--------------------------------------------\n");
		System.out.print("\nInput String: ");
    	for (int i=0; i<inputArray.length;i++) {
    		System.out.print(inputArray[i]);
    	}

    	initialReservationTable = getStagesAndCorrespondingTimeSlicesUsedFromInputArray(inputArray);
    	initialForbiddenLatencies = getForbiddenLatencyFromReservationTable(initialReservationTable);
		initialAllowedTransitions = getArrayOfAllowedTransitionGivenForbiddenLatency(initialForbiddenLatencies);
		initialCollisionVector = getCollisionVectorGivenAllowedTransitions(initialAllowedTransitions);
		initialSimpleCycleArray = getAllSimpleCycles();
		initialGreedyCycle = getTheGreedyCycleGivenSimleCycles(initialSimpleCycleArray);
		findMaximumMALGivenCollisionVector(initialCollisionVector);
		findMinimumMALGivenReservationTable(initialReservationTable, numberOfResources, numberOfTimeSlices);
		
		removeCollisionInReservationTable(initialReservationTable);
		System.out.println("\n--------------------------------------------\n");

		if(collisionExists)
		{
			NonLinearPipeline.outputFile.append("Collision exists in reservation table.");
			NonLinearPipeline.outputFile.append("On optimization.");
			System.out.println("Collision exists in reservation table.");
			System.out.println("On optimization.");

			initialForbiddenLatencies = getForbiddenLatencyFromReservationTable(initialReservationTable);
			initialAllowedTransitions = getArrayOfAllowedTransitionGivenForbiddenLatency(initialForbiddenLatencies);
			initialCollisionVector = getCollisionVectorGivenAllowedTransitions(initialAllowedTransitions);
			initialSimpleCycleArray = getAllSimpleCycles();
			initialGreedyCycle = getTheGreedyCycleGivenSimleCycles(initialSimpleCycleArray);
			findMaximumMALGivenCollisionVector(initialCollisionVector);
			findMinimumMALGivenReservationTable(initialReservationTable, numberOfResources, numberOfTimeSlices);
		}
		else
		{
			System.out.println("Reservation table is optimized");
		}		
		
		if(currentMAL<initialMAL)
		{
			System.out.println("\n\nReservation table is now optimized.");
		}
		else
		{
			System.out.println("\n\nReservation table cannot be optimized further.");	
		}
		System.out.println("\n--------------------------------------------\n");
	}
	
	public static int[] getArrayOfElementsFromInputFile(String file) 
	{
		int lineCount = 0;
		try 
		{
			Scanner s1 = new Scanner(new File(file));
			while(s1.hasNext()){
				lineCount++;
				String line = s1.next();
				System.out.println(line);

			}
			//System.out.println("\nNumber of values: " + lineCount);

			int[] numberArray = new int[lineCount];
			Scanner s2 = new Scanner(new File(file));
			
			for(int i = 0; i<lineCount;i++){
				numberArray[i] = Integer.parseInt(s2.next());
			}
			s1.close();
			s2.close();
			
			if(numberArray.length>=200)
			{
				System.out.println("\nInput out range");
				return null;
			}
			return numberArray;
		}
		catch (FileNotFoundException e) 
		{
			System.out.println("File not found");
		}
		
		return null;
	} // EOD getArrayOfElementsFromInputFile

	public static int[][] getStagesAndCorrespondingTimeSlicesUsedFromInputArray(int[] inputArray) 
	{
		int[] inputValue = new int[inputArray.length];
		for ( int i = 0; i < inputArray.length; ++i ) 
		{
			inputValue[i] = inputArray[i];
		} 

		int maxStage=0, maxTimeSlot=0, index=0;
		while(index<inputValue.length)
		{			
			if(inputValue[index]>maxStage) 
			{
				maxStage = inputValue[index];
			}
			index = index+2;
		}
		
		index=1;
		while(index<inputValue.length)
		{
			if(inputValue[index]>maxTimeSlot) 
			{
				maxTimeSlot = inputValue[index];
			}
			index = index+2;
		}
		
		if(maxStage>10){
			System.out.println("\nInvalid number of resourses \n Acceptable range of resources are 10");
			return null;
		}
		if(maxTimeSlot>20){
			System.out.println("\nInvalid number of time slots \n Acceptable range of time slots are 20");
			return null;
		}
		
		System.out.println("\nNumber of resources: " + maxStage +"\nNumber of time-slices: " + maxTimeSlot);
		numberOfResources = maxStage;
		numberOfTimeSlices = maxTimeSlot;
		
		maxTimeSlot=maxTimeSlot+1;
		int[][] SCMatrix = new int[maxStage][maxTimeSlot];
		
		for(int i=0,k=0,m=0; i<inputValue.length;)
		{
			if(i==0)
			{
				SCMatrix[k][m] = inputValue[i];
				SCMatrix[k][m+1] = inputValue[i+1];
				m++;
				i=i+2;
			}
			else
			{
				if(SCMatrix[k][0] == inputValue [i])
				{
					SCMatrix[k][m+1] = inputValue[i+1];
					m++;
					i=i+2;
				}
				else
				{   
					k=(inputValue[i]-1);
					m =0;
					SCMatrix[k][m] = inputValue[i];
					SCMatrix[k][m+1] = inputValue[i+1];
					m++;
					i=i+2;
				}
			}
		}
		
		/*
		System.out.print("\nReservation Table: row indicates resources and column indicates time slices\n");			
		for(int k=0;k<maxStage;k++)
		{	
			for(int m=1;m<maxTimeSlot;m++) 
				if(SCMatrix[k][m] != 0) 
					System.out.print(SCMatrix[k][m]);
			System.out.print("\n");			
		}
		*/
		return SCMatrix;
	}// EOD getStagesAndClockCyslesArrayFromInputArray	
	
	public static ArrayList<Integer> getForbiddenLatencyFromReservationTable(int[][] reservationTable) 
	{
		ArrayList<Integer> forbiddenLatencies = new ArrayList<Integer>();

		//Get the number of time slices used for a task **********************
		ArrayList<Integer> numberOfTimeSliceUsedByResources = new ArrayList<Integer>();
		for (int i = 0; i < numberOfResources; i++)
		{
			int numOfTimeSlotUsed = 0;
			for (int j = 1; j < numberOfTimeSlices; j++)
			{ 
				if(reservationTable[i][j] != 0) 
					numOfTimeSlotUsed++;
				}	
			numberOfTimeSliceUsedByResources.add(i, numOfTimeSlotUsed);
		}
		//END***************************************
		
		//Get the forbidden latencies at each stage**********************
		int k=0;
		for(int i=0;i<numberOfResources;i++)
		{		
			for(int j=1;j<=numberOfTimeSliceUsedByResources.get(k);j++)
			{ 
				int h=j+1;
				while(h<=numberOfTimeSliceUsedByResources.get(k))
				{
					int latency = reservationTable[i][h]-reservationTable[i][j];
					forbiddenLatencies.add(latency);
					h++;
				}
			}
			k++;
		}
		
		ArrayList<Integer> distinctLatencies = new ArrayList<Integer>();
		for(int i=0;i<forbiddenLatencies.size();i++){
        	boolean isDistinct = true;
        	Integer element = forbiddenLatencies.get(i);
        	for(int j=i+1;j<forbiddenLatencies.size();j++)
        	{
        		if(element != forbiddenLatencies.get(j)) continue;
        		else {isDistinct = false;break;}
        	}
        	if(isDistinct){if(element!=0) {distinctLatencies.add(element);}}
		}
        forbiddenLatencies = distinctLatencies;
		Collections.sort(forbiddenLatencies);
        System.out.print("\nForbidden latencies: ");
        for(int counter: forbiddenLatencies){System.out.print(counter);}
		//END**************************************

		return forbiddenLatencies;
	}// EOD getForbiddenLatencyFromReservationTable 
	

	public static ArrayList<Integer> getCollisionVectorGivenAllowedTransitions(ArrayList<Integer> allowedTranitions) 
	{
		ArrayList<Integer> tempCollisionVector = new ArrayList<Integer>();
	    //create collisionVector
		initialCollisionVectorSize = Collections.max(allowedTranitions);  
        tempCollisionVector = new ArrayList<Integer>(initialCollisionVectorSize);
        for(Integer i=0; i<initialCollisionVectorSize; i++)
        {
        	if(allowedTranitions.contains(i+1))
        		tempCollisionVector.add(i, 0);
        	else
        		tempCollisionVector.add(i, 1);
        }
        
        Collections.reverse(tempCollisionVector);
        System.out.print("\nInitial Collision Vector: ");
        for(int counter: tempCollisionVector){
 			System.out.print(counter);
 		}
        return tempCollisionVector;
	}//EOD getCollisionVectorGivenAllowedTransitions
	

	public static ArrayList<Integer> getArrayOfAllowedTransitionGivenForbiddenLatency(ArrayList<Integer> forbiddenLatency) 
	{
		ArrayList<Integer> allowableTransitions = new ArrayList<Integer>();
        for(Integer i=1; i<Collections.max(forbiddenLatency); i++)
        {
        	if(!forbiddenLatency.contains(i))
        		allowableTransitions.add(i);
        }
		allowableTransitions.add(Collections.max(forbiddenLatency)+1);

        System.out.print("\nAllowed transitions: ");
        for(int counter: allowableTransitions){System.out.print(counter);}
        return allowableTransitions;
	}//EOD getArrayOfAllowedTransitionGivenForbiddenLatency
	
	
	public static ArrayList<Integer> getArrayOfAllowedTransitionGivenCollisionVector(ArrayList<Integer> collisionVector) 
	{
		int j = collisionVector.size();
		ArrayList<Integer> allowableTransitions = new ArrayList<Integer>();
        for(Integer i=collisionVector.size()-1; i>0; i--)
        {
        	if(collisionVector.get(i)==0)
        		allowableTransitions.add(j-i);
        }
        return allowableTransitions;
	}//EOD getArrayOfAllowedTransitionGivenCollisionVector
	
	
	public static void printStateTransition(ArrayList<Integer> initialCollisionVector, ArrayList<Integer> finalCollisionVector, int shiftedBy)
	{	
        System.out.print("\n\t");
        for(int counter: initialCollisionVector){System.out.print(counter);}
        System.out.print(" --> " + shiftedBy + " --> ");
        for(int counter: finalCollisionVector){System.out.print(counter);}
	}
	

	public static ArrayList<Integer> collisionVectorOnShiftOperation(ArrayList<Integer> collisionVector, int shiftBy)  
	{
		Integer size = collisionVector.size();
		ArrayList<Integer> newCV = new ArrayList<Integer>();
		
		if(shiftBy==0)//return collisionVector = received collisionVector
		{
			for (int i=0; i<size; i++)
				newCV.add(collisionVector.get(i));
			return newCV;
		}
		else if(shiftBy>size)//return collisionVector with all zero's
		{
			for (int i=0; i<size; i++)
				newCV.add(0);
		}
		else
		{
			int i=0;
			while(i<shiftBy) 
			{
				newCV.add(0); 
				i++;
			}
			int j=0;
			while(i<size)
			{
				int b = collisionVector.get(j++);
				newCV.add(b);
				i++;
			}
		}		
        return newCV;
	}//EOD collisionVectorOnTransition

	public static ArrayList<Integer> collisionVectorOnOROperation(ArrayList<Integer> initialCollisionVector, ArrayList<Integer> newCollisionVector) 
	{
		ArrayList<Integer> resultingArray = new ArrayList<Integer>();
		for(int i=0; i<initialCollisionVectorSize;i++)
		{
			int orResult = (initialCollisionVector.get(i)|newCollisionVector.get(i));
			resultingArray.add(orResult);
		}
		return resultingArray;
	}
	
	public static ArrayList<ArrayList<Integer>> getAllSimpleCycles() 
	{
		ArrayList<ArrayList<Integer>> simpleCycle = new ArrayList<ArrayList<Integer>>();
		int row=0, column=0;		
		System.out.print("\n\n\nSTATE_TRANSITIONS");

		for(int i = 0; i<initialAllowedTransitions.size(); i++)
		{
	        System.out.print("\ncycle " + (i+1) + ":");
			
	        Boolean shouldStopTransition = false;
			ArrayList<Integer> oldCV = new ArrayList<>(initialCollisionVector);
	        ArrayList<Integer> shiftByArray = new ArrayList<Integer>();
			
	        int shiftBy = (initialAllowedTransitions.size()!=0)?(initialAllowedTransitions.get(i)):0;
			if(shiftBy==0 || shiftBy>initialCollisionVectorSize) break;;			
			shiftByArray.add(column++ , shiftBy);
	        
			ArrayList<Integer> newCV = collisionVectorOnShiftOperation(oldCV,shiftBy);
			newCV = collisionVectorOnOROperation(oldCV, newCV);
			printStateTransition(oldCV,newCV,shiftBy);//print transition

			while(shouldStopTransition==false)
			{
				if(newCV.size()!=oldCV.size()){ shouldStopTransition = true; break;}
				else if(newCV.equals(initialCollisionVector)){ shouldStopTransition = true; break;}
				else if(newCV.equals(oldCV)){ shouldStopTransition = true; break;}
				else
				{
					//get new greedy cycle
					ArrayList<Integer> newSetOfAllowedTransitins = getArrayOfAllowedTransitionGivenCollisionVector(newCV);
			        shiftBy = (newSetOfAllowedTransitins.size()!=0)?(newSetOfAllowedTransitins.get(0)):0;
					if(shiftBy==0 || shiftBy>initialCollisionVectorSize){ shouldStopTransition = true;	break;}		
					shiftByArray.add(column++ , shiftBy);	       

			        oldCV = newCV;
			        newCV = collisionVectorOnShiftOperation(oldCV,shiftBy);
					newCV = collisionVectorOnOROperation(oldCV, newCV);
					printStateTransition(oldCV,newCV,shiftBy);//print transition
				}
			}
			simpleCycle.add(row, shiftByArray);		
			row++;
			column = 0;
		}
		
		//PRINT ALL SIMPLE CYCLES
        System.out.print("\n\n\nLIST_OF_SIMPLE_CYCLES");
        
        for(int r=0; r<simpleCycle.size(); r++)
        {
        	ArrayList<Integer> rowElements = new ArrayList<Integer>();
        	rowElements.addAll(simpleCycle.get(r));
			System.out.print("\ncycle " + (r+1) +": ");
			int c =0;
        	while(c<rowElements.size())
        	{
                System.out.print(""+rowElements.get(c));
                c++;
                if(c<rowElements.size())
                    System.out.print(", ");            	
        	}
        }
        
		return simpleCycle;
	}//EOD getAllSimpleCycles
	
	public static ArrayList<Integer> getTheGreedyCycleGivenSimleCycles(ArrayList<ArrayList<Integer>> simpleCycle) 
	{
		ArrayList<Integer> greedyCycle = new ArrayList<Integer>();
		ArrayList<Float> arrayWithAvgOfCycles = new ArrayList<Float>();

		for(int r=0; r<simpleCycle.size(); r++)
		{
			ArrayList<Integer> rowElements = new ArrayList<Integer>();
			rowElements.addAll(simpleCycle.get(r));
			
	        float countOfTransitions = 0;
	        float sumOfTransitions = 0;
	        
	        for(int c=0; c<rowElements.size(); c++)
	        {
	        	sumOfTransitions = sumOfTransitions + rowElements.get(c);
				countOfTransitions++;
	        }
			float avgLatencyForTheCurrentCycle = sumOfTransitions/countOfTransitions;
			arrayWithAvgOfCycles.add(avgLatencyForTheCurrentCycle);
		}
		
		
		//shortest cycle
		Float currentValue = arrayWithAvgOfCycles.get(0);
		int smallestIndex = 0;
		for (int j=1; j < arrayWithAvgOfCycles.size(); j++) {
			if (arrayWithAvgOfCycles.get(j) < currentValue){
				currentValue = arrayWithAvgOfCycles.get(j);
				smallestIndex = j;
			}
		}
		
		greedyCycle = simpleCycle.get(smallestIndex);
		
		//PRINT GREEDY CYCLE
		System.out.print("\n\n\nGREEDY_CYCLE is ");
		int c =0;
    	while(c<greedyCycle.size())
    	{
            System.out.print(""+greedyCycle.get(c++));
            if(c<greedyCycle.size())
                System.out.print(",");            	
    	}	
	    DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		String d = df.format(Collections.min(arrayWithAvgOfCycles));
		System.out.print("\n\n\nMAL = " + d);
		
		if(initialMAL==0)
			initialMAL = Collections.min(arrayWithAvgOfCycles);
		else
			currentMAL = Collections.min(arrayWithAvgOfCycles);			

		return greedyCycle;
	}//EOD getTheGreedyCycleGivenSimleCycles

	public static int findMaximumMALGivenCollisionVector(ArrayList<Integer> collisionVector) 
	{
		int maximumMAL = 1;
		
        for(int i=0; i<collisionVector.size(); i++)
        {
        	if(collisionVector.get(i)==1){
        		 maximumMAL++;
        	}
        }		
        System.out.print("\n" +"Theoretical Maximum MAL = "+ maximumMAL);
		return maximumMAL;
	}//EOD findMinimumMALGivenCollisionVector

	public static int findMinimumMALGivenReservationTable(int[][] reservationTable, int numberOfResources, int numberOfTimeSlice) 
	{
		int minimumMAL = 0;
		ArrayList<Integer> arrayOfNumOfCyclesUsedByEachResource = new ArrayList<Integer>();
        for(int i=0; i<numberOfResources ; i++)
        {
        	int numOfCycleUsedByCurResource = 0;
            for(int j=1; j<numberOfTimeSlice; j++)
            {
            	if(reservationTable[i][j]!=0){
            		numOfCycleUsedByCurResource++;
            	}
            }
            arrayOfNumOfCyclesUsedByEachResource.add(numOfCycleUsedByCurResource);
        }		
        minimumMAL = Collections.max(arrayOfNumOfCyclesUsedByEachResource);
        System.out.print("\n"+"Theoretical Minimum MAL = "+ minimumMAL);
		return minimumMAL;
	}//EOD findMinimumMALGivenCollisionVector

	public static void shiftElementsInReservationTable(int row, int col)
	{            	
		for(int c=col;c<numberOfTimeSlices;c++)
		{
			for(int r=0;r<numberOfResources;r++)
			{
				if(!(r==row && c==col) && initialReservationTable[r][c]>=initialReservationTable[row][col]){
				initialReservationTable[r][c] = initialReservationTable[r][c] +1;
				}
			}
		}

		/*
        System.out.print("\n\nRT:");
        for(int r=0; r<numberOfResources; r++)
        {
            System.out.print("\n");            	
            for(int c=1; c<numberOfTimeSlices; c++)
            {
                System.out.print(initialReservationTable[r][c]+" ");            	
            }
        }
        System.out.print("-----------------------------------------------");   
        */         	
        
	}//EOD shiftElementsInReservationTable
	
	public static void detectDuplicateValuesInTheTable(int curRow, int curCol, int[][] reservationTable)
	{
		boolean firstRow = true;
		for(int r=curRow;r<numberOfResources;r++)
		{
			for(int c=1;c<numberOfTimeSlices;c++)
			{
				if(c<=curCol && firstRow==true){ continue; }
				else firstRow = false;			
				if(initialReservationTable[curRow][curCol]==initialReservationTable[r][c])
				{
					collisionExists = true;
					shiftElementsInReservationTable(r, c);
					break;
				}
			}			
		}		
	}//EOD detectDuplicateValuesInTheTable
	
	public static void removeCollisionInReservationTable(int[][] reservationTable)
	{
		for(int r=0;r<numberOfResources;r++)
		{
			for(int c=1;c<numberOfTimeSlices;c++)
			{ 
				if(reservationTable[r][c]!=0)
				{
					detectDuplicateValuesInTheTable(r,c,reservationTable);
				}
			}
		}
	}//EOD removeCollisionInReservationTable


}// EOD NonLinearPipeline


	