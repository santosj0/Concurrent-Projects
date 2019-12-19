package assignment2;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This program is used to demonstrate the difference in runtime between a single-thread
 * method and a multi-thread method for determining all the prime numbers within a selected 
 * range of 1 to 2147483647.
 * @author James Santos
 * @version 1.0.2
 * Updated Information:
 * 	Replace this for previous date
 * 	- Modified the switch case inside the main thread to run functions instead of previous content
 * 	- Modified PossiblePrime class to implement to Runnable instead of subclassing Thread 
 *	- Added ThreadPool to limit thread amount
 *	
 *	10/3/2019
 *	- Added Custom Exception class for when I want to throw a general exception with a message
 */

public class PrimeNumbersLatches 
{
	/*
	 * Runnable for generating a possible prime
	*/
	static class PossiblePrimeTask implements Runnable {
		private int num;
		private ArrayList<Integer> answers;
		private CountDownLatch latch;
		
		public PossiblePrimeTask(int number, ArrayList<Integer> answers, CountDownLatch latch) {
			this.num = number;
			this.answers = answers;
			this.latch = latch;
		}
		
		@Override
		public void run() {
			if(PrimeNumbersLatches.determinePrimeNumber(num)) {
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
			if(PrimeNumbersLatches.determinePrimeNumber(i)) {
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
		ArrayList<Integer> answersSingle = PrimeNumbersLatches.primeInRange(range);
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
		final int MAX_T = size;
		
		//Creates a thread pool with a fixed pool size of MAX_T
		ExecutorService pool = Executors.newFixedThreadPool(MAX_T);
		
		//Generates the CountDownLatch where the number of threads
		//represent how much the latch has to count down from
		CountDownLatch latch = new CountDownLatch(range - 2);
		
		//Determine all prime numbers in range of argument as well as
		//determining how long it took
		long startMulti = System.currentTimeMillis();
		
		//Holder for all prime numbers within the range
		ArrayList<Integer> answersMulti = new ArrayList<Integer>();
		
		//Generates the threads and starts them
		for(int i=2; i <= range; i++) {
			PossiblePrimeTask task = new PossiblePrimeTask(i, answersMulti, latch);
			pool.execute(task);
		}
		
		//Shuts down the threadpool meaning no other task can be added to the
		//executor
		pool.shutdown();
		
		//Waits until all of the tasks in the executor are finished by
		//decrementing the latch until the latch reaches zero
		try {
			latch.await();
		} catch (InterruptedException e) {
			System.out.println("Unable to await for latch.");
		}
		
		//Finishes running
		long runtimeMulti = System.currentTimeMillis() - startMulti;
		
		//Output Results
		/* 
		 * Issue seems to occur here because the main thread is trying to 
		 * print the answers, but is not waiting for all of the tasks to complete
		 * first. 
		 */
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
			num = Integer.parseInt(System.console().readLine());
			if(num < 0) {
				throw new Exception();
			}
			
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
			pool = Integer.parseInt(System.console().readLine());
			if(pool < 0 || pool > prime) {
				throw new Exception();
			}
		} catch (Exception e) {
			System.out.println("Only positive integers that are less than prime range size.");
		}
		
		return pool;
	}
	
	private static void menuItem6(int pool) {
		System.out.println(pool + " is the pool size.");
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
		
		//Total amount of threads that can be generated at a time
		int poolSize = numArg;
		
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
					+ "\n7) Exit");
			
			//Result of the choice from the menu
			int numDecision;
			
			//Checks to make sure decision is an integer between 1 and 7
			try {
				
				numDecision = Integer.parseInt(System.console().readLine());
				if(numDecision > 7 || numDecision < 0) {
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
					
				//Exits the program
				default:
					menuItemExit();
			}
			
			//Separates results from next menu selection
			System.out.println();
			
		}

	}

}
