package dev.haydenholmes.network.protocol;

public class SBWrapper { // would extend but StringBuilder is defined as final

    private final StringBuilder stringBuilder;

    public SBWrapper() {
        this.stringBuilder=new StringBuilder();
    }

    protected void append(String val) {
        this.stringBuilder.append(val);
    }

    protected void append(String val, boolean spacing) {
        append(val);
        if(spacing)
            append(" ");
    }

    protected void newLine() {
        append("\n");
    }

    public String get() {
        return stringBuilder.toString().trim();
    }

}
