import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.regex.*;
import java.util.stream.*;
import java.math.*;

/**
 * Returns a {@code Scanner}-like object that first takes input from the provided object and then 
 * takes input from the console after the rovided object is exhausted.
 */
public class PartialScanner implements Closeable, Iterator<String> {
	/** The initial scanner */
	private Scanner first;
	/** Scanner connected to the console */
	private Scanner console;
	/** Whether the first scanner has been closed */
	private boolean closed;

	public PartialScanner(File source) throws FileNotFoundException {
		this(new Scanner(source));
	}

	public PartialScanner(File source, String charsetName) throws FileNotFoundException {
		this(new Scanner(source, charsetName));
	}

	public PartialScanner(File source, Charset charset) throws IOException {
		this(new Scanner(source, charset));
	}

	public PartialScanner(InputStream source) {
		this(new Scanner(source));
	}

	public PartialScanner(InputStream source, String charsetName) {
		this(new Scanner(source, charsetName));
	}

	public PartialScanner(InputStream source, Charset charset) {
		this(new Scanner(source, charset));
	}

	public PartialScanner(Readable source) {
		this(new Scanner(source));
	}

	public PartialScanner(String source) {
		this(new Scanner(source));
	}

	public PartialScanner(ReadableByteChannel source) {
		this(new Scanner(source));
	}

	public PartialScanner(ReadableByteChannel source, String charsetName) {
		this(new Scanner(source, charsetName));
	}

	public PartialScanner(ReadableByteChannel source, Charset charset) {
		this(new Scanner(source, charset));
	}

	public PartialScanner(Path source) throws IOException {
		this(new Scanner(source));
	}

	public PartialScanner(Path source, String charsetName) throws IOException {
		this(new Scanner(source, charsetName));
	}

	public PartialScanner(Path source, Charset charset) throws IOException{
		this(new Scanner(source, charset));
	}

	private PartialScanner(Scanner initial) {
		this.first = initial;
		this.console = new Scanner(System.in);
		closed = false;
	}

	public void close() {
		this.first.close();
		closed = true;
		this.console.close();
	}

	public Pattern delimiter() {
		if (!closed) {
			return first.delimiter();
		}
		return console.delimiter();
	}

	public PartialScanner useDelimiter(Pattern pattern) {
		first.useDelimiter(pattern);
		console.useDelimiter(pattern);
		return this;
	}

	public PartialScanner useDelimiter(String pattern) {
		first.useDelimiter(pattern);
		console.useDelimiter(pattern);
		return this;
	}

	public Locale locale() {
		if (!closed) {
			return first.locale();
		}
		return console.locale();
	}

	public PartialScanner useLocale(Locale locale) {
		first.useLocale(locale);
		console.useLocale(locale);
		return this;
	}

	public int radix() {
		if (!closed) {
			return first.radix();
		}
		return console.radix();
	}

	public PartialScanner useRadix(int radix) {
		first.useRadix(radix);
		console.useRadix(radix);
		return this;
	}

	public MatchResult match() {
		try {
			return console.match();
		}
		catch (IllegalStateException e) {
			return first.match();
		}
	}

	public boolean hasNext() {
		if (!closed && first.hasNext()) {
			return true;//first.hasNext();
		}
		return console.hasNext();
	}

