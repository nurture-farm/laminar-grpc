package farm.nurture.laminar.core.sql.dao;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PrintWriterOptional {
    public static final String NL = "\n";
    List<String> buffer = null;
    PrintWriter pw = null;

    public PrintWriterOptional(PrintWriter pw) {
        if (null == pw) buffer = new ArrayList<>();
        else this.pw = pw;
    }

    public PrintWriterOptional() {
        buffer = new ArrayList<>();
    }

    public PrintWriterOptional println(String val) {
        if (null == pw) {
            buffer.add(val);
            buffer.add(NL);
        } else {
            pw.println(val);
        }
        return this;
    }

    public PrintWriterOptional print(String val) {
        if (null == pw) {
            buffer.add(val);
        } else {
            pw.print(val);
        }
        return this;
    }

    public boolean clear() {
        if (null != buffer) buffer.clear();
        return true;
    }

    public byte[] getBytes() throws UnsupportedEncodingException {
        if (null == buffer) return null;

        int size = 0;
        for (String val : buffer) {
            size += val.length();
        }
        byte[] contentB = new byte[size];

        int index = 0;
        for (String val : buffer) {
            int len = val.length();
            // System.out.println( "Size:" + size + " , index=" + index + ", len=" + len);
            if (len > 0) System.arraycopy(val.getBytes("UTF-8"), 0, contentB, index, len);
            index = index + len;
        }

        return contentB;
    }
}
