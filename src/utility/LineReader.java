package utility;

import java.io.IOException;
import java.io.InputStream;

public class LineReader {
	private InputStream in;
	
	public LineReader(InputStream in) {
		this.in = in;
	}
	
	public String readLine() throws IOException {
		StringBuffer str = new StringBuffer();
		
		int ch;
		while((ch = in.read()) != '\n' && ch != -1)
			str.append((char) ch);
		
		str.deleteCharAt(str.length()-1);
		
		return str.toString();
	}
	
	public void close() throws IOException {
		in.close();
	}
}
