package dev.haydenholmes.network.response.ready;

import dev.haydenholmes.network.response.Code;
import dev.haydenholmes.network.response.ESMTPBuilder;

public class ReadyESMTP {

    public static String acceptance() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.OK)
                .sendDomain(true)
                .setProtocol(Code.PROTOCOL.ESMTP)
                .setMessage("My Mail Service")
                .build();
    }

    public static String badSyntax() {
        return new ESMTPBuilder()
                .setStatus(Code.ESMTP_STATUS.INTERNAL_SERVER_ERROR)
                .setMessage("Command not understood")
                .build();
    }

}