	public String next() {
		if (!closed) {
			if (first.hasNext()) {
				return first.next();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public boolean hasNext(String pattern) {
		if (!closed && first.hasNext()) {
			return true;//first.hasNext(pattern);
		}
		return console.hasNext(pattern);
	}

	public String next(String pattern) {
		if (!closed) {
			if (first.hasNext()) {
				return first.next(pattern);
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.next(pattern);
	}

	public boolean hasNext(Pattern pattern) {
		if (!closed && first.hasNext()) {
			return true;//first.hasNext(pattern);
		}
		return console.hasNext(pattern);
	}

	public String next(Pattern pattern) {
		if (!closed) {
			return first.next(pattern);
		}
		return console.next(pattern);
	}

	public boolean hasNextLine() {
		if (!closed && first.hasNextLine()) {
			return first.hasNextLine();
		}
		return console.hasNextLine();
	}

	public String nextLine() {
		if (!closed) {
			if (first.hasNextLine()) {
				return first.nextLine();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextLine();
	}

	public String findInLine(String pattern) {
		if (!closed) {
			String r = first.findInLine(pattern);
			if (r != null && hasLine()) {
				return r;
			}
		}
		String r = console.findInLine(pattern);
		if (r != null && !closed) {
			first.close();
			closed = true;
		}
		return r;
	}

	public String findInLine(Pattern pattern) {
		if (!closed) {
			String r = first.findInLine(pattern);
			if (r != null && hasLine()) {
				return r;
			}
		}
		String r = console.findInLine(pattern);
		if (r != null && !closed) {
			first.close();
			closed = true;
		}
		return r;
	}

	/*public String findWithinHorizon(String pattern, int horizon) {

	}
	
	public String findWithinHorizon(Pattern pattern, int horizon) {
	
	}*/

	public PartialScanner skip(Pattern pattern) {
		if (!closed) {
			first.skip(pattern);
			return this;
		}
		console.skip(pattern);
		return this;
	}

	public PartialScanner skip(String pattern) {
		if (!closed) {
			first.skip(pattern);
			return this;
		}
		console.skip(pattern);
		return this;
	}

	public boolean hasNextBoolean() {
		if (!closed && first.hasNext()) {
	
			return first.hasNextBoolean();
		}
		return console.hasNextBoolean();
	}

	public boolean nextBoolean() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextBoolean();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextBoolean();
	}

	public boolean hasNextByte() {
		if (!closed && first.hasNext()) {
			return first.hasNextByte();
		}
		return console.hasNextByte();
	}

	public boolean hasNextByte(int radix) {
		if (!closed && first.hasNext()) {
			return first.hasNextByte(radix);
		}
		return console.hasNextByte(radix);
	}

	public byte nextByte() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextByte();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextByte();
	}

	public byte nextByte(int radix) {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextByte(radix);
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextByte(radix);
	}

	public boolean hasNextShort() {
		if (!closed && first.hasNext()) {
			return first.hasNextShort();
		}
		return console.hasNextShort();
	}

	public boolean hasNextShort(int radix) {
		if (!closed && first.hasNext()) {
			return first.hasNextShort(radix);
		}
		return console.hasNextShort(radix);
	}

	public short nextShort() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextShort();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextShort();
	}

	public short nextShort(int radix) {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextShort(radix);
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextShort(radix);
	}

	public boolean hasNextInt() {
		if (!closed && first.hasNext()) {
			return first.hasNextInt();
		}
		return console.hasNextInt();
	}

	public boolean hasNextInt(int radix) {
		if (!closed && first.hasNext()) {
			return first.hasNextInt(radix);
		}
		return console.hasNextInt(radix);
	}

	public int nextInt() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextInt();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextInt();
	}

	public int nextInt(int radix) {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextInt(radix);
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextInt(radix);
	}

	public boolean hasNextLong() {
		if (!closed && first.hasNext()) {
			return first.hasNextShort();
		}
		return console.hasNextLong();
	}

	public boolean hasNextLong(int radix) {
		if (!closed && first.hasNext()) {
			return first.hasNextLong(radix);
		}
		return console.hasNextLong(radix);
	}

	public long nextLong() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextLong();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextLong();
	}

	public long nextLong(int radix) {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextLong(radix);
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextLong(radix);
	}

	public boolean hasNextFloat() {
		if (!closed && first.hasNext()) {
			return first.hasNextFloat();
		}
		return console.hasNextFloat();
	}

	public float nextFloat() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextFloat();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextFloat();
	}

	public boolean hasNextDouble() {
		if (!closed && first.hasNext()) {
			return first.hasNextDouble();
		}
		return console.hasNextDouble();
	}

	public double nextDouble() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextDouble();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextDouble();
	}

	public boolean hasNextBigInteger() {
		if (!closed && first.hasNext()) {
			return first.hasNextBigInteger();
		}
		return console.hasNextBigInteger();
	}

	public boolean hasNextBigInteger(int radix) {
		if (!closed && first.hasNext()) {
			return first.hasNextBigInteger(radix);
		}
		return console.hasNextBigInteger(radix);
	}

	public BigInteger nextBigInteger() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextBigInteger();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextBigInteger();
	}

	public BigInteger nextBigInteger(int radix) {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextBigInteger(radix);
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextBigInteger(radix);
	}

	public boolean hasNextBigDecimal() {
		if (!closed && first.hasNext()) {
			return first.hasNextBigDecimal();
		}
		return console.hasNextBigDecimal();
	}

	public BigDecimal nextBigDecimal() {
		if (!closed) {
			if (first.hasNext()) {
				return first.nextBigDecimal();
			}
			else {
				first.close();
				closed = true;
			}
		}
		return console.nextBigDecimal();
	}

	public PartialScanner reset() {
		first.reset();
		console.reset();
		return this;
	}

	public Stream<String> tokens() {
		if (!closed) {
			return Stream.concat(first.tokens(), console.tokens());
		}
		return console.tokens();
	}

	public Stream<MatchResult> findAll(Pattern pattern) {
		if (!closed) {
			return Stream.concat(first.findAll(pattern), console.findAll(pattern));
		}
		return console.findAll(pattern);
	}

	public Stream<MatchResult> findAll(String patString) {
		if (!closed) {
			return Stream.concat(first.findAll(patString), console.findAll(patString));
		}
		return console.findAll(patString);
	}

	/**
	 * Determines whether there is a line separator in {@code first}
	 * @return {@code true} if first contains a complete line, {@code false} otherwise
	 */
	private boolean hasLine() {
		return true;
	}
}
