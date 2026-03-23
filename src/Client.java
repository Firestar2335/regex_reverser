import java.io.*;
import java.util.regex.*;
import tokens.*;
//import java.util.*;
import java.time.*;
import java.time.format.*;
@SuppressWarnings("unused")
public class Client {
	private static final int CORE_COUNT = 24;
	public static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM'/'dd'/'yyyy HH':'mm':'ss");

	private static final Pattern QUANTIFIER = Pattern.compile("(?<![*+?])([*+?])(?![*+?])");

	private static LocalDateTime[] timestamps = new LocalDateTime[6];

	public static void main(String[] args) {
		/*Parser single = new SingleParser();
		Parser multi = null;//new MultiParser(10);
		String input;
		//Scanner s = new Scanner(System.in);
		PartialScanner s = new PartialScanner("^(a(b(c(d)e(f)g)(hi|j|k)+(lm[no]))pq|rs(1(2(3(4(5))))(4)(18(41(3))fyhfs)))$\n");
		System.out.print("Type your regex > ");
		input = s.nextLine();
		while (!"quit".equalsIgnoreCase(input)) {
			compare(input, single, multi);
			System.out.print("Type your regex > ");
			input = s.nextLine();
		}
		s.close();*/
		//args = new String[]{"C:\\Users\\thoma\\Desktop\\test.txt","C:\\Users\\thoma\\Desktop\\testRev.txt"};
		if (args.length < 2) {
			System.out.println("regex_reverser.jar inFile outFile [numThreads [makeLazy]]");
			System.out.println("\t-inFile - The path to the file to read regex from");
			System.out.println("\t-outFile - The file to write the reversed regex to");
			System.out.print("\t-numThreads - The number of threads to use for processing. Defaults to ");
			System.out.print(CORE_COUNT);
			System.out.println(". Specify 0 to use the default value. Must be at least 2");
			System.out.println("\t-makeLazy - Whether to make all quantifiers lazy. Defaults to true");
			System.out.println();
			System.out.println("It is recommended to provide the '-Xmx' to allow the program to use more memory.");
			System.out.println("For example, 'java -jar -Xmx200G regex_reverser.jar' to allow the program to use 200 GB of ram");
			return;
			//throw new RuntimeException();
		}
		int count;
		if (args.length < 3) {
			count = CORE_COUNT-1;
		}
		else {
			try {
				int i = Integer.parseInt(args[2])-1;
				if (i == -1) {
					count = CORE_COUNT - 1;
				}
				else if (i < 0) {
					throw new IllegalArgumentException("numThreads was not at least 2");
				}
				else {
					count = i;
				}
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException(e);
			}
		}
		boolean lazy;
		if (args.length >= 4) {
			if (args[3].equalsIgnoreCase("false")) {
				lazy = false;
			}
			else if (args[3].equalsIgnoreCase("true")) {
				lazy = true;
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		else {
			lazy = true;
		}
		Parser m = new MultiParser(count);
		rev(args[0],args[1],m,lazy);
	}

	/**
	 * Reverses and writes the provided regex file to the specified file while timing it.
	 * @param inPath The path containing the file to reverse
	 * @param outPath The path to write the reversed regex to
	 * @param p The parser to parse the regex with
	 * @param lazy Whether all quantifiers not already made possesive should be made lazy
	 * @return The time elapsed between starting to read the file and finishing writing the file.
	 */
	private static Duration rev(String inPath, String outPath, Parser p, boolean lazy) {
		File in = new File(inPath);
		File out = new File(outPath);
		timestamps[0] = LocalDateTime.now();
		System.out.print("Start time: ");
		System.out.println(timestamps[0].format(fmt));
		try {
			fileReverse(in, out, p, lazy);
		}
		catch (IOException e) { }
		timestamps[5] = LocalDateTime.now();
		System.out.print("Stop time: ");
		System.out.println(timestamps[5].format(fmt));
		Duration dur = Duration.between(timestamps[0], timestamps[5]);
		System.out.print("Total time: ");
		System.out.println(formatDuration(dur));
		System.out.println("-".repeat(20));
		System.out.print("Reading: ");
		System.out.println(formatDuration(Duration.between(timestamps[0],timestamps[1])));
		System.out.print("Parsing: ");
		System.out.println(formatDuration(Duration.between(timestamps[1],timestamps[2])));
		System.out.print("Reversing: ");
		System.out.println(formatDuration(Duration.between(timestamps[2],timestamps[3])));
		if (lazy) {
			System.out.print("Lazy: ");
			System.out.println(formatDuration(Duration.between(timestamps[3],timestamps[4])));
		}
		System.out.print("Writing: ");
		System.out.println(formatDuration(Duration.between(timestamps[4],timestamps[5])));
		return dur;
	}

	private static void compare(String testValue, Parser parserA, Parser parserB) {
		System.out.println(testValue);
		if (parserA != null) {
			if (parserB != null) {
				System.out.print("Parser A: ");
			}
			TokenBase a = parserA.parse(testValue);
			System.out.println(a);
			System.out.println(a.reverse());
		}
		if (parserB != null) {
			if (parserA != null) {
				System.out.print("Parser B: ");
			}
			TokenBase b = parserB.parse(testValue);
			System.out.println(b);
			System.out.println(b.reverse());
		}
	}

	private static void fileReverse(File fileIn, File fileOut, Parser parser, boolean makeLazy) throws IOException {
		/*if (!fileIn.canRead()) {
			throw new IllegalArgumentException("Input file was not readable");
		}
		if (fileOut.exists() && !fileOut.canWrite()) {
			throw new IllegalArgumentException("Output file was writable.")
		}*/
		FileReader r = null;
		FileWriter w = null;
		try {
			r = new FileReader(fileIn);
			w = new FileWriter(fileOut);
			String s = r.readAllAsString();
			timestamps[1] = LocalDateTime.now();
			TokenBase tokens = parser.parse(s);
			timestamps[2] = LocalDateTime.now();
			System.out.print("Finished parsing at ");
			System.out.println(timestamps[2].format(fmt));
			s = tokens.reverse().compile();
			timestamps[3] = LocalDateTime.now();
			System.out.print("Reversed at ");
			System.out.println(timestamps[3].format(fmt));
			if (makeLazy) {
				Matcher m = QUANTIFIER.matcher(s);
				s = m.replaceAll("$1?");
			}
			timestamps[4] = LocalDateTime.now();
			System.out.print("Made lazy at ");
			System.out.println(timestamps[4].format(fmt));
			w.write(s);
		}
		finally {
			if (r != null) {
				r.close();
			}
			if (w != null) {
				w.close();
			}
		}
	}

	/**
	 * Formats the provided duration as 'D:HH:mm:ss.nnnnnnnnn', omitting as many fields as possible
	 * @param d
	 * @return
	 */
	public static String formatDuration(Duration d) {
		long days = d.toDaysPart();
		int hours = d.toHoursPart();
		int minutes = d.toMinutesPart();
		int seconds = d.toSecondsPart();
		int nanos = d.getNano();
		String fmt = "";
		if (days != 0) {//Days
			fmt += "%1$d:";
		}
		if (fmt.length() > 0) {//Hours
			fmt += ":%2$02d:";
		}
		else if (hours != 0) {
			fmt += "%2$d:";
		}
		if (fmt.length() > 0) {//Minutes
			fmt += "%3$02d:";
		}
		else if (minutes != 0) {
			fmt += "%3$d:";
		}
		if (fmt.length() > 0) {//Seconds
			fmt += "%4$02d";
		}
		else {
			fmt += "%4$d";
		}
		fmt += ".%5$09d";//Nanoseconds
		return String.format(fmt, days, hours, minutes, seconds, nanos);
	}
}