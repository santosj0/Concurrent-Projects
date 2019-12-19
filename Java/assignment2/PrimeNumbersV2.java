package assignment2;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This program is used to demonstrate the difference in runtime between a single-thread
 * method and a multi-thread method for determining all the prime numbers within a selected 
 * range of 1 to 2147483647.
 * @author James Santos
 * @version 2.0.2
 * 
 * Updated Information:
 * 	9/29/2019
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
 *  - Compiled AwaitTermination, Futures, and Latches together into a single application
 *  - Added an option to toggle whether or not all of the primes are displayed
 *  
 *  10/23/2019
 *  - Modified the optimization thread pool from number of cores to Brian Goetz formula with specific
 *    conditions
 *  - Added a new menu item to set parameters for Goetz formula
 *  - Modified max threadpool size to 5000 threads
 */

public class PrimeNumbersV2
{	
	
	// Runnable for generating a possible prime
	static class PossiblePrimeAwaitTermination implements Runnable {
		private int num;
		private CopyOnWriteArrayList<Integer> answers;
		
		public PossiblePrimeAwaitTermination(int number, CopyOnWriteArrayList<Integer> answersMulti) {
			this.num = number;
			this.answers = answersMulti;
			
		}
		
		@Override
		public void run() {
			if(PrimeNumbersV2.determinePrimeNumber(num)) {
				answers.add(this.num);
			}
		}
		
	}
	
	// Callable for generating a possible prime
	static class PossiblePrimeCallable implements Callable<Integer> {
		private int num;
		
		public PossiblePrimeCallable(int number) {
			this.num = number;
		}
		
		public Integer call() {
			
			if(PrimeNumbersV2.determinePrimeNumber(num)) {
				return this.num;
			}
			
			return 0;
		}
		
	}
	
	// Runnable with latch for generating a possible prime
	static class PossiblePrimeLatch implements Runnable {
		private int num;
		private ArrayList<Integer> answers;
		private CountDownLatch latch;
		
		public PossiblePrimeLatch(int number, ArrayList<Integer> answers, CountDownLatch latch) {
			this.num = number;
			this.answers = answers;
			this.latch = latch;
		}
		
