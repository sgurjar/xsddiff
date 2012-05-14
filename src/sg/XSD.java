package sg;

import htiek.DirectedGraph;
import htiek.TopologicalSort;

import java.io.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.impl.util.SchemaWriter;
import com.sun.xml.xsom.parser.SchemaDocument;
import com.sun.xml.xsom.parser.XSOMParser;

public final class XSD {

  public static final String C = XSD.class.getSimpleName();

  private XSOMParser _parser;
  private Writer _writer;
  private File _xsdfile;

  public XSD parse(File xsdfile)
      throws SAXException, IOException
  {
    SAXParserFactory f = SAXParserFactory.newInstance();
    f.setNamespaceAware(true);
    XSOMParser parser = new XSOMParser(f);
    parser.parse(xsdfile);
    _parser = parser;
    _xsdfile = xsdfile;
    return this;
  }

  public XSD setWriter(OutputStream writer) {
    _writer = new OutputStreamWriter(writer);
    return this;
  }

  public XSD setWriter(Writer writer) {
    _writer = writer;
    return this;
  }

  /** prints canonical representation of schema element */
  public void print_element(QName qname)
      throws SAXException, IOException
  {
    print_element(qname.getNamespaceURI(), qname.getLocalPart());
  }

  public void print_element(String namespaceURI, String localName)
      throws SAXException, IOException
  {
    Debug.trace("namespaceURI=%s,localName=%s", namespaceURI, localName);
    XSElementDecl elementdecl = _parser.getResult().getElementDecl(namespaceURI, localName);
    if (elementdecl == null) throw new IllegalArgumentException(
        String.format("no element found for {%s}%s in %s",namespaceURI,localName,_xsdfile)
        );
    elementdecl.apply(new XSFunctionImpl(_writer));
  }

  /** prints canonical representation of schema */
  public void print_schema(Writer writer)
      throws SAXException, IOException
  {
    new SchemaWriter(writer).visit(_parser.getResult());
  }

  public void print_references()
      throws SAXException, IOException
  {
    DirectedGraph<String> graph = new DirectedGraph<String>();

    for (SchemaDocument doc : _parser.getDocuments()) {
      dependency_graph(doc, graph);
    }

    PrintWriter pw = _writer instanceof PrintWriter ?
        (PrintWriter) _writer : new PrintWriter(_writer, AUTO_FLUSH);

    try {
      for (String doc : TopologicalSort.sort(graph)) {
        pw.println(doc);
        for (String ref : graph.edgesFrom(doc)) {
          pw.println("\t" + ref);
        }
      }
    } finally {
      pw.flush();
    }
  }

  void dependency_graph(SchemaDocument doc, DirectedGraph<String> graph)
      throws SAXException, IOException
  {
    String root = doc.getSystemId();
    if (exclude(root)) return;

    graph.addNode(root);
    for (SchemaDocument refdoc : doc.getReferencedDocuments()) {
      String ref = refdoc.getSystemId();
      graph.addNode(ref);
      graph.addEdge(root, ref);
      dependency_graph(refdoc, graph);
    }
  }

  boolean exclude(String systemId) {
    return systemId.startsWith("jar:");
    //
    // exclude systemId's like
    // jar:file:/lib/xsom.jar!/com/sun/xml/xsom/impl/parser/datatypes.xsd
  }

  static final boolean AUTO_FLUSH = true;
}
