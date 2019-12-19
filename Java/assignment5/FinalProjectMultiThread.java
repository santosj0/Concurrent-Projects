package assignment5;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * 
 * This file will go to the specified url link, web scrap all anchor tags to retrieve image names, download them, 
 * convert them to gray scale, and save them in the specified folder.
 * This multithreaded program follows a 2 producer - 2 producer/consumer - 2 producer/consumer - producer/consumer
 * layout.
 * @author James
 *
 */

public class FinalProjectMultiThread {

	/* FUNCTIONS */
	
	/**
	 * Removes the folder if it exists
	 * @param path The location where the folder exists
	 */
	public static void deleteFolder(File path) {
		if(path.exists()) {
			System.out.println("\n" + path.getName() + " exists. Deleting contents.\n");
			
			// List of all files
			String[] files = path.list();
			
			// Delete each file
			for(String file: files) {
				File currentFile = new File(path.getPath(), file);
				String name = currentFile.getName();
				currentFile.delete();
				System.out.println(name + " was deleted.");
			}
			
			// Delete directory
			path.delete();
			System.out.println("\nMTFolder director was deleted.");
		}
	}
	
	/**
	 * This function will compare the word provided with each extension. If one matches, 
	 * then return true. Otherwise, false.
	 * @param extensions  Array of strings with the desired extensions.
	 * @param word  String to be compared
	 * @return Boolean of whether or not word exists in the extensions array
	 */
	public static boolean checkExtension(String[] extensions, String word) {
		
		// Defaults to false
		boolean exist = false;
		
		// Checks to see if the available 
		for(String ext: extensions) {
			if(word.equals(ext)) {
				exist = true;
			}
		}
		
		return exist;
	}
	
