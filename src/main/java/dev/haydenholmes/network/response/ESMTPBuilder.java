package dev.haydenholmes.network.response;

import dev.haydenholmes.MyEmail;

public final class ESMTPBuilder extends ResponseBuilder {

    private Code.ESMTP_STATUS status = null;
    private boolean multiline = false;
    private Code.PROTOCOL protocol = null;
    private boolean domain = false;
    private String message = "";

    public ESMTPBuilder setStatus(Code.ESMTP_STATUS status) {
        return setStatus(status, false);
    }

    public ESMTPBuilder setStatus(Code.ESMTP_STATUS status, boolean multiline) {
        this.status = status;
        this.multiline = multiline;
        return this;
    }

    public ESMTPBuilder setProtocol(Code.PROTOCOL protocol) {
        this.protocol = protocol;
        return this;
    }

    public ESMTPBuilder sendDomain(boolean val) {
        this.domain = val;
        return this;
    }

    public ESMTPBuilder setMessage(String message) {
        this.message=message;
        return this;
    }

    @Override
    public String build() {
        SBWrapper builder = new SBWrapper();
        if(status != null) {
            if(!multiline) {
                builder.append(String.valueOf(status.getCode()), true);
            } else {
                builder.append(String.valueOf(status.getCode()), false);
                builder.append("-");
            }
        }
        if(domain) {
            builder.append(MyEmail.properties.DOMAIN(), true);
        }
        if(protocol != null) {
            builder.append(protocol.value(), true);
        }
        if(!message.isBlank()) {
            builder.append(message);
        }
        return builder.get();
    }

}
