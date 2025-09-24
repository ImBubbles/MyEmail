package dev.haydenholmes.network.protocol.ready;

import dev.haydenholmes.network.protocol.Code;
import dev.haydenholmes.network.protocol.SMTPBuilder;

import java.util.List;

public class ReadySMTPC { // Prepared SMTP client queries

    public static String quit() {
        return new SMTPBuilder()
                .command(Code.SMTP_COMMANDS.QUIT)
                .get();
    }

    public static String ehlo() {
        return new SMTPBuilder()
                .command(Code.SMTP_COMMANDS.EHLO)
                .domain()
                .get();
    }

    public static String mailFrom(String address, List<Code.SMTP_NOTIFY> notifs, Code.SMTP_ORCPT orcpt) {

        String notifStr = "";
        if(notifs!=null && !notifs.isEmpty()) {
            StringBuilder notifStrB = new StringBuilder();
            for(Code.SMTP_NOTIFY notif : notifs) {
                notifStrB.append(notif.name()).append(", ");
            }
            notifStr = notifStrB.toString();
            notifStr = notifStr.substring(0, notifStr.lastIndexOf(","));
        }

        SMTPBuilder smtpBuilder = new SMTPBuilder()
                .command(Code.SMTP_COMMANDS.MAIL_FROM, true)
                .message("<" + address + "> ");

        if(!notifStr.isBlank())
            smtpBuilder.message("[NOTIFY=" + notifStr + "] ");

        if(orcpt!=null)
            smtpBuilder.message("[ORCPT=" + orcpt.name() + "] ");

        return smtpBuilder.get().trim();

    }

    public static String startTLS() {
        return new SMTPBuilder()
                .command(Code.SMTP_COMMANDS.STARTTLS)
                .get();
    }

}
