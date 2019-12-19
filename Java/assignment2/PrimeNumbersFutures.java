package assignment2;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
 *  - Implemented the Futures method for pausing the main thread
 *  - Added ability to modify threadpool size that caps at 10000 threads
 *  - Added Optimal thread pool size based on the amount of cores the system has
 *  
 *  10/21/2019
 *  - Modified PossiblePrimeTask From Runnable to Callable to try and limit synchronization blocks
 */

public class PrimeNumbersFutures 
{	
	/*
	 * Runnable for generating a possible prime
	*/
	static class PossiblePrimeTask implements Callable<Integer> {
		private int num;
		
		public PossiblePrimeTask(int number) {
			this.num = number;
		}
		
		public Integer call() {
			
			if(PrimeNumbersFutures.determinePrimeNumber(num)) {
				return this.num;
			}
			
			return 0;
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
			if(PrimeNumbersFutures.determinePrimeNumber(i)) {
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
	
	/* Menu Items */
	//Single Thread
	public static void menuItem1(int range) {
		System.out.println("Computing Primes through a single Thread...");
		
		//Determine all prime numbers in range of argument as well as
		//determining how long it took
		long startSingle = System.currentTimeMillis();
		ArrayList<Integer> answersSingle = PrimeNumbersFutures.primeInRange(range);
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
	
	//Multi-Thread
	public static void menuItem2(int range, int size) {
		System.out.println("Computing Primes through multiple Threads...");
		
		//Sets the maximum number of threads that execute at one time
		int MAX_T = size;
		
		//Creates a thread pool with a fixed pool size of MAX_T
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
		
		//List for Future Tasks
		ArrayList<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		
		//Determine all prime numbers in range of argument as well as
		//determining how long it took
		long startMulti = System.currentTimeMillis();
		
		//Holder for all prime numbers within the range
		ArrayList<Integer> answersMulti = new ArrayList<Integer>();
		
		//Generates the threads and starts them
		for(int i=2; i <= range; i++) {
			futures.add(pool.submit(new PossiblePrimeTask(i)));
		}
		
		//Shuts down the threadpool meaning no other task can be added to the
		//executor and will wait until all of the threads currently in the
		//threadpool is finished
		pool.shutdown();
		
		//This method will pause the main thread until each future object
		//returns null before continuing
		for(Future<Integer> future : futures) {
			try {
				int result = future.get();
				if(result != 0) {
					answersMulti.add(result);
				}
			} catch (InterruptedException | ExecutionException e) {
				System.out.println("Error Message: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//Finishes running
		long runtimeMulti = System.currentTimeMillis() - startMulti;
		
		//Output Results
		String outputMulti = "Primes are ";
		for(int answer: answersMulti) {
			outputMulti += answer + ", ";
		}
		System.out.println(outputMulti.substring(0, outputMulti.length() - 2));
		System.out.println("That took " + runtimeMulti/1000f + " seconds");
		System.out.println("There are a total of " + answersMulti.size() + " prime numbers.");
		
	}
	
	//Change prime range
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
	
	//Display Prime Range
	private static void menuItem4(int num) {
		System.out.println(num +" is the number");
	}
	
	//Change pool size
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
	
	//Display pool size
	private static void menuItem6(int pool) {
		System.out.println(pool + " is the pool size.");
	}
	
	//Optimize pool size
	private static int menuItem7() {
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("You have " + cores + " cores available to use. Setting pool size to this.");
		return cores;
	}
	
	//Bypass cap size
	private static int menuItem8(int pool) {
		System.out.println("WARNING!! WARNING!! WARNING!!"
						 + "\nAttempting to increase the pool size limit greater than 10000 may cause a OutOfMemoryError that "
						 + "is not caught. This means that the JVM may sort of crash and continue to run while it tries to "
						 + "kill the said threads. As such, do you wish to continue? (Y/N)");
			
		if(!System.console().readLine().contentEquals("Y")) {
			System.out.println("I'm glad you decided against this/not input Y. Pool size will remain the same.");
			return pool;
		}
		
		System.out.println("Please input new 'possibly broken' pool size:");
		try {
			int attempt = Integer.parseInt(System.console().readLine());
			if(attempt < 0) {
				throw new Exception();
			}
			pool = attempt;
		}catch(Exception e) {
			System.out.println("For some reason, I'm glad that you failed to put a valid number.... "
					+ "I'm not even going to say how you failed. A broken JVM is just terrible for everyone. "
					+ "Pool size is remaining the same.");
		}
		
		return pool;
	}
	
	//Exits from system.
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
					+ "\n8) Bypass Pool Size Limits"
					+ "\n9) Exit");
			
			//Result of the choice from the menu
			int numDecision;
			
			//Checks to make sure decision is an integer between 1 and 7
			try {
				
				numDecision = Integer.parseInt(System.console().readLine());
				if(numDecision > 9 || numDecision < 0) {
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
					
				case 8:
					poolSize = menuItem8(poolSize);
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
