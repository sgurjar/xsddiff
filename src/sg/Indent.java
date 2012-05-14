
package sg;

public class Indent {

    private final StringBuilder _buf;
    private final String _indent;

    public Indent() {
        this("\t");
    }

    public Indent(String indent) {
        _indent = indent;
        _buf = new StringBuilder();
    }

    public Indent append() {
        _buf.append(_indent);
        return this;
    }

    public Indent reduce() {
        int pos = _buf.lastIndexOf(_indent);
        if (pos != -1) _buf.delete(pos, _buf.length());
        return this;
    }

    @Override
    public String toString() {
        return _buf.toString();
    }
}
