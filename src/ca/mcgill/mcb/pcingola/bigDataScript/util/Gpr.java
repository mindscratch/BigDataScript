/*
 * Created on Nov 24, 2003
 */
package ca.mcgill.mcb.pcingola.bigDataScript.util;

import java.awt.Color;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * General pupose rutines
 * @author root
 */
public class Gpr {

	@SuppressWarnings("unchecked")
	static private class StringTokenizer {

		String string = null;
		int tokens = 0;
		int[] separatorPosition = new int[1000];

		StringTokenizer(String value, char delim) {
			string = value;
			// Loop on the characters counting the separators and remembering
			// their positions
			StringCharacterIterator sci = new StringCharacterIterator(string);
			char c = sci.first();
			while (c != CharacterIterator.DONE) {
				if (c == delim) {
					// Remember its position
					separatorPosition[tokens] = sci.getIndex();
					tokens++;

					// Resize the position array if needed
					if (tokens >= separatorPosition.length) {
						int[] copy = new int[separatorPosition.length * 10];
						System.arraycopy(separatorPosition, 0, copy, 0, separatorPosition.length);
						separatorPosition = copy;
					}
				}
				c = sci.next();
			}
			// Add one token: tokens = separatorCount + 1
			tokens++;
		}

		<T> T[] tokens(Class<T> componentType) {
			T[] r = (T[]) Array.newInstance(componentType, tokens);
			Constructor<T> ctor;
			try {
				ctor = componentType.getConstructor(String.class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Cannot create an array of type [" + componentType + "] from an array of String. The type [" + componentType.getSimpleName() + "] must define a single arg constructor that takes a String.class instance.");
			}

			String currentValue = null;
			int i = 0;
			try {
				int start = 0;
				for (i = 0; i < tokens; i++) {
					// Index of the token's last character (exclusive)
					int nextStart = separatorPosition[i];
					// Special case for the last token
					if (i == tokens - 1) nextStart = string.length();

					// Calculate the size of the token
					int length = nextStart - start;
					if (length > 0) {
						currentValue = string.substring(start, nextStart);
						r[i] = ctor.newInstance(currentValue);
					}
					start = nextStart + 1;
				}
			} catch (Exception e) {
				throw new RuntimeException("Cannot create an instance of type [" + componentType + "] from the " + i + "th string value [" + currentValue + "].", e);
			}
			return r;
		}
	}

	// Number of cores in this computer
	public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

	// User's home directory
	public static final String HOME = System.getProperty("user.home");

	/**
	 * Return file's name (without the path)
	 * @param file
	 */
	public static String baseName(String file) {
		if (file == null) return "";
		File f = new File(file);
		return f.getName();
	}

	/**
	 * Return file's name (without the path)
	 * @param file
	 */
	public static String baseName(String file, String ext) {
		File f = new File(file);
		String base = f.getName();
		if (base.endsWith(ext)) return base.substring(0, base.length() - ext.length());
		return base;
	}

	// Show a long as a 64 bit binary number
	public static String bin64(long l) {
		String bl = Long.toBinaryString(l);
		String out = "";
		for (int i = bl.length(); i < 64; i++)
			out += "0";
		return out + bl;
	}

	/**
	 * Can we read this file (either exact name or append a '.gz'
	 */
	public static boolean canRead(String fileName) {
		File inputFile = new File(fileName);
		if (inputFile.exists() && inputFile.canRead()) return true;

		inputFile = new File(fileName + ".gz");
		if (inputFile.exists() && inputFile.canRead()) return true;

		return false;
	}

