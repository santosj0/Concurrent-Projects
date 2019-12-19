package assignment5;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DisplayPath {

	public static void main(String[] args) {
		Path currentDir = Paths.get(System.getProperty("user.dir"));
		System.out.println(currentDir.toString());

	}

}
