package dev.haydenholmes.email;

import dev.haydenholmes.network.protocol.Code;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Email {

    public enum Type {
        INCOMING,
        OUTGOING
    }

    private String sender;
    private String message;
    private boolean authed;
    private final List<Recipient> recipients = new ArrayList<>();
    private int size = 0;
    private Code.SMTP_BODY bodyType = null;
    private boolean SMTPUTF8 = false;

    // Content

    private Map<String, String> headers = new LinkedHashMap<>();
    private String body;

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void addRecipient(Recipient recipient) {
        this.recipients.add(recipient);
    }

    public void setMessage(String message) {
        this.message=message;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setBodyType(Code.SMTP_BODY bodyType) {
        this.bodyType = bodyType;
    }

    public void setSMTPUTF8(boolean SMTPUTF8) {
        this.SMTPUTF8 = SMTPUTF8;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setAuthed(boolean authed) {
        this.authed = authed;
    }

    public String getSender() {
        return sender;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public String getMessage() {
        return message;
    }

    public String getBody() {
        return body;
    }

    public boolean isAuthed() {
        return authed;
    }

    // NOTE- THIS IS NOT, I REPEAT, IS NOT MEANT FOR ACTUAL ADDRESS VALIDATION
    // THIS IS SIMPLY MADE TO CHECK FOR A MALFORMED EMAIL
    public static boolean validateAddressString(String string) {
        // Simple check for "@"
        return string.contains("@");
    }

    public static String trimAddress(String string) {
        // Basically just remove "<" and ">"
        string = string.trim();
        if(string.startsWith("<"))
            string = string.substring(1);
        if(string.endsWith(">"))
            string = string.substring(0, string.length()-1);
        return string;
    }

}
