/*
 * This file is part of Quack and is Licensed under the MIT License.
 */
package net.covers1624.quack.io;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by covers1624 on 3/4/21.
 */
public class IndentPrintWriter extends PrintWriter {

    public static final String DEFAULT_INDENT = "    ";

    private final PrintWriter delegate;
    private final String indentStr;

    private int indent = 0;

    public IndentPrintWriter(OutputStream os) {
        this(os, DEFAULT_INDENT);
    }

    public IndentPrintWriter(OutputStream os, String indentStr) {
        this(new PrintWriter(os, true), indentStr);
    }

    public IndentPrintWriter(PrintWriter delegate) {
        this(delegate, DEFAULT_INDENT);
    }

    public IndentPrintWriter(PrintWriter delegate, String indentStr) {
        super(NullOutputStream.INSTANCE, true);
        this.delegate = delegate;
        this.indentStr = indentStr;

        //Replace the underlying Writer
        out = new OutputStreamWriter(new ConsumingOutputStream(this::printWithIndent));
    }

    public void pushIndent() {
        indent++;
    }

    public void popIndent() {
        indent--;
    }

    private void printWithIndent(String s) {
        if (s.isEmpty()) {
            delegate.println();
            return;
        }

        for (int i = 0; i < indent; i++) {
            delegate.print(indentStr);
        }
        delegate.println(s);
    }
}
