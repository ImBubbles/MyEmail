package dev.haydenholmes.network.protocol;

import dev.haydenholmes.MyEmail;

public final class SMTPBuilder extends SBWrapper {

    public SMTPBuilder status(Code.SMTP_STATUS status) {
        return status(status, false);
    }

    public SMTPBuilder status(Code.SMTP_STATUS status, boolean multiline) {
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

    public SMTPBuilder command(Code.SMTP_COMMANDS command) {
        return command(command, false);
    }

    public SMTPBuilder command(Code.SMTP_COMMANDS command, boolean semicolon) {
        if(command!=null) {
            if(!semicolon)
                append(command.value(), true);
            else {
                append(command.value(), false);
                append(":", false);
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