	/**
	 * Closes any streams with the try catch imbedded for cleaner code.
	 * @param c  Stream that is closable. Example: InputStream/OutputStream.
	 */
	public static void close(Closeable c) {
		// Stream is not open
		if (c == null) return;
		
		// Attempts to close it. If it cannot close it, then something really
		// wrong is happening.
		try {
			c.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/* PRODUCERS/CONSUMERS */
	static class SaveConsumer extends Thread {
		
		// Variables
		private BlockingQueue<HashMap<String, BufferedImage>> sQueue;
		private final File loc;
		
		// Instantiation without Name
		public SaveConsumer(BlockingQueue<HashMap<String, BufferedImage>> saveQueue, File location) {
			this.sQueue = saveQueue;
			this.loc = location;
		}
		
		// Instantiation with Name
		public SaveConsumer(BlockingQueue<HashMap<String, BufferedImage>> saveQueue, File location, String name) {
			this.sQueue = saveQueue;
			this.loc = location;
			this.setName(name);
		}
		
		// Executes when run
		public void run() {
			try {
				
				// Loops until it recieves a poison pill
				while(true) {
					// Retrieve the HashMap - Take from Queue
					HashMap<String, BufferedImage> shmap = sQueue.take();
					
					// Check for Poison Pill
					if(shmap.containsKey("POISON")) {
						System.out.println(this.getName() + " was killed.");
						
						// Kil the thread
						return;
					}
					
					// Save buffered image at location provided
					shmap.forEach((k, v) -> {
						System.out.println("Saving grayscaled image " + k);
						
						// Generate save location
						String grayLocation = loc.toString() + FileSystems.getDefault().getSeparator() + k;
						File local = new File(grayLocation);
						
						// Saves image at location
						try {
							ImageIO.write(v, "jpg", local);
							
							// Set Permissions
							local.setReadable(true, false);
							local.setExecutable(true, false);
							
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					});
					
				}
				
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
	}
	
	static class GrayScaleConsumer extends Thread {
		
		// Queues
		BlockingQueue<HashMap<String, BufferedImage>> gQueue;
		BlockingQueue<HashMap<String, BufferedImage>> sQueue;
		
		// Instantiation without Name
		public GrayScaleConsumer(BlockingQueue<HashMap<String, BufferedImage>> grayQueue, BlockingQueue<HashMap<String, BufferedImage>> saveQueue) {
			this.gQueue = grayQueue;
			this.sQueue = saveQueue;
		}
		
		//Instantiation with Name
		public GrayScaleConsumer(BlockingQueue<HashMap<String, BufferedImage>> grayQueue, BlockingQueue<HashMap<String, BufferedImage>> saveQueue, String name) {
			this.gQueue = grayQueue;
			this.sQueue = saveQueue;
			this.setName(name);
		}
		
		// Executes when thread runs
		public void run() {
			try {
				
				// Loops until Poison Pill
				while(true) {

					// Retrieve the HashMap
					HashMap<String, BufferedImage> ghmap = gQueue.take();
					
					// Create HashMap for sQueue
					HashMap<String, BufferedImage> shmap = new HashMap<String, BufferedImage>();
					
					// Check for Poison Pill
					if(ghmap.containsKey("POISON")) {
						System.out.println(this.getName() + " was killed.");
						
						// Add poison pill to save queue
						shmap.put("POISON", null);
						sQueue.put(shmap);
						
						// Kills the thread
						return;
					}
										
					// Convert to Gray Scale
					ghmap.forEach((k, v) -> {
						System.out.println("Converting " + k + " to grayscale");
						BufferedImage buffim = v;
						
						// Retrieve image dimensions
						int width = buffim.getWidth();
						int height = buffim.getHeight();
						
						// Convert each pixel to grayscale
						for(int i = 0; i < height; i++) {
							for(int j = 0; j < width; j++) {
								
								// Generate grayscale color
								Color c = new Color(buffim.getRGB(j, i));
								int red = (int) (c.getRed() * 0.299);
								int green = (int) (c.getGreen() * 0.587);
								int blue = (int) (c.getBlue() * 0.114);
								int gs = red + green + blue;
								Color newColor = new Color(gs, gs, gs);
								
								// Set the grayscale color
								buffim.setRGB(j, i, newColor.getRGB());
								
							}
						}
						
						// Add buffim to save queue
						shmap.put(k, buffim);
						try {
							sQueue.put(shmap);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					});

				}
				
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		
	}
	
	static class DownloadConsumer extends Thread {
		
		// Queues
		BlockingQueue<HashMap<String, String>> dQueue;
		BlockingQueue<HashMap<String, BufferedImage>> gQueue;
		
		// Instantiation without Name
		public DownloadConsumer(BlockingQueue<HashMap<String, String>> downQueue, BlockingQueue<HashMap<String, BufferedImage>> grayQueue) {
			this.dQueue = downQueue;
			this.gQueue = grayQueue;
		}
		
		// Instantiation with Name
		public DownloadConsumer(BlockingQueue<HashMap<String, String>> downQueue, BlockingQueue<HashMap<String, BufferedImage>> grayQueue, String name) {
			this.dQueue = downQueue;
			this.gQueue = grayQueue;
			this.setName(name);
		}
		
		// Executes when thread starts
		public void run(){
			try {
				
				// Runs until it retrieves Poison Pill
				while(true) {
					
					// Retrieve the HashMap
					HashMap<String, String> dhmap = dQueue.take();
					
					// Create HashMap for gQueue
					HashMap<String, BufferedImage> ghmap = new HashMap<String, BufferedImage>();
					
					// Determine Poison Pill
					if(dhmap.get("POISON") != null) {
						System.out.println(this.getName() + " was killed.");
						
						// Add Poison pill to Gray Scale Queue
						ghmap.put("POISON", null);
						gQueue.put(ghmap);
						
						// Kill Thread
						return;
					}
					
					// Image name
					String image = dhmap.get("Image");
					
					// Start download
					System.out.println("Downloading " + image);
					
					// Generate URL to download the image from
					URL imageUrl = null;
					try {
						imageUrl = new URL(dhmap.get("Url"));
					} catch (MalformedURLException e) {
						e.printStackTrace();
						
						// Move onto next in the Queue
						continue;
					}
					
					// Download the Images
					try {
						// Retrieve image from url
						BufferedImage buffImg = ImageIO.read(imageUrl);
						
						// Add image to Gray Scale Queue
						ghmap.put(image, buffImg);
						gQueue.put(ghmap);
						
					} catch (IOException e) {
						e.printStackTrace();
						
						// Move onto next link
						continue;
					}
					
				}
				
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			
		}
		
	}
	
	static class DownloadProducer extends Thread {
		
		// Variables
		BlockingQueue<HashMap<String, String>> dQueue;
		List<String> links;
		List<String> images;
		int end;
		
		// Instantiation with name
		public DownloadProducer(BlockingQueue<HashMap<String, String>> downQueue, List<String> links, List<String> images) {
			this.dQueue = downQueue;
			this.links = links;
			this.images = images;
			this.end = links.size();
		}
		
		// Instantiation with name
		public DownloadProducer(BlockingQueue<HashMap<String, String>> downQueue, List<String> links, List<String> images, String name) {
			this.dQueue = downQueue;
			this.links = links;
			this.images = images;
			this.end = links.size();
			this.setName(name);
		}
		
		// Executes when thread runs
		public void run() {
			
			// Performs Action on each image/link
			for(int i = 0; i <= end; i++) {
				// Sets up HashMap for adding to Download Queue
				HashMap<String, String> thm = new HashMap<String, String>();
				
				// Variables
				String image;
				
				// Create Maps
				if(i == end) {
					image = "POISON PILL";
					thm.put("POISON", "TRUE");
				}else {
					image = images.get(i);
					thm.put("Url", links.get(i));
					thm.put("Image", image);
				}
				
				// Add to Download Queue
				try {
					dQueue.put(thm);
				} catch (InterruptedException e) {
					System.out.println("Unable to add " + image + " to Queue.");
				}
				
				System.out.println("Added " + image + " to Queue.");
				
			}
			
			// Producer has finished
			System.out.println(this.getName() + " was killed.");
			
		}
		
	}
	
	
	/* DRIVER */
	public static void main(String[] args) throws Exception{

		/* START PROGRAM */
		System.out.println("Starting program");
		
		// Runtime
		long runtime = System.currentTimeMillis();
		
		// Defines the webURL to grab images from
		String webUrl = "http://elvis.rowan.edu/~mckeep82/ccp/fa19/Astronomy/";
		
		// Second test url
		// String webUrl = "http://elvis.rowan.edu/~santosj0/awp/homework3/files/static/images/defaults/";
		
		// Creates an empty url object
		URL url = null;
		
		// Establish the url as a URL object
		try {
			url = new URL(webUrl);
		} catch (MalformedURLException e) {		
			System.out.println("Issue trying to establish url.");
			System.out.println(e.getMessage());
			System.exit(0);
		}
		
		// Generates MTFolder path
		Path currentDir = Paths.get(System.getProperty("user.dir"));
		File mtf = new File(Paths.get(currentDir.toString(), "MTFolder").toString());
		
		// Delete MTFolder if it exists
		deleteFolder(mtf);
		
		// Create the MTFolder
		mtf.mkdir();
		
		// Set Permissions
		mtf.setReadable(true, false);
		mtf.setExecutable(true, false);
		
		System.out.println("\nMTFolder directory was generated.\n");
		
		// Establish URL connection
		URLConnection uconn = url.openConnection();
		
		// Read the URL
		BufferedReader br = new BufferedReader(new InputStreamReader(uconn.getInputStream()));
		String html = "";
		String inputLine;
		while((inputLine = br.readLine()) != null) {
			html += inputLine;
		}
		br.close();
				
		// Extract the jpg links from the html page
		HTMLEditorKit.Parser parser = new ParserDelegator();
		Reader reader = new StringReader(html);
		final List<String> images = new ArrayList<String>();
		
		parser.parse(reader, new HTMLEditorKit.ParserCallback() {
			public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
				// Only executes on anchor tags
				if(t == HTML.Tag.A) {
					
					// Retrieves the href of the anchor tags
					Object link = a.getAttribute(HTML.Attribute.HREF);
					// Converts the href to a string
					String[] sep = String.valueOf(link).split("\\.");
					String extension = sep[sep.length - 1];
					String[] exts = {"jpg"};
							
					if(checkExtension(exts, extension)) {
						images.add(String.valueOf(link));
					}
					
				}
			}
		}, true);
		
		// Closes the StringReader
		reader.close();
		
		// Generate the links
		final List<String> links = new ArrayList<String>();
		for(String image: images) {
			links.add(webUrl + image);
		}
		
		/* Download Images */
		// Generate Download Queue
		BlockingQueue<HashMap<String, String>> dQueue = new LinkedBlockingDeque<>(5);
		
		// Retrieve end points
		int mid = links.size() / 2;
		int end = links.size();
		
		// Divide the lists in half
		List<String> l1 = links.subList(0, mid);
		List<String> l2 = links.subList(mid, end);
		List<String> i1 = images.subList(0, mid);
		List<String> i2 = images.subList(mid, end);
		
		// Create the download producers
		DownloadProducer dP1 = new DownloadProducer(dQueue, l1, i1);
		DownloadProducer dP2 = new DownloadProducer(dQueue, l2, i2);
		
		// Start the download producers
		dP1.start();
		dP2.start();
		
		// Generate GrayScale Queue
		BlockingQueue<HashMap<String, BufferedImage>> gsQueue = new LinkedBlockingDeque<>(5);
		
		// Create the download consumers
		DownloadConsumer dC1 = new DownloadConsumer(dQueue, gsQueue, "Download Consumer 1");
		DownloadConsumer dC2 = new DownloadConsumer(dQueue, gsQueue, "Download Consumer 2");
		
		// Start the download consumers
		dC1.start();
		dC2.start();
		
		// Generate Save Queue
		BlockingQueue<HashMap<String, BufferedImage>> sQueue = new LinkedBlockingDeque<>(5);
		
		// Create the grayscale consumers
		GrayScaleConsumer gC1 = new GrayScaleConsumer(gsQueue, sQueue, "Gray Scale Consumer 1");
		GrayScaleConsumer gC2 = new GrayScaleConsumer(gsQueue, sQueue, "Gray Scale Consumer 2");
		
		// Start the grayscale consumers
		gC1.start();
		gC2.start();
		
		// Create the save consumers
		SaveConsumer sC1 = new SaveConsumer(sQueue, mtf, "Save Consumer 1");
		SaveConsumer sC2 = new SaveConsumer(sQueue, mtf, "Save Consumer 2");
		
		// Start the save consumers
		sC1.start();
		sC2.start();
		
		// Wait until all threads have killed themselves
		while(dC1.isAlive() || dC2.isAlive()
				|| gC1.isAlive() || gC2.isAlive()
				|| sC1.isAlive() || sC2.isAlive()) {
			Thread.sleep(50);
		}
		
		/* FINISH PROGRAM */
		System.out.println("\nRuntime: " + (System.currentTimeMillis() - runtime)/1000F);
		System.out.println("Finished program");
		
	}

}
