package sg;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

public class Debug {

  public static String FQCN = Debug.class.getName();

  private static HashMap<String,Boolean> enable = new HashMap<String,Boolean>();

  private static PrintStream log = System.err;

  private static boolean ALL_OFF = false;

  static {
    for (String clas : System.getProperty("debug", "").split(":")) {
      enable.put(clas.toLowerCase(), Boolean.TRUE);
    }
    ALL_OFF = enable.isEmpty();
    log = debugfile(System.getProperty("debugfile"));
  }

  static void trace(String fmt, Object... args) {
    if (ALL_OFF) return;
    String[] a = marker();
    String klass = a[0].substring(a[0].lastIndexOf('.')+1);
    if (on(klass)) {
      log.println(a[1] + String.format(fmt, args));
    }
  }

  static boolean on(String klass) {
    return (enable.get(klass.toLowerCase()) != null);
  }

  static PrintStream debugfile(String file) {
    if (file == null || (file = file.trim()).isEmpty()) return System.err;
    else {
      try {
        return new PrintStream(new FileOutputStream(file));
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  static String[] marker() {
    StackTraceElement[] st = new Throwable().getStackTrace();
    for (int i = 0; i < st.length; i++) {
      StackTraceElement ste = st[i];
      String c = ste.getClassName();
      if (FQCN.equals(c)) continue; // skip Debug class methods on stacktrace
      String m = ste.getMethodName();
      String f = ste.getFileName();
      int n = ste.getLineNumber();
      Thread th = Thread.currentThread();

      return new String[] {c,
          th.getName() + th.getPriority() +
              " [" + f + ":" + n + "-" + c + "." + m + "] "};
    }
    return new String[] {"", ""};
  }
}
