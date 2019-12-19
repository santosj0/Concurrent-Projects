package assignment5;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
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
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class FinalProjectSingleThread {
	
	/**
	 * Removes the folder if it exists
	 * @param path The location where the folder exists
	 */
	public static void deleteFolder(File path) {
		if(path.exists()) {
			System.out.println(path.getName() + " exists. Deleting contents and remaking directory.");
			
			// List of all files
			String[] files = path.list();
			
			// Delete each file
			for(String file: files) {
				File currentFile = new File(path.getPath(), file);
				currentFile.delete();
			}
			
			// Delete directory
			path.delete();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		/* Start program */
		System.out.println("Starting program\n");
		
		// Start Runtime
		long runtime = System.currentTimeMillis();
		
		// Defines the webURL to grab images from
		String webUrl = "http://elvis.rowan.edu/~mckeep82/ccp/fa19/Astronomy/";
		
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
		
		// Generates STFolder path
		Path currentDir = Paths.get(System.getProperty("user.dir"));
		File stf = new File(Paths.get(currentDir.toString(), "STFolder").toString());
		
		// Delete STFolder if it exists
		deleteFolder(stf);
		
		// Create the STFolder
		stf.mkdir();
		
		// Provide permissions
		stf.setReadable(true);
		stf.setExecutable(true);
		
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
					String slink = String.valueOf(link);
					String jpg = slink.substring(slink.length() - 4);
					if(slink != null && slink.length() > 4 && jpg.equals(".jpg")) {
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
		
		// Download each image, convert each image, and save each image
		for(int i = 0; i < links.size(); i++) {
			
			/* DOWNLOAD IMAGE */
			System.out.println("Downloading " + images.get(i));
			
			// Generate URL to download the image from
			URL imageUrl = new URL(links.get(i));
			
			BufferedImage buffim = ImageIO.read(imageUrl);
			
			/* CONVERT TO GRAYSCALE */
			System.out.println("Converting " + images.get(i) + " to grayscale");
			
			// Get image dimensions
			int width = buffim.getWidth();
			int height = buffim.getHeight();
			
			// Convert each pixel to grayscale
			for(int k = 0; k < height; k++) {
				for(int j = 0; j < width; j++) {
					
					// Generate grayscale color
					Color c = new Color(buffim.getRGB(j, k));
					int red = (int) (c.getRed() * 0.299);
					int green = (int) (c.getGreen() * 0.587);
					int blue = (int) (c.getBlue() * 0.114);
					int gs = red + green + blue;
					Color newColor = new Color(gs, gs, gs);
					
					buffim.setRGB(j, k, newColor.getRGB());
					
				}
			}
			
			/* SAVE IMAGE */
			System.out.println("Saving grayscaled image " + images.get(i));
			
			// Generate second save location
			String grayLocation = stf.toString() + FileSystems.getDefault().getSeparator() + images.get(i);
			File local = new File(grayLocation);
			ImageIO.write(buffim, "jpg", local);
			
			// Set Permissions
			local.setReadable(true, false);
			local.setExecutable(true, false);
			
		}
		
		/* FINISHED Running */
		System.out.println("\nRuntime: " + (System.currentTimeMillis() - runtime)/1000F);
		System.out.println("Finished Single Thread");
		
	}

}
