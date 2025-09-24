package dev.haydenholmes.network.protocol.ready;

import dev.haydenholmes.MyEmail;
import dev.haydenholmes.network.protocol.Code;
import dev.haydenholmes.network.protocol.SMTPBuilder;
import dev.haydenholmes.util.StringCast;

public class ReadySMTPS { // Prepared SMTP server responses

    public static String acceptance() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.READY)
                .domain()
                .message("Service Ready")
                .get();
    }

    public static String badCommand() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.INTERNAL_SERVER_ERROR)
                .message("Syntax error, Command not understood")
                .get();
    }

    public static String badSyntax() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.BAD_SYNTAX)
                .message("Syntax error in parameters or arguments")
                .get();
    }

    public static String badSequence() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.BAD_SEQUENCE)
                .message("Bad sequence of commands")
                .get();
    }

    public static String acknowledge() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE)
                .message("OK")
                .get();
    }

    public static String startTLSReady() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.READY)
                .message("Ready to start TLS")
                .get();
    }

    public static String tlsRequired() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.BAD_SEQUENCE)
                .message("TLS connection required")
                .get();
    }

    public static String authSuccess() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.AUTH_SUCCESS)
                .message("Auth successful")
                .get();
    }

    public static String authFailed() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.AUTH_FAIL)
                .message("Auth failed")
                .get();
    }

    public static String username64() {
        return new SMTPBuilder()
                .message(StringCast.toBase64("Username:"))
                .get();
    }

    public static String password64() {
        return new SMTPBuilder()
                .message(StringCast.toBase64("Password:"))
                .get();
    }

    public static String transactionFailed() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.FAILURE)
                .message("Transaction failed")
                .get();
    }

    public static String relayNotAllowed() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.FAILURE)
                .message("Cannot relay on this server")
                .get();
    }

    public static String relayOnly() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.FAILURE)
                .message("Relay server")
                .get();
    }

    public static String startMail() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.START_MAIL_INPUT)
                .message("Start mail input; end with <CRLF>.<CRLF>")
                .get();
    }

    public static String bye() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.QUIT)
                .message("Bye")
                .get();
    }

    // ADVERTISING

    public static String advertiseTLS() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE, true)
                .message("STARTTLS")
                .get();
    }

    public static String advertiseSize() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE, true)
                .message("SIZE "+MyEmail.properties.SIZE())
                .get();
    }

    public static String advertiseHelp() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE, true)
                .message("HELP")
                .get();
    }

    public static String advertiseAuth() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE, true)
                .message("AUTH PLAIN LOGIN")
                .get();
    }

    public static String advertisePipelining() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE, true)
                .message("PIPELINING")
                .get();
    }

    public static String advertise8BITMIME() {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE, true)
                .message("8BITMIME")
                .get();
    }

    public static String advertiseHello(String part) {
        return new SMTPBuilder()
                .status(Code.SMTP_STATUS.ACKNOWLEDGE, true)
                .message(part + " Hello")
                .get();
    }

}
