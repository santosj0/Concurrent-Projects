package assignment3;


import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

/**
 * This program is designed to show the responsiveness of a user halting a thread pool midway while
 * it is tasked with performing multiple steps. The program follows the general guidelines of its
 * predecessor, but provides a GUI for user interaction instead of a terminal execution. 
 * @author James
 * @version 3.0.3.1
 * 
 * Version 3.0.1:
 * - Goal: Design the program based on minimum requirements
 * 10/24/2019
 * - Generated the frame of the application
 * - Added the display information
 * - Added the button information
 *   
 * Version 3.0.2:
 * - Goal: Add functionality to the start button
 * 10/24/2019
 * - Added functionality to start button
 * - Created the Swing Worker
 *  
 *  Version 3.0.3:
 *  - Goal: Add functionality to the cancel button
 *  10/24/2019
 *  - Created custom Swing Worker
 *  - Added getters for the spinners
 *  - Modified fixedThreadPool to custom ThreadPool that acts as a cachedThreadpool, but with modifying maximum thread count
 *  - TODO: Modify cancel button because it is currently broken
 *  - TODO: Remove debug print statements
 *  - TODO: Comment the crap out of the doInBackground and done menthods
 */

public class PrimeNumbersV303 {
	
	// Debugging Purposes
	private static void printMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
	
	// Callable for generating a possible prime
	private static class PossiblePrimeCallable implements Callable<Integer> {
		private int num;
			
		public PossiblePrimeCallable(int number) {
			this.num = number;
		}
		
		public Integer call() {
			
			if(PrimeNumbersV3.determinePrimeNumber(num)) {
				return this.num;
			}
			
			return 0;
		}
			
	}
	
	static class Producer extends SwingWorker<String, Integer> {
		// Instantiate variables
		private int MAX_T;		// Maximum number of threads that can be generated
		private int range;		// Determine primes in range
		int max_value = 1;		// Determines maximum value for display
		
		// Custom ThreadPool
		ThreadPoolExecutor pool = new ThreadPoolExecutor(0, 1, 10, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		
		// Setters
		public void setMAX_T(int MAX_T) {
			this.MAX_T = MAX_T;
		}
		
		public void setRange(int range) {
			this.range = range;
		}
		
		// Generate Thread Pool
		public void generatePool() {

			pool.setMaximumPoolSize(MAX_T);
			System.out.println("Maximum pool size set to: " + pool.getMaximumPoolSize());
			
		}
		
		// Process performed in background
		@Override
		protected String doInBackground() throws Exception {
			System.out.println("Running Background");
			
			// List for Future Tasks
			ArrayList<Future<Integer>> futures = new ArrayList<Future<Integer>>();
			System.out.println("FList created");
			
			for(int i=2; i <= range; i++) {
				futures.add(pool.submit(new PossiblePrimeCallable(i)));
			}
			System.out.println("Tasks submitted");
			try {
			for(Future<Integer> future : futures) {

					int result = future.get();
					
					if(result > max_value) {
						max_value = result;
					}
					
					if(result > 0) {
						numTotalPrimes++;
					}
					
					// Updates the labels
					countTotalPrimes.setText(String.valueOf(numTotalPrimes));
					countCurrentWorker.setText(String.valueOf(max_value));;
					
				
			}
			}catch(Exception e) {
				System.out.println("All futures interrupted.");
			}
			
			System.out.println("Futures received/processed.");
			return "Finished Execution";
		}
		
		// Finishes the process
		protected void done() {
			System.out.println("Done starting");
			try {
				String statusMsg = get();
				labelState.setText(statusMsg);
				countCurrentWorker.setText(String.valueOf(range));
			}catch(Exception e) {
				labelState.setText("Interrupted");
			}finally {
				if(!pool.getQueue().isEmpty()) {
					printMessage("Threads are still in queue.");
				}
				
				System.out.println(pool.getQueue().isEmpty());
				
				startBtn.setEnabled(true);
				
				//Reset Maximum Value
				max_value = 1;
				
				System.out.println("Current active thread count: " + pool.getActiveCount());
			}
			System.out.println("Done Finished\n");
		}
		
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
	
	// Display Labels - Containers
	private static JLabel labelStatus, labelState, labelThreadCount, labelPrimeRange, labelCurrentWorker, countCurrentWorker, labelTotalPrimes, countTotalPrimes;
	private static JSpinner spinThreadCount, spinPrimeRange;
	private static JFrame mainFrame;
	private static JPanel ctrPanel1, ctrPanel2, displayPanel, btnPanel;
	private static JButton startBtn, cancelBtn;
	
	// Generate the Worker
	static Producer producer;
	
	// Counter
	private static int numTotalPrimes = 0;
	
	// Getters
	private static int getSpinThreadCount() {
		// Retrieves max thread pool
		try {
			spinThreadCount.commitEdit();
		}catch(java.text.ParseException e) {
			JOptionPane.showMessageDialog(null, "Number needs to be an integer greater than 1 and less than 500. Using previous number: " + spinThreadCount.getValue() + ".");
		}

		return (int) spinThreadCount.getValue();
	}
	
	private static int getSpinPrimeRange() {
		
		// Retrieves Prime range
		try {
			spinPrimeRange.commitEdit();
		}catch(java.text.ParseException e) {
			JOptionPane.showMessageDialog(null, "Number needs to be an integer greater than 1 and less than max int value. Using previous number: " + spinPrimeRange.getValue() + ".");		
		}
				
		return (int) spinPrimeRange.getValue();
		
	}
	
	// Main Frame
	public static void initializeGUI() {
		
		// Frame Initialization - Design
		mainFrame = new JFrame("Compute Primes in Range");
		mainFrame.setSize(350, 250);
		mainFrame.setLayout(new FlowLayout());
		mainFrame.setResizable(false);
		
		// Terminates the program when closed
		mainFrame.addWindowListener(new WindowAdapter()  
	     { 
	         public void windowClosing(WindowEvent windowEvent) 
	         { 
	             System.exit(0); 
	         } 
	     }); 
		
		// Initializes the panels and add layout design
		ctrPanel1 = new JPanel();
		ctrPanel1.setLayout(new FlowLayout());
		ctrPanel2 = new JPanel();
		ctrPanel2.setLayout(new FlowLayout());
		
		// Adds the panels onto the JFrame
		mainFrame.add(ctrPanel1);
		mainFrame.add(ctrPanel2);
		
		/* Design the display Panel */
		displayPanel = new JPanel();
		GroupLayout displayLayout = new GroupLayout(displayPanel);
		displayLayout.setAutoCreateGaps(true);
		displayLayout.setAutoCreateContainerGaps(true);
		
		// Generates SpinnerModel
		SpinnerModel spRange = new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1);
		SpinnerModel spThread = new SpinnerNumberModel(1, 1, 500, 1);
		
		// Thread Count Group
		labelThreadCount = new JLabel("Number of Threads:");
		spinThreadCount = new JSpinner(spThread);
		
		// Prime Range Group
		labelPrimeRange = new JLabel("Prime Range: ");
		spinPrimeRange = new JSpinner(spRange);
		
		// Current Worker Group
		labelCurrentWorker = new JLabel("Current Worker:");
		countCurrentWorker = new JLabel("0");
		
		// Total Primes Group
		labelTotalPrimes = new JLabel("Total Amount of Primes:");
		countTotalPrimes = new JLabel("0");
		
		// Process State
		labelStatus = new JLabel("Current State:");
		labelState = new JLabel("Not Started");

		// Positions the Groups
		displayLayout.setHorizontalGroup(
			displayLayout.createSequentialGroup()
				// Label Group
				.addGroup(
					displayLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(labelStatus)
						.addComponent(labelThreadCount)
						.addComponent(labelPrimeRange)
						.addComponent(labelCurrentWorker)
						.addComponent(labelTotalPrimes)
				)
				
				// Field Group
				.addGroup(
					displayLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(labelState)
						.addComponent(spinThreadCount)
						.addComponent(spinPrimeRange)
						.addComponent(countCurrentWorker)
						.addComponent(countTotalPrimes)
				)
		);
		
		displayLayout.setVerticalGroup(
			displayLayout.createSequentialGroup()
				// Thread Pool State
				.addGroup(
					displayLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelStatus)
						.addComponent(labelState)
				)
			
				// Thread Count
				.addGroup(
					displayLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelThreadCount)
						.addComponent(spinThreadCount)
				)
				
