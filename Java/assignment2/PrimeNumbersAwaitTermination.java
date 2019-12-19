package assignment2;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This program is used to demonstrate the difference in runtime between a single-thread
 * method and a multi-thread method for determining all the prime numbers within a selected 
 * range of 1 to 2147483647.
 * @author James Santos
 * @version 1.0.3
 * Updated Information:
 * 	Replace this for previous date
 * 	- Modified the switch case inside the main thread to run functions instead of previous content
 * 	- Modified PossiblePrime class to implement to Runnable instead of subclassing Thread 
 *	- Added ThreadPool to limit thread amount
 *	
 *	10/3/2019
 *	- Added Custom Exception class for when I want to throw a general exception with a message
 *
 *  10/9/2019
 *  - Removed Custom Exception class because it wasn't catching everything. May need to look at
 *    a better way to do exceptions for later.
 *  - Implemented the awaitTermination method for pausing the main thread
 *  - Added ability to modify threadpool size that caps at 10000 threads
 *  - Added Optimal thread pool size based on the amount of cores the system has
 */

public class PrimeNumbersAwaitTermination 
{	
	/*
	 * Runnable for generating a possible prime
	*/
	static class PossiblePrimeTask implements Runnable {
		private int num;
		private CopyOnWriteArrayList<Integer> answers;
		
		public PossiblePrimeTask(int number, CopyOnWriteArrayList<Integer> answersMulti) {
			this.num = number;
			this.answers = answersMulti;
			
		}
		
		@Override
		public void run() {
			if(PrimeNumbersAwaitTermination.determinePrimeNumber(num)) {
				synchronized(this) {
					answers.add(this.num);
				}
			}
		}
		
	}
	
	/**
	 * Determines every prime number in a provided range and adds 
	 * them to a list that will be returned.
	 * @param range - Range for prime number determination.
	 * @return - A list of all prime numbers in the specified range.
	 */
	public static ArrayList<Integer> primeInRange(int range) {
		ArrayList<Integer> primes = new ArrayList<Integer>();
		for(int i=2; i <= range; i++) {
			if(PrimeNumbersAwaitTermination.determinePrimeNumber(i)) {
				primes.add(i);
			}
		}
		
		return primes;
	}
	
	/**
	 * Method that returns a boolean on whether or not the provided number is a prime.
	 * @param possiblePrime - Number to be determined if it is Prime
	 * @return - True/False whether or not the number provided is a prime
	 */
	public static boolean determinePrimeNumber(int possiblePrime) 
	{
		//Prime numbers is not divisible by more than half itself
		for(int i=2; i <= possiblePrime/2; i++) {
			if(possiblePrime % i == 0) {
				return false;
			}
		}
		return true;
	}
	
	//Menu Items
	public static void menuItem1(int range) {
		System.out.println("Computing Primes through a single Thread...");
		
		//Determine all prime numbers in range of argument as well as
		//determining how long it took
		long startSingle = System.currentTimeMillis();
		ArrayList<Integer> answersSingle = PrimeNumbersAwaitTermination.primeInRange(range);
		long runtimeSingle = System.currentTimeMillis() - startSingle;
		
		//Output the results
		String outputSingle = "Primes are ";
		for(int answer: answersSingle) {
			outputSingle += answer + ", ";
		}
		System.out.println(outputSingle.substring(0, outputSingle.length() - 2));
		System.out.println("That took " + runtimeSingle/1000F + " seconds");
		System.out.println("There are a total of " + answersSingle.size() + " prime numbers.");
	}
	
	public static void menuItem2(int range, int size) {
		System.out.println("Computing Primes through multiple Threads...");
		
		//Sets the maximum number of threads that execute at one time
		int MAX_T = size;
		
		//Creates a thread pool with a fixed pool size of MAX_T
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
		
		//Determine all prime numbers in range of argument as well as
		//determining how long it took
		long startMulti = System.currentTimeMillis();
		
		//Holder for all prime numbers within the range
		CopyOnWriteArrayList<Integer> answersMulti = new CopyOnWriteArrayList<Integer>();
		
		//Generates the threads and starts them
		for(int i=2; i <= range; i++) {
			pool.execute(new PossiblePrimeTask(i, answersMulti));
		}
		
		//Shuts down the threadpool meaning no other task can be added to the
		//executor and will wait until all of the threads currently in the
		//threadpool is finished
		pool.shutdown();
		
		//Waits until all of the task in the executor is finished
		
		//This method will pause the thread that calls this function until
		//the threadpool has finished running all of its tasks OR
		//until the timeout occurs.
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			System.out.println("Interruption signal sent to halt calculations.");
		}
		
