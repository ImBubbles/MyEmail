package dev.haydenholmes.network.response;

public class SBWrapper {

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
        return stringBuilder.toString();
    }

}