		@Override
		public void run() {
			if(PrimeNumbersV2.determinePrimeNumber(num)) {
				synchronized(this) {
					answers.add(this.num);
				}
			}
			latch.countDown();
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
			if(PrimeNumbersV2.determinePrimeNumber(i)) {
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
		// Prime numbers is not divisible by more than half itself
		for(int i=2; i <= possiblePrime/2; i++) {
			if(possiblePrime % i == 0) {
				return false;
			}
		}
		return true;
	}
	
	/* Menu Items */
	// Single Thread
	public static void menuItem1(int range, boolean displayPrimes) {
		System.out.println("Computing Primes through a single Thread...");
		
		// Determine all prime numbers in range of argument as well as
		// determining how long it took
		long startSingle = System.currentTimeMillis();
		ArrayList<Integer> answersSingle = PrimeNumbersV2.primeInRange(range);
		long runtimeSingle = System.currentTimeMillis() - startSingle;
		
		// Output the results
		if(displayPrimes) {
			String outputSingle = "Primes are ";
			for(int answer: answersSingle) {
				outputSingle += answer + ", ";
			}
			
			System.out.println(outputSingle.substring(0, outputSingle.length() - 2));
		}
		
		System.out.println("That took " + runtimeSingle/1000F + " seconds");
		System.out.println("There are a total of " + answersSingle.size() + " prime numbers.");
	}
	
	// Await Termination
	public static void menuItem2(int range, int size, boolean displayPrimes) {
		System.out.println("Computing Primes through multiple Threads...");
		
		// Sets the maximum number of threads that execute at one time
		int MAX_T = size;
		
		// Creates a thread pool with a fixed pool size of MAX_T
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
		
		// Determine all prime numbers in range of argument as well as
		// determining how long it took
		long startMulti = System.currentTimeMillis();
		
		// Holder for all prime numbers within the range
		CopyOnWriteArrayList<Integer> answersMulti = new CopyOnWriteArrayList<Integer>();
		
		// Generates the threads and starts them
		for(int i=2; i <= range; i++) {
			pool.execute(new PossiblePrimeAwaitTermination(i, answersMulti));
		}
		
		// Shuts down the threadpool meaning no other task can be added to the
		// executor and will wait until all of the threads currently in the
		// threadpool is finished
		pool.shutdown();
		
		// This method will pause the thread that calls this function until
		// the threadpool has finished running all of its tasks OR
		// until the timeout occurs.
		try {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			System.out.println("Interruption signal sent to halt calculations.");
		}
		
		// Finishes running
		long runtimeMulti = System.currentTimeMillis() - startMulti;
		
		// Output Results
		if(displayPrimes) {
			String outputMulti = "Primes are ";
			for(int answer: answersMulti) {
				outputMulti += answer + ", ";
			}
		
			System.out.println(outputMulti.substring(0, outputMulti.length() - 2));
		}
		
		System.out.println("That took " + runtimeMulti/1000F + " seconds");
		System.out.println("There are a total of " + answersMulti.size() + " prime numbers.");
		
	}
	
	// Futures
	public static void menuItem3(int range, int size, boolean displayPrimes) {
		System.out.println("Computing Primes through multiple Threads...");
		
		// Sets the maximum number of threads that execute at one time
		int MAX_T = size;
		
		// Creates a thread pool with a fixed pool size of MAX_T
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
		
		// List for Future Tasks
		ArrayList<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		
		// Determine all prime numbers in range of argument as well as
		// determining how long it took
		long startMulti = System.currentTimeMillis();
		
		// Holder for all prime numbers within the range
		ArrayList<Integer> answersMulti = new ArrayList<Integer>();
		
		// Generates the threads and starts them
		for(int i=2; i <= range; i++) {
			futures.add(pool.submit(new PossiblePrimeCallable(i)));
		}
		
		// Shuts down the threadpool meaning no other task can be added to the
		// executor and will wait until all of the threads currently in the
		// threadpool is finished
		pool.shutdown();
		
		// This method will pause the main thread until each future object
		// returns null before continuing
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
		
		// Finishes running
		long runtimeMulti = System.currentTimeMillis() - startMulti;
		
		// Output Results
		if(displayPrimes) {
			String outputMulti = "Primes are ";
			for(int answer: answersMulti) {
				outputMulti += answer + ", ";
			}
			
			System.out.println(outputMulti.substring(0, outputMulti.length() - 2));
		}
		
		System.out.println("That took " + runtimeMulti/1000f + " seconds");
		System.out.println("There are a total of " + answersMulti.size() + " prime numbers.");
		
	}
	
	// Latches
	public static void menuItem4(int range, int size, boolean displayPrimes) {
		System.out.println("Computing Primes through multiple Threads...");
		
		// Sets the maximum number of threads that execute at one time
		final int MAX_T = size;
		
		// Creates a thread pool with a fixed pool size of MAX_T
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
		
		// Generates the CountDownLatch where the number of threads
		// represent how much the latch has to count down from
		CountDownLatch latch = new CountDownLatch(range - 2);
		
		// Determine all prime numbers in range of argument as well as
		// determining how long it took
		long startMulti = System.currentTimeMillis();
		
		// Holder for all prime numbers within the range
		ArrayList<Integer> answersMulti = new ArrayList<Integer>();
		
		// Generates the threads and starts them
		for(int i=2; i <= range; i++) {
			PossiblePrimeLatch task = new PossiblePrimeLatch(i, answersMulti, latch);
			pool.execute(task);
		}
		
		// Shuts down the threadpool meaning no other task can be added to the
		// executor
		pool.shutdown();
		
		// Waits until all of the tasks in the executor are finished by
		// decrementing the latch until the latch reaches zero
		try {
			latch.await();
		} catch (InterruptedException e) {
			System.out.println("Unable to await for latch.");
		}
		
		// Finishes running
		long runtimeMulti = System.currentTimeMillis() - startMulti;
		
		// Output Results
		if(displayPrimes) {
			String outputMulti = "Primes are ";
			for(int answer: answersMulti) {
				outputMulti += answer + ", ";
			}
		
			System.out.println(outputMulti.substring(0, outputMulti.length() - 2));
		}
		
		System.out.println("That took " + runtimeMulti/1000F + " seconds");
		System.out.println("There are a total of " + answersMulti.size() + " prime numbers.");
		
	}
	
	// Change prime range
	private static int menuItem5(int num) {
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
	
	// Display Prime Range
	private static void menuItem6(int num) {
		System.out.println(num +" is the number");
	}
	
	// Change pool size
	private static int menuItem7(int pool, int prime) {
		System.out.println("Please input new pool size:");
		try {
			int attempt = Integer.parseInt(System.console().readLine());
			if(attempt < 0 || attempt > prime || attempt > 5000) {
				throw new Exception();
			}
			pool = attempt;
		} catch (Exception e) {
			System.out.println("Only positive integers that are less than prime range size or less than 5000.");
		}
		
		return pool;
	}
	
	// Display pool size
	private static void menuItem8(int pool) {
		System.out.println(pool + " is the pool size.");
	}
	
	// Optimize pool size
	private static int menuItem9() {
		
		int cores = Runtime.getRuntime().availableProcessors();
		int threads = (int) (cores * 1 * (1 + (0.5)));
		
		System.out.println("You have " + cores + " cores available to use. CPU utilization set to 100%. Wait time over service"
				+ " time ratio set to 0.5. Total thread count set to " + threads + ".");
		
		return threads;
	
	}
	
	// Custom Optimize pool size
	private static int menuItem10(int pool) {
		
		int cores = Runtime.getRuntime().availableProcessors();
		int threads = 0;
		
		try {
			// Retrieve CPU Utilization
			System.out.println("Please input CPU utilization between 0.1 and 1.");
			float cpu = Float.parseFloat(System.console().readLine());
			
			if(cpu < 0.1 || cpu > 1) {
				throw new Exception();
			}
			
			// Retrieve Wait Time
			System.out.println("Please input wait time in terms of milliseconds as an integer greater than 0.");
			float wtime = Integer.parseInt(System.console().readLine());
			
			if(wtime < 0) {
				throw new Exception();
			}
			
			// Retrieve Service Time
			System.out.println("Please input service time in terms of milliseconds as an integer greater than 0.");
			float stime = Integer.parseInt(System.console().readLine());
			
			if(stime < 0) {
				throw new Exception();
			}
			
			// Optimal Threads based on Input
			threads = (int) (cores * cpu * (1 + (wtime / stime)));
			
			System.out.println("Total cores are " + cores + ". "
					+ "CPU utilization set to " + cpu + ". "
					+ "Wait time / Service Time Ratio: " + wtime/stime + ". "
					+ "Thread pool count set to: " + threads + ".");
			
		}catch(Exception e) {
			System.out.println("Invalid input. Thread pool size remained the same: " + pool + ".");
			threads = pool;
		}
		
		return threads;
		
	}
	
	// Bypass cap size
	private static int menuItem11(int pool) {
		System.out.println("WARNING!! WARNING!! WARNING!!"
						 + "\nAttempting to increase the pool size limit greater than 5000 may cause a OutOfMemoryError that "
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
	
	// Toggle Prime Display
	private static boolean menuItem12(boolean primeDisplay) {
		System.out.println("PrimeDisplay is now set to: " + !primeDisplay);
		return !primeDisplay;
	}
	
	// Exits from system.
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
		// Number that will be used for retrieving all prime numbers
		// within its range
		int numArg;
		
		// Determines if prime list is shown
		boolean isDisplayed = true;
		
		// Checks to make sure the first argument passed in the 
		// command line is a proper integer
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
		
		// Total amount of threads that can be generated at a time, but caps at 5000
		int poolSize = 5000;
		if(numArg < 5000) {
			poolSize = numArg;
		}
		
		// Menu
		while(true) {
			// Display the choices and get a response
			System.out.println("Please choose an option"
					+"\nRuns:"
					+ "\n1) Single threaded"
					+ "\n2) Multi threaded await_termination"
					+ "\n3) Multi threaded futures"
					+ "\n4) Multi threaded latches"
					
					+ "\nParameter:"
					+ "\n5) Change parameter"
					+ "\n6) Display number"
					
					+ "\nPool Size:"
					+ "\n7) Change Pool Size"
					+ "\n8) Display Pool Size"
					+ "\n9) Optimize Pool Size"
					+ "\n10) Custom Optimize Pool Size"
					+ "\n11) Bypass Pool Size Limits"
					
					+ "\nOptions:"
					+ "\n12) Toggle Prime Display"
					+ "\n13) Exit");
			
			// Result of the choice from the menu
			int numDecision;
			
			// Checks to make sure decision is an integer between 1 and 7
			try {
				
				numDecision = Integer.parseInt(System.console().readLine());
				if(numDecision > 13 || numDecision < 0) {
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
			
				// Single-Threaded
				case 1:
					menuItem1(numArg, isDisplayed);
					break;
					
				// Multi-Threaded Await Termination
				case 2:
					menuItem2(numArg, poolSize, isDisplayed);
					break;
					
				// Multi-Threaded Futures
				case 3:
					menuItem3(numArg, poolSize, isDisplayed);
					break;
					
				// Multi-Threaded Latches
				case 4:
					menuItem4(numArg, poolSize, isDisplayed);
					break;
					
				// Replaces the number argument for prime determination in range
				case 5:
					numArg = menuItem5(numArg);
					break;
					
				// Display the current number for determining primes in range
				case 6:
					menuItem6(numArg);
					break;
					
				// Change the pool size
				case 7:
					poolSize = menuItem7(poolSize, numArg);
					break;
					
				// Displays the pool size
				case 8:
					menuItem8(poolSize);
					break;
				
				// Optimizes pool size based fixed Goetz Formula
				case 9:
					poolSize = menuItem9();
					break;
				
				// Optimizes pool size based on user input for Goetz Formula
				case 10:
					poolSize = menuItem10(poolSize);
					break;
					
				// Bypass pool size
				// NOT RECOMMENDED TO USE! I DON'T KNOW WHY I ALLOW THIS!!!!
				case 11:
					poolSize = menuItem11(poolSize);
					break;
				
				// Toggle prime display
				case 12:
					isDisplayed = menuItem12(isDisplayed);
					break;
					
				// Exits the program
				default:
					menuItemExit();
			}
			
			// Separates results from next menu selection
			System.out.println();
			
		}

	}

}
