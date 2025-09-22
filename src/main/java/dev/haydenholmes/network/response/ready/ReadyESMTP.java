package dev.haydenholmes.network.response.ready;

import dev.haydenholmes.MyEmail;
import dev.haydenholmes.network.response.Code;
import dev.haydenholmes.network.response.ESMTPBuilder;

public class ReadyESMTP {

    public static String acceptance() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.READY)
                .sendDomain(true)
                .setMessage("Service Ready")
                .build();
    }

    public static String badCommand() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.INTERNAL_SERVER_ERROR)
                .setMessage("Syntax error, Command not understood")
                .build();
    }

    public static String badSyntax() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.BAD_SYNTAX)
                .setMessage("Syntax error in parameters or arguments")
                .build();
    }

    public static String badSequence() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.BAD_SEQUENCE)
                .setMessage("Bad sequence of commands")
                .build();
    }

    public static String acknowledge() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE)
                .setMessage("OK")
                .build();
    }

    public static String startTLSReady() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.READY)
                .setMessage("Ready to start TLS")
                .build();
    }

    public static String transactionFailed() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.FAILURE)
                .setMessage("Transaction failed")
                .build();
    }

    // ADVERTISING

    public static String advertiseTLS() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE, true)
                .setMessage("STARTTLS")
                .build();
    }

    public static String advertiseSize() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE, true)
                .setMessage("SIZE "+MyEmail.properties.SIZE())
                .build();
    }

    public static String advertiseHelp() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE, true)
                .setMessage("HELP")
                .build();
    }

    public static String advertiseAuth() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE, true)
                .setMessage("AUTH PLAIN LOGIN")
                .build();
    }

    public static String advertisePipelining() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE, true)
                .setMessage("PIPELINING")
                .build();
    }

    public static String advertise8BITMIME() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE, true)
                .setMessage("8BITMIME")
                .build();
    }

    public static String advertiseHello(String part) {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.ACKNOWLEDGE, true)
                .setMessage(part + " Hello")
                .build();
    }

}
