package sg;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.namespace.QName;

public class Cmdline {

  static class UsageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UsageException() { super(); }
    public UsageException(String msg) { super(msg); }

    public void error(PrintStream out) {
      String msg = getMessage();
      if (msg != null && !msg.isEmpty()) {
        out.println("error: " + msg);
      }
    }

    public void usage(PrintStream out) {
      out.println(Cmdline.usage(System.getProperty("prog", "xsddiff")));
    }
  }

  static String usage(String progname) {
    return "usage: \n"
    + "\t" + progname + " -e localname [-n namespaceuri] -p <xsdfile>\n"
    + "\t" + progname + " -e localname [-n namespaceuri] -d <xsdfile1> <xsdfile2>\n"
    + "\t" + progname + " -r <xsdfile>\n"
    + "\nwhere options are:\n"
    + "  -p <xsdfile>              prints canonical representation of schema element\n"
    + "  -d <xsdfile1> <xsdfile2>  prints diff of schema element in 2 xsd files\n"
    + "  -r <xsdfile>              prints all dependent schemas of given xsd file\n"
    + "  -e                        local part of element name\n"
    + "  -n                        namespace uri of element\n"
    + "  -h                        prints this help message";
  }

  public static Cmdline parse(String[] argv) throws UsageException {
    String progname = System.getProperty("prog", "xsddiff");

    // If the first character in the option string is a colon,
    // for example ":abc::d", then getopt() will return a ':'
    // instead of a '?' when it encounters an option with a
    // missing required argument. This allows the caller to
    // distinguish between invalid options and valid options
    // that are simply incomplete.

    Getopt g = new Getopt(
        progname,
        argv,
        ":pdrhe:n:",
        null);

    g.setOpterr(false);

    Cmdline cmdline = new Cmdline();
    int c;

    while ((c = g.getopt()) != -1) {
      switch (c) {
        case 'p': // print
          cmdline.opts.put("print", "1");
          break;
        case 'd': // diff
          cmdline.opts.put("diff", "1");
          break;
        case 'r': // references
          cmdline.opts.put("ref", "1");
          break;
        case 'h': // help
          cmdline.opts.put("help", "1");
          break;
        case 'e': // element localname
          cmdline.opts.put("localpart", g.getOptarg());
          break;
        case 'n': // element namespace
          cmdline.opts.put("namespace", g.getOptarg());
          break;
        case ':':
          System.out.println("Argument missing for option " + (char) g.getOptopt()+"\n");
          break;
        case '?':
          System.out.println("Invalid option '" + (char) g.getOptopt() + "'\n");
          break;
      }
    }

    for (int i = g.getOptind(); i < argv.length; i++) {
      cmdline.args.add(argv[i]);
    }

    if (cmdline.help()) throw new UsageException();

    return cmdline.validate();
  }

  public enum CMD {PRINT, DIFF, REF};

  private CMD cmd;
  private HashMap<String,String> opts = new HashMap<String,String>();
  private ArrayList<String> args = new ArrayList<String>();
  private QName qname;
  private File xsdfile1;
  private File xsdfile2;

  public CMD cmd(){return cmd;}
  public QName qname(){return qname;}
  public File xsdfile1(){return xsdfile1;}
  public File xsdfile2(){return xsdfile2;}

  public boolean help() { return opts.isEmpty() || opts.containsKey("help"); }

  private Cmdline validate() {
    if (opts.containsKey("print")) { setPrint(); }
    else if (opts.containsKey("diff")) { setDiff(); }
    else if (opts.containsKey("ref")) { setRef(); }
    else { throw new UsageException("no option provided"); }
    return this;
  }

  private void setPrint() {
    cmd = CMD.PRINT;

    if (args.size() < 1)
      throw new UsageException("print requires 1 xsdfiles " + args);

    File f = new File(args.get(0));
    if (!f.isFile())
      throw new UsageException("file not exists or read permission denied. " + f);
    xsdfile1 = f;

    String local = opts.get("localpart");
    if (local == null || (local = local.trim()).isEmpty())
      throw new UsageException("localpart is missing");
    String ns = opts.get("namespace");
    qname = ns == null ? new QName(local) : new QName(ns, local);

  }

  private void setRef(){
    cmd = CMD.REF;

    if (args.size() < 1)
      throw new UsageException("ref requires 1 xsdfiles " + args);

    File f = new File(args.get(0));
    if (!f.isFile())
      throw new UsageException("file not exists or read permission denied. " + f);

    xsdfile1 = f;
  }

  private void setDiff(){
    cmd = CMD.DIFF;
    String local = opts.get("localpart");
    if (local == null || (local = local.trim()).isEmpty())
      throw new UsageException("localpart is missing");

    String ns = opts.get("namespace");
    qname = ns == null ? new QName(local) : new QName(ns, local);

    if (args.size() < 2)
      throw new UsageException("diff requires 2 xsdfiles " + args);

    File f = new File(args.get(0));
    if (!f.isFile())
      throw new UsageException("file not exists or read permission denied. " + f);
    xsdfile1 = f;

    f = new File(args.get(1));
    if (!f.isFile())
      throw new UsageException("file not exists or read permission denied. " + f);
    xsdfile2 = f;
  }
}