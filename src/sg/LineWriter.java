package sg;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/** Writes one line as one array element */
public class LineWriter extends Writer {

  private ArrayList<String> lines = new ArrayList<String>();
  private StringBuilder sbuf = new StringBuilder();

  @Override
  public void write(char[] cbuf, int off, int len)
      throws IOException {
    for (int i = off; i < off + len; i++) {
      char c = cbuf[i];
      switch (c) {
        case '\n':
          lines.add(sbuf.toString());
          sbuf.delete(0, sbuf.length());
          break;
        case '\r': // ignore
          break;
        default:
          sbuf.append(c);
      }
    }
  }

  @Override
  public void flush() throws IOException {
    // check for left overs
    if (sbuf != null && sbuf.length() != 0) {
      lines.add(sbuf.toString());
      sbuf = null; // done with it
    }
  }

  @Override
  public void close() throws IOException {
    flush();
  }

  public String[] asArray() {
    return lines.toArray(new String[lines.size()]);
  }

}
