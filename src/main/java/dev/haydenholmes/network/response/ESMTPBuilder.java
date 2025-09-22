package dev.haydenholmes.network.response;

import dev.haydenholmes.MyEmail;

public class ESMTPBuilder extends ResponseBuilder {

    private Code.ESMTP_STATUS status = null;
    private Code.PROTOCOL protocol = null;
    private boolean domain = false;
    private String message = "";

    public ESMTPBuilder setStatus(Code.ESMTP_STATUS status) {
        this.status = status;
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
            builder.append(String.valueOf(status.getCode()), true);
        }
        if(domain) {
            builder.append(MyEmail.properties.domain(), true);
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
