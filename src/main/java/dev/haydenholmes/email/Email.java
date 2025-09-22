package dev.haydenholmes.email;

import dev.haydenholmes.network.response.ready.ReadyESMTP;

import java.util.ArrayList;
import java.util.List;

public class Email {

    public enum Type {
        INCOMING,
        OUTGOING
    }

    private String sender;
    private String message;
    private final List<Recipient> recipients = new ArrayList<>();

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void addRecipient(Recipient recipient) {
        this.recipients.add(recipient);
    }

    public void setMessage(String message) {
        this.message=message;
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

    // NOTE- THIS IS NOT, I REPEAT, IS NOT MEANT FOR ACTUAL ADDRESS VALIDATION
    // THIS IS SIMPLY MADE TO CHECK FOR A MALFORMED EMAIL
    public static boolean validateEmailString(String string) {
        // Simple check for "<", ">", and "@"
        if(!(string.contains("<")&&string.endsWith(">")&&string.contains("@"))) {
            return false;
        }
        return true;
    }

    public static String trimEmail(String string) {
        // Just in case handle if there's other stuff besides the email
        int beginning = string.indexOf('<');
        int end = string.indexOf('>');
        string = string.substring(beginning, end);
        // Basically just remove "<" and ">"
        string = string.trim();
        if(string.startsWith("<"))
            string = string.substring(1, string.length()-1);
        if(string.endsWith(">"))
            string = string.substring(0, string.length()-2);
        return string;
    }



}
