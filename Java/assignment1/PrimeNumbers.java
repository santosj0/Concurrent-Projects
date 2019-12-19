package assignment1;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This program is used to demonstrate the difference in runtime between a single-thread
 * method and a multi-thread method for determining all the prime numbers within a selected 
 * range of 1 to 2147483647.
 * @author James Santos
 * @version 1.0.1
 *
 */
//@NotThreadSafe
public class PrimeNumbers 
{
	
	/*
	 * Threaded class that simply checks if the number is prime.
	 */
	static class PossiblePrime extends Thread {
		/*
		 * Possible areas of concern: Num is not changed as it is read only. The only
		 * time num could be changed is through user output before multi-option is
		 * selected. Result copies a reference to the location of primeList which
		 * can be a caused for concern since this class alters the PrimeList. However,
		 * since I do not care about the order in which the prime numbers are added to
		 * the primeList, I am not worried about a race condition occurring.
		 * There is an if then act scenario in the run when determining whether or
		 * not the number is a Prime.
		 */
		int num;
		CopyOnWriteArrayList<Integer> result;
		
		/**
		 * Thread to determine if number is prime.
		 * @param number - Number to determine if it is prime.
		 * @param primeList - If number is prime, add it to the list.
		 */
		public PossiblePrime(int number, CopyOnWriteArrayList<Integer> primeList) {
			this.num = number;
			this.result = primeList;
			this.setName("Thread" + number);
		}
		
		public void run() {
			if(PrimeNumbers.determinePrimeNumber(num)) {
				result.add(num);
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
			if(PrimeNumbers.determinePrimeNumber(i)) {
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

	/**
	 * 
	 * @param args
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
			
			if(numArg < 2) {
				throw new Exception();
			}
			
		}catch(Exception e) {
			System.out.println("First argument passed must be a positive number greater than 2 "
					+ "and less than 2147483647. "
					+ "All extra parameters will be ignored.");
			return;
		}
		
		//Menu
		while(true) {
			//Display the choices and get a response
			System.out.println("Please choose an option"
					+ "\n1) Single threaded"
					+ "\n2) Multi threaded"
					+ "\n3) Change parameter"
					+ "\n4) Display number"
					+ "\n5) Exit");
			
			//Result of the choice from the menu
			int numDecision;
			
			//Checks to make sure decision is an integer between 1 and 5
			try {
				
				numDecision = Integer.parseInt(System.console().readLine());
				if(numDecision > 5 || numDecision < 0) {
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
					System.out.println("Computing Primes through a single Thread...");
					
					//Determine all prime numbers in range of argument as well as
					//determining how long it took
					long startSingle = System.currentTimeMillis();
					ArrayList<Integer> answersSingle = PrimeNumbers.primeInRange(numArg);
					long runtimeSingle = System.currentTimeMillis() - startSingle;
					
					//Output the results
					String outputSingle = "Primes are ";
					for(int answer: answersSingle) {
						outputSingle += answer + ", ";
					}
					System.out.println(outputSingle.substring(0, outputSingle.length() - 2));
					System.out.println("That took " + runtimeSingle/1000F + " seconds");
					System.out.println("There are a total of " + answersSingle.size() + " prime numbers.");
					
					break;
					
				//Multi-threaded
				case 2:
					System.out.println("Computing Primes through multiple Threads...");
					
					//List of all possible prime threads
					ArrayList<PossiblePrime> threads = new ArrayList<PossiblePrime>();
					
					//Determine all prime numbers in range of argument as well as
					//determining how long it took
					long startMulti = System.currentTimeMillis();
					
					//Holder for all prime numbers within the range
					CopyOnWriteArrayList<Integer> answersMulti = new CopyOnWriteArrayList<Integer>();
					
					//Generates the threads and starts them
					for(int i=2; i <= numArg; i++) {
						threads.add(new PossiblePrime(i, answersMulti));
						threads.get(i - 2).run();
					}
					
					//Waits for each thread to finish before continuing
					for(PossiblePrime thread: threads) {
						try {
							thread.join();
						} catch (Exception e) {
							System.out.println("Unable to wait for " + thread.getName());
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
					System.out.println("That took " + runtimeMulti/1000F + " seconds");
					System.out.println("There are a total of " + answersMulti.size() + " prime numbers.");
					
					break;
				
				//Replaces the number argument for prime determination in range
				case 3:
					System.out.println("Please input new number to test:");
					try {
						numArg = Integer.parseInt(System.console().readLine());
						if(numArg < 1) {
							throw new Exception();
						}
						
					} catch (Exception e) {
						System.out.println("Positive integers only that are less than 2147483647.");
					}
					break;
					
				//Display the current number for determining primes in range
				case 4:
					System.out.println(numArg +" is the number");
					break;
					
				//Exits the program
				case 5:
					System.out.println("Thank you for using PrimeNumbers Threaded edition.\n");
					return;
			}
			
			//Separates results from next menu selection
			System.out.println();
			
		}

	}

}
