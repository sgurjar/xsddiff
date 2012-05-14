package sg;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.namespace.QName;

import org.xml.sax.SAXException;

import bmsi.util.Diff;
import bmsi.util.DiffPrint.UnifiedPrint;

public class XSDDiff {

  static final String C = XSDDiff.class.getSimpleName();

  public static class SchemaFile {
    private File _file;
    private String _header;
    public SchemaFile header(String h) { _header = h; return this; }
    public SchemaFile file(File f) { _file = f; return this; }
    public File file() { return _file; }
    public String header() {
      try {return _header == null ? _file.getCanonicalPath() : _header;}
      catch (IOException ex) { throw new RuntimeException(ex); }
    }

    @Override public String toString() { return "(" + header() + "->" + file() + ")"; }
  }

  private PrintWriter pw = new PrintWriter(System.out);

  public XSDDiff setOutput(PrintWriter writer) { pw = writer; return this; }

  public void diff(SchemaFile file1, SchemaFile file2, QName qname)
      throws SAXException, IOException {

    Debug.trace("file1=%s, file2=%s, qname=%s", file1, file2, qname);

    LineWriter w1 = new LineWriter(), w2 = new LineWriter();
    XSD xsd1 = new XSD().parse(file1.file()).setWriter(w1);
    xsd1.print_element(qname.getNamespaceURI(), qname.getLocalPart());
    XSD xsd2 = new XSD().parse(file2.file()).setWriter(w2);
    xsd2.print_element(qname.getNamespaceURI(), qname.getLocalPart());

    String[] a1 = w1.asArray(); Debug.trace("a1 %s lines", a1.length);
    String[] a2 = w2.asArray(); Debug.trace("a2 %s lines", a2.length);

    if (Debug.on(C)) {
      Debug.trace("=========%s", file1);
      for (String a : a1)
        Debug.trace("%s", a);
      Debug.trace("=========%s", file2);
      for (String a : a2)
        Debug.trace("%s", a);
    }

    Diff diff = new Diff(a1, a2);
    boolean reverse = false;
    Diff.change script = diff.diff_2(reverse);
    if (script == null) pw.println("No differences");
    UnifiedPrint p = new UnifiedPrint(a1, a2);
    p.setOutput(pw);
    p.print_header(file1.header(), file2.header());
    p.print_script(script);
    pw.flush();
  }
}
