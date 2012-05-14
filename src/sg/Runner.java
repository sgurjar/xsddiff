package sg;

public class Runner {

  public static void main(String[] args) throws Exception {
    //System.setProperty("debug", "XSDDiff:XSD");
    //System.setProperty("debugfile", "/temp/xsddiff.debug");

    run1(args);
  }

  static void run1(String[] args){
    try {
      Cmdline cmdline = Cmdline.parse(args);
      switch (cmdline.cmd()) {
        case PRINT:
          new XSD()
            .setWriter(System.out)
            .parse(cmdline.xsdfile1())
            .print_element(cmdline.qname());
          break;
        case DIFF:
          new XSDDiff().diff(
              new XSDDiff.SchemaFile().file(cmdline.xsdfile1()),
              new XSDDiff.SchemaFile().file(cmdline.xsdfile2()),
              cmdline.qname()
              );
          break;
        case REF:
          new XSD()
            .setWriter(System.out)
            .parse(cmdline.xsdfile1())
            .print_references();
          break;
        default:
          throw new AssertionError("unknown cmd is '" + cmdline.cmd() + "'");
      }
    } catch (Cmdline.UsageException usage) {
      usage.error(System.err);
      usage.usage(System.err);
    } catch (Exception error) {
      System.err.println(error.getMessage());
      error.printStackTrace(System.err);
    }
  }
}