		//Finishes running
		long runtimeMulti = System.currentTimeMillis() - startMulti;
		
		//Output Results
		String outputMulti = "Primes are ";
		for(int answer: answersMulti) {
			outputMulti += answer + ", ";
		}
		System.out.println(outputMulti.substring(0, outputMulti.length() - 2));
		System.out.println("That took " + runtimeMulti/1000F + " seconds");
		System.out.println("There are a total of " + answersMulti.size() + " prime numbers.");
		
	}
	
	private static int menuItem3(int num) {
		System.out.println("Please input new number to test:");
		try {
			int attempt = Integer.parseInt(System.console().readLine());
			if(attempt < 0) {
				throw new Exception();
			}
			num = attempt;
		} catch (Exception e) {
			System.out.println("Positive integers only that are less than 2147483647.");
		}
		
		return num;
	}
	
	private static void menuItem4(int num) {
		System.out.println(num +" is the number");
	}
	
	private static int menuItem5(int pool, int prime) {
		System.out.println("Please input new pool size:");
		try {
			int attempt = Integer.parseInt(System.console().readLine());
			if(attempt < 0 || attempt > prime || attempt > 10000) {
				throw new Exception();
			}
			pool = attempt;
		} catch (Exception e) {
			System.out.println("Only positive integers that are less than prime range size or less than 10000.");
		}
		
		return pool;
	}
	
	private static void menuItem6(int pool) {
		System.out.println(pool + " is the pool size.");
	}
	
	private static int menuItem7() {
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("You have " + cores + " cores available to use. Setting pool size to this.");
		return cores;
	}
	
	public static void menuItemExit() {
		System.out.println("Thank you for using PrimeNumbers Threaded edition.\n");
		System.exit(0);
	}
	

	/**
	 * Main Driver
	 * @param args - Arguments passed from the command line
	 */
	public static void main(String[] args) 
	{
		//Number that will be used for retrieving all prime numbers
		//within its range
		int numArg;
		
		//Checks to make sure the first argument passed in the 
		//command line is a proper integer
		try {
			numArg = Integer.parseInt(args[0]);
			
			if(numArg < 0) {
				throw new Exception();
			}
			
		}catch(Exception e) {
			System.out.println("First argument passed must be a positive number greater than 0 "
					+ "and less than 2147483647. "
					+ "All extra parameters will be ignored.");
			return;
		}
		
		//Total amount of threads that can be generated at a time, but caps at 10000
		int poolSize = 10000;
		if(numArg < 10000) {
			poolSize = numArg;
		}
		
		//Menu
		while(true) {
			//Display the choices and get a response
			System.out.println("Please choose an option"
					+ "\n1) Single threaded"
					+ "\n2) Multi threaded"
					+ "\n3) Change parameter"
					+ "\n4) Display number"
					+ "\n5) Change Pool Size"
					+ "\n6) Display Pool Size"
					+ "\n7) Optimize Pool Size"
					+ "\n8) Exit");
			
			//Result of the choice from the menu
			int numDecision;
			
			//Checks to make sure decision is an integer between 1 and 7
			try {
				
				numDecision = Integer.parseInt(System.console().readLine());
				if(numDecision > 8 || numDecision < 0) {
					throw new Exception();
				}
				
			}catch(Exception e) {
				System.out.println("Please provide a number corrolating to the choices.\n");
				continue;
			}
			
			/*
			 * Based on the choice provided, you can: Run single-thread calculation,
			 * run multi-thread calculation, change the prime number range, display
			 * the current prime number range, and exit from the application.
			 */
			switch(numDecision) {
			
				//Single-Threaded
				case 1:
					menuItem1(numArg);
					break;
					
				//Multi-threaded
				case 2:
					menuItem2(numArg, poolSize);
					break;
				
				//Replaces the number argument for prime determination in range
				case 3:
					numArg = menuItem3(numArg);
					break;
					
				//Display the current number for determining primes in range
				case 4:
					menuItem4(numArg);
					break;
					
				//Change the pool size
				case 5:
					poolSize = menuItem5(poolSize, numArg);
					break;
					
				//Displays the pool size
				case 6:
					menuItem6(poolSize);
					break;
				
				//Optimizes pool size based on total amount of cores
				case 7:
					poolSize = menuItem7();
					break;
					
				//Exits the program
				default:
					menuItemExit();
			}
			
			//Separates results from next menu selection
			System.out.println();
			
		}

	}

}
