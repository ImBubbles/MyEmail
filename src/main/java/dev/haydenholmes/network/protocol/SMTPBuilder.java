package dev.haydenholmes.network.protocol;

import dev.haydenholmes.MyEmail;

public final class SMTPBuilder extends SBWrapper {

    public SMTPBuilder status(Code.ESMTP_STATUS status) {
        return status(status, false);
    }

    public SMTPBuilder status(Code.ESMTP_STATUS status, boolean multiline) {
        if(status!=null) {
            if(!multiline) {
                append(String.valueOf(status.getCode()), true);
            } else {
                append(String.valueOf(status.getCode()), false);
                append("-");
            }
        }
        return this;
    }

    public SMTPBuilder protocol(Code.PROTOCOL protocol) {
        if(protocol!=null)
            append(protocol.value(), true);
        return this;
    }

    public SMTPBuilder domain() {
        append(MyEmail.properties.DOMAIN(), true);
        return this;
    }

    public SMTPBuilder message(String message) {
        if(message!=null && !message.isBlank())
            append(message);
        return this;
    }

}