	/**
	 * Compare if objects are null. If non-null, compare objects
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static int compareNull(Comparable o1, Comparable o2) {
		int i1 = (o1 == null ? 0 : 1);
		int i2 = (o2 == null ? 0 : 1);
		int cmp = i1 - i2;
		if (cmp != 0) return cmp;

		return o1.compareTo(o2);
	}

	/**
	 * Count lines in a file (same as 'wc -l file' in unix)
	 */
	public static int countLines(String file) {
		try {
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(file));
			int lines = 0;
			while (reader.readLine() != null)
				lines++;
			reader.close();
			return lines;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Prits a debug message (prints class name, method and line number)
	 * @param obj					Object to print
	 * @param offset				Offset N lines from stacktrace
	 */
	public static void debug(Object obj) {
		debug(obj, 1, true);
	}

	public static void debug(Object obj, int offset) {
		debug(obj, offset, true);
	}

	public static void debug(Object obj, int offset, boolean newLine) {
		StackTraceElement ste = new Exception().getStackTrace()[1 + offset];
		String steStr = ste.getClassName();
		int ind = steStr.lastIndexOf('.');
		steStr = steStr.substring(ind + 1);
		steStr += "." + ste.getMethodName() + "(" + ste.getLineNumber() + "):\t" + (obj == null ? null : obj.toString());
		if (newLine) System.err.println(steStr);
		else System.err.print(steStr);
	}

	/**
	 * Return file's dir
	 */
	public static String dirName(String file) {
		File f = new File(file);
		return f.getParent();
	}

	/**
	 * Does 'file' exist?
	 */
	public static boolean exists(String file) {
		return new File(file).exists();
	}

	/**
	 * Get a file's extension (all letters after the last '.'
	 */
	public static String extName(String file) {
		String base = baseName(file);
		int idx = base.lastIndexOf('.');
		if (idx >= 0) return base.substring(idx + 1);
		return "";
	}

	public static File getCanonicalFile(File f) {
		try {
			return f.getCanonicalFile();
		} catch (Exception e) {
			return f;
		}
	}

	/**
	 * Canonical path as a string
	 */
	public static String getCanonicalFileName(File f) {
		try {
			return f.getCanonicalPath();
		} catch (Exception e) {
			return f.getPath();
		}
	}

	/**
	 * Canonical path as a string
	 */
	public static String getCanonicalFileName(String f) {
		try {
			File ff = new File(f);
			return ff.getCanonicalPath();
		} catch (Exception e) {
			return f;
		}
	}

	/**
	 * Generate an evenly separated pallette of colors
	 * @param num	Number of colors
	 * @return
	 */
	public static Paint[] getPaints(int num) {
		return getPaints(num, false);
	}

	public static Paint[] getPaints(int num, boolean goUp) {
		int r, g, b, i, jump, jumpr, jumpg, jumpb;
		Paint paints[] = new Paint[num];

		for (jump = 1; (jump * jump * jump) <= num; jump++);

		jumpr = jumpg = jumpb = jump;
		if (((jumpr - 1) * jumpg * jumpb) <= num) jumpr--;

		if ((jumpr * (jumpg - 1) * jumpb) <= num) jumpg--;

		if (jumpr > 1) jumpr = 255 / (jumpr - 1);
		else jumpr = 255;

		if (jumpg > 1) jumpg = 255 / (jumpg - 1);
		else jumpg = 255;

		if (jumpb > 1) jumpb = 255 / (jumpb - 1);
		else jumpb = 255;

		if (goUp) for (i = 0, r = 0; (r <= 255) && (i < num); r += jumpr)
			for (g = 0; (g <= 255) && (i < num); g += jumpg)
				for (b = 0; (b <= 255) && (i < num); b += jumpb, i++)
					paints[i] = new Color(r, g, b);
		else for (i = 0, r = 255; (r >= 0) && (i < num); r -= jumpr)
			for (g = 255; (g >= 0) && (i < num); g -= jumpg)
				for (b = 255; (b >= 0) && (i < num); b -= jumpb, i++)
					paints[i] = new Color(r, g, b);
		return paints;
	}

	public static StringBuffer inputStream2StringBuffer(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		StringBuffer file = new StringBuffer();

		String line = reader.readLine();
		while (line != null) {
			file.append(line + "\n");
			line = reader.readLine();
		}
		return file;
	}

	public static boolean isValidIp(String ip) {
		if (ip == null) return false;

		String[] ipSplitted = ip.split("\\.");

		if (ipSplitted.length != 4) return false;

		for (int i = 0; i < ipSplitted.length; i++) {
			if (ipSplitted[i].length() > 3) return false;
			try {
				int ipPart = Integer.parseInt(ipSplitted[i]);
				if (ipPart < 0 || ipPart > 255) return false;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return true;
	}

	/** Is this a valid Rid */
	public static boolean isValidRid(int i) {
		return (i > 0);
	}

	/**
	 * Remove spaces and tabs from string.
	 * @param str
	 * @return
	 */
	public static String noSpaces(String str) {
		String lines[] = str.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines)
			sb.append(line.trim().replaceAll("\\s", "") + "\n");
		return sb.toString();
	}

	/**
	 * Equivalent to Boolean.parseBoolean, except it returns 0 on invalid integer (NumberFormatException)
	 * @param s
	 * @return	int
	 */
	public static boolean parseBoolSafe(String s) {
		try {
			if (s == null) return false;
			if (s.equalsIgnoreCase("true")) return true; // 'true'
			if (s.equalsIgnoreCase("t")) return true; // Abreviation of 'true'
			if (Gpr.parseIntSafe(s) != 0) return true; // A non-zero number
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Equivalent to Double.parseDouble(), except it returns 0 on invalid double (NumberFormatException)
	 */
	public static double parseDoubleSafe(String s) {
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			return 0.0;
		}
	}

	/**
	 * Equivalent to Float.parseFloat(), except it returns 0 on invalid double (NumberFormatException)
	 */
	public static float parseFloatSafe(String s) {
		try {
			return Float.parseFloat(s);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Equivalent to Integer.parseInt, except it returns 0 on invalid integer (NumberFormatException)
	 */
	public static int parseIntSafe(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Equivalent to Long.parseLong, except it returns 0 on invalid integer (NumberFormatException)
	 */
	public static long parseLongSafe(String s) {
		try {
			return Long.parseLong(s);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Equivalent to Long.parseLong, except it returns 0 on invalid integer (NumberFormatException)
	 * It also allows numbers to be finished by 'K', 'M', 'G', etc. to identify (2^10, 2^20, 2^30, etc.)
	 */
	public static long parseMemSafe(String s) {

		s = s.trim().toUpperCase();
		if (s.isEmpty()) return 0L;

		// Find multiplier
		long mult = 1;
		if (s.endsWith("K")) mult = 1L << 10;
		else if (s.endsWith("M")) mult = 1L << 20;
		else if (s.endsWith("G")) mult = 1L << 30;
		else if (s.endsWith("T")) mult = 1L << 40;
		else if (s.endsWith("P")) mult = 1L << 50;

		// Remove last char
		if (mult != 1) s = s.substring(0, s.length() - 1).trim();

		try {
			return Long.parseLong(s) * mult;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Add 'preLine' to each line in 'str'
	 */
	public static String prependEachLine(String preLine, String str) {
		if (preLine == null || str == null) return null;

		StringBuilder sb = new StringBuilder();
		for (String line : str.split("\n"))
			sb.append(preLine + line + "\n");

		return sb.toString();

	}

	/**
	 * Read a file as a String.
	 * Note: the file can be compressed using gzip (file name must have a ".gz" extension).
	 *
	 * @param shellFileName : File to read (null on error)
	 * @param showExceptions : show exceptions if true
	 */
	public static String read(BufferedReader inFile) {
		StringBuffer strb = new StringBuffer();
		char buff[] = new char[10240];
		int len = 0;
		try {
			while ((len = inFile.read(buff)) >= 0)
				strb.append(buff, 0, len);
			inFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return strb.toString();
	}

	/**
	 * Read an input stream
	 */
	public static String read(InputStream is) {
		if (is == null) return null;
		StringBuffer strb = new StringBuffer();
		char buff[] = new char[10240];
		int len = 0;

		try {
			BufferedReader inFile = new BufferedReader(new InputStreamReader(is));
			while ((len = inFile.read(buff)) >= 0)
				strb.append(buff, 0, len);
			inFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return strb.toString();

	}

	/**
	 * Try to open a file (BufferedReader) using either the file or a gzip file (appending '.gz' to fileName)
	 */
	public static BufferedReader reader(String fileName) {
		return reader(fileName, false, true);
	}

	public static BufferedReader reader(String fileName, boolean gzip) {
		return reader(fileName, gzip, true);
	}

	/**
	 * Try to open a file (BufferedReader) using either the file or a gzip file (appending '.gz' to fileName)
	 * @param fileName
	 * @param gzip : If true, file is assumed to be gzipped
	 * @return
	 */
	public static BufferedReader reader(String fileName, boolean gzip, boolean showExceptions) {
		BufferedReader reader = null;

		try {
			if (fileName.equals("-")) {
				return new BufferedReader(new InputStreamReader(System.in));
			} else if (fileName.endsWith(".gz") || gzip) {
				// This is a gzip compressed file
				File inputFile = new File(fileName);
				if (inputFile.exists()) return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(fileName)))));
				else throw new RuntimeException("File not found '" + fileName + "'");
			} else {
				// Try opening the file
				File inputFile = new File(fileName);
				if (inputFile.exists()) return new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
				else {
					// Doesn't exists? => append '.gz' the file's name and try gzipped file
					String fileNameGz = fileName + ".gz";
					inputFile = new File(fileNameGz);
					if (inputFile.exists()) return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(new File(fileNameGz)))));
					else {
						if (!showExceptions) {
							Gpr.debug("File not found '" + fileName + "'");
							return null;
						}
						throw new RuntimeException("File not found '" + fileName + "'");

					}
				}
			}
		} catch (FileNotFoundException e) {
			reader = null;
			Gpr.debug(e);
		} catch (IOException e) {
			reader = null;
			Gpr.debug(e);
		}

		return reader;
	}

	public static String readFile(String fileName) {
		return readFile(fileName, true);
	}

	/**
	 * Read a file as a String.
	 * Note: the file can be compressed using gzip (file name must have a ".gz" extension).
	 *
	 * @param fileName : File to read (null on error)
	 * @param showExceptions : show exceptions if true
	 */
	public static String readFile(String fileName, boolean showExceptions) {
		BufferedReader inFile;
		StringBuffer strb = new StringBuffer();
		char buff[] = new char[10240];
		int len = 0;
		try {
			// Open
			inFile = reader(fileName, false, showExceptions);
			if (inFile == null) return "";

			// Read
			while ((len = inFile.read(buff)) >= 0)
				strb.append(buff, 0, len);
			inFile.close();
		} catch (IOException e) {
			if (showExceptions) throw new RuntimeException(e);
			else return "";
		}
		return strb.toString();
	}

	/**
	 * Read an object from a file (supposing an object was previously serialized to that file)
	 * @param fileName
	 * @return An object from that file
	 */
	public static Object readFileSerialized(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object obj = in.readObject();
			in.close();
			return obj;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read an object from a file (supposing an object was previously serialized to that file)
	 * Note: The file is compressed using GZIP
	 * @param fileName
	 * @return An object from that file
	 */
	public static Object readFileSerializedGz(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
			Object obj = in.readObject();
			in.close();
			return obj;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Read an object from a file (supposing an object was previously serialized to that file)
	 * Note: Same as 'readFileSerializedGz' but it throws all the exceptions
	 *
	 *
	 * @param fileName
	 * @return An object from that file
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public static Object readFileSerializedGzThrow(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(fileName)));
		Object obj = in.readObject();
		in.close();
		return obj;
	}

	public static String removeExt(String file) {
		int lastDot = file.lastIndexOf('.');
		if (lastDot >= 0) return file.substring(0, lastDot);
		return file;
	}

	/**
	 * Remove extension from a file (if matche one of 'fileExtensions[]')
	 * @param file
	 * @return
	 */
	public static String removeExt(String file, String fileExtensions[]) {
		for (String ext : fileExtensions)
			if (file.toLowerCase().endsWith(ext)) return file.substring(0, file.length() - ext.length());

		return file;
	}

	/**
	 * Create a string of n time 'c'
	 */
	public static String repeat(char c, int n) {
		if (n == 0) return "";

		char str[] = new char[n];
		Arrays.fill(str, c);
		return new String(str);
	}

	/**
	 * Convert a string to be used in a filename
	 */
	public static String sanityzeName(String fileName) {
		String out = fileName.trim().replaceAll("[^0-9_a-zA-Z\\.]", "_");
		out = out.replaceAll("_+", "_");

		// Remove all leading underscores
		while (out.startsWith("_") || out.startsWith("."))
			out = out.substring(1);

		// Remove all trailing underscores
		while (out.endsWith("_") || out.endsWith("."))
			out = out.substring(0, out.length() - 1);

		return out;
	}

	/**
	 * Show a mark
	 * @param i
	 * @param showEvery
	 */
	public static void showMark(int i, int showEvery) {
		if (showEvery <= 0) return;
		if (i % showEvery == 0) {
			if (i % (100 * showEvery) == 0) System.err.print(".\n" + i + "\t");
			else System.err.print('.');
		}
	}

	/**
	 * Show a mark
	 * @param i
	 * @param showEvery
	 */
	public static void showMark(int i, int showEvery, String newLineStr) {
		if (showEvery <= 0) return;
		if (i % showEvery == 0) {
			if (i % (100 * showEvery) == 0) System.err.print(".\n" + newLineStr + i + "\t");
			else System.err.print('.');
		}
	}

	/**
	 * Show a mark (on STDERR)
	 * @param i
	 * @param showEvery
	 */
	public static void showMarkStderr(int i, int showEvery) {
		if (i % showEvery == 0) {
			if (i % (100 * showEvery) == 0) System.err.print(".\n" + i + "\t");
			else System.err.print('.');
		}
	}

	public static void showStackTrace(int steps) {
		showStackTrace(steps, 1);
	}

	/**
	 * Prints a stack trace for a number of steps
	 * @param count
	 */
	public static void showStackTrace(int steps, int offset) {
		Exception e = new Exception();
		String sep = "";
		for (int i = 0; i < steps; i++) {
			int num = i + offset + 1;
			if (num < e.getStackTrace().length) {
				StackTraceElement ste = e.getStackTrace()[num];
				String steStr = ste.getClassName();
				int ind = steStr.lastIndexOf('.');
				steStr = steStr.substring(ind + 1);
				steStr += "." + ste.getMethodName() + "(" + ste.getLineNumber() + "):";
				System.err.println(sep + steStr);
				sep = "\t";
			}
		}
	}

	/**
	 * Splits a separated string into an array of <code>String</code> tokens. If
	 * the input string is null, this method returns null.
	 *
	 * <p/>
	 * Implementation note: for performance reasons, this implementation uses
	 * neither StringTokenizer nor String.split(). StringTokenizer does not
	 * return all tokens for strings of the form "1,2,,3," unless you use an
	 * instance that returns the separator. By doing so, our code would need to
	 * modify the token string which would create another temporary object and
	 * would make this method very slow. <br/>
	 * String.split does not return all tokens for strings of the form
	 * "1,2,3,,,". We simply cannot use this method.
	 * <p/>
	 * The result is a custom String splitter algorithm which performs well for
	 * large Strings.
	 *
	 * @param value
	 *            the string value to split into tokens
	 * @return an array of String Objects or null if the string value is null
	 */
	static public String[] split(final String value, char delim) {
		if (value == null) return null;
		StringTokenizer st = new StringTokenizer(value, delim);
		return st.tokens(String.class);
	}

	public static String tabs(int tabs) {
		String t = "";
		for (int i = 0; i < tabs; i++)
			t += "\t";
		return t;
	}

	/**
	 * Write an object to a file
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static String toFile(String fileName, Object obj) {
		return toFile(fileName, obj, false);
	}

	/**
	 * Write an object to a file
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static String toFile(String fileName, Object obj, boolean append) {
		try {
			String str = obj.toString();
			FileWriter outFile = new FileWriter(fileName, append);
			outFile.write(str);
			outFile.flush();
			outFile.close();
			return str;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write an object to a file (as a string)
	 * Note: The file is compressed using GZIP
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static void toFileGz(String fileName, Object obj) {
		PrintStream outFile;
		try {
			outFile = new PrintStream(new GZIPOutputStream(new FileOutputStream(fileName)));
			outFile.print(obj.toString());
			outFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write an object to a file by invoking Serialization methods
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static void toFileSerialize(String fileName, Object obj) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(fileName));
			out.writeObject(obj);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Write an object to a file by invoking Serialization methods
	 * Note: The file is compressed using GZIP
	 * @param fileName: File to write
	 * @param obj: Object
	 */
	public static void toFileSerializeGz(String fileName, Object obj) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));
			out.writeObject(obj);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Convert 2 numbers to memory units [KB, MB, GB, TB, PB]
	 */
	public static String toStringMem(long memBytes) {
		double k = 1024, m = k * k, g = k * m, t = k * g, p = k * t;
		double mem = memBytes;

		String unit = "B";
		if (mem > p) {
			mem /= p;
			unit = "PB";
		} else if (mem > t) {
			mem /= t;
			unit = "TB";
		} else if (mem > g) {
			mem /= g;
			unit = "GB";
		} else if (mem > m) {
			mem /= m;
			unit = "MB";
		} else if (mem > k) {
			mem /= k;
			unit = "KB";
		}
		return String.format("%.1f %s", mem, unit);
	}
}
