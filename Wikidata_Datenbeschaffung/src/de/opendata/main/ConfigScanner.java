package de.opendata.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Scanner to read configuration files
 * 
 * @author Marco Kinkel
 * @version 0.1
 */
public class ConfigScanner {

	private final File filePath;
	private final static Charset ENCODING = StandardCharsets.UTF_8;

	/**
	 * Constructor.
	 * 
	 * @param fileName
	 *            full name of an existing, readable file.
	 * @throws FileNotFoundException
	 * 				if file of filename is not found
	 */
	public ConfigScanner(String fileName) throws FileNotFoundException {
		filePath = new File(fileName);
	}

	/**
	 * Getter for filepath
	 * @return filepath
	 */
	public File getFilePath() {
		return filePath;
	}

	/** Template method that calls {@link #processLine(String)}. */
	public final void processLineByLine() throws IOException {
		String line = "";

		try (Scanner scanner = new Scanner(filePath, ENCODING.name())) {
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				if (!line.equals("") && !line.substring(0, 1).equals("#")) {
					processLine(line);
				}
			}
		}
	}

	/**
	 * Process lines and store values in Helper-Fields
	 */
	protected void processLine(String line) {
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter("=");
		if (scanner.hasNext()) {
			String property = scanner.next();
			String value = scanner.next();

			property = property.trim();
			value = value.trim();

			switch (property) {
			case "UPDATE_INTERVAL_SEC":
				Helper.UPDATE_INTERVAL_SEC = Integer.parseInt(value);
				break;
			case "LOGFILE_PATH":
				Helper.LOGFILE_PATH = value;
				break;
			case "LOGGING_LEVEL":
				Helper.LOGGING_LEVEL = value;
				break;
			case "LOGFILE_COUNT":
				Helper.LOGFILE_COUNT = Integer.parseInt(value);
				break;
			case "OFFLINE_MODE":
				Helper.OFFLINE_MODE = Boolean.parseBoolean(value);
				break;
			case "TIMEOUT_SEC":
				Helper.TIMEOUT_SEC = Integer.parseInt(value);
				break;
			case "DUMPFILE_PATH":
				Helper.DUMPFILE_PATH = value;
				break;
			case "BLOCK_SIZE":
				Helper.BLOCK_SIZE = Integer.parseInt(value);
				break;
			case "DATABASE_PATH":
				Helper.DATABASE_PATH = value;
				break;
			case "DB_USERNAME":
				Helper.DB_USERNAME = value;
				break;
			case "DB_PASSWORD":
				Helper.DB_PASSWORD = value;
				break;
			case "SCHEMA":
				Helper.SCHEMA = value;
				break;
			case "VIEWS_PATH":
				Helper.VIEWS_PATH = value;
			case "COMMIT_MODE":
				Helper.COMMIT_MODE = value;
			// TODO: When adding a new configuration attribute
			}

		} else {
			System.out.println("Empty or invalid line. Unable to process.");
		}
		scanner.close();
	}

}