				// Prime Range
				.addGroup(
					displayLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelPrimeRange)
						.addComponent(spinPrimeRange)
				)
				
				// Current Worker
				.addGroup(
					displayLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelCurrentWorker)
						.addComponent(countCurrentWorker)
				)
				
				// Total Primes
				.addGroup(
					displayLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelTotalPrimes)
						.addComponent(countTotalPrimes)
				)
		);
		
		// Sets the layout to the panel
		displayPanel.setLayout(displayLayout);
		
		// Adds the displayPanel to the ctrPanel1
		ctrPanel1.add(displayPanel);
		
		/* Design the button panel */
		btnPanel = new JPanel();
		GroupLayout buttonLayout = new GroupLayout(btnPanel);
		displayLayout.setAutoCreateGaps(true);
		displayLayout.setAutoCreateContainerGaps(true);
		
		// Design the start button
		startBtn = new JButton("Start");
		startBtn.setToolTipText("Commence computing prime numbers with set range.");
		startBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				producer = new Producer();
				
				// Resets counter
				numTotalPrimes = 0;
				
				// Configures / executes the Producer
				System.out.println("Alpha");
				producer.setMAX_T(getSpinThreadCount());
				producer.setRange(getSpinPrimeRange());
				producer.generatePool();
				producer.execute();
				System.out.println("Beta");
				
				// Disables the start button
				startBtn.setEnabled(false);
				System.out.println("Finished");
			}
			
		});
		
		
		// Design the cancel button
		cancelBtn = new JButton("Cancel");
		cancelBtn.setToolTipText("Halt the computation of prime numbers in set range.");
		cancelBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				producer.cancel(true);
			}
			
		});
		
		// Position the Buttons
		buttonLayout.setHorizontalGroup(
			buttonLayout.createSequentialGroup()
				.addComponent(startBtn)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
								 GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(cancelBtn)
		);
		
		buttonLayout.setVerticalGroup(
			buttonLayout.createParallelGroup()
				.addComponent(startBtn)
				.addComponent(cancelBtn)
		);
		
		// Sets the layout to the panel
		btnPanel.setLayout(buttonLayout);
				
		// Adds the displayPanel to the ctrPanel1
		ctrPanel2.add(btnPanel);		
		
		// Sets the main frame as visible
		mainFrame.setVisible(true);
		
		// Centers the application
		mainFrame.setLocationRelativeTo(null);
		
	}

	// Runs the program
	public static void main(String[] args) {
		initializeGUI();
	}

}
