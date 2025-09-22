package dev.haydenholmes.network.response;

import java.util.HashSet;
import java.util.Set;

public final class Code {

    public enum ESMTP_STATUS {
        OK(200),
        READY(220),
        QUIT(221),
        AUTH_SUCCESS(235),
        ACKNOWLEDGE(250),
        START_MAIL_INPUT(354),
        NOT_FOUND(404),
        UNAVAILABLE(421),
        INTERNAL_SERVER_ERROR(500),
        BAD_SYNTAX(501),
        BAD_SEQUENCE(503),
        AUTH_FAIL(535),
        FAILURE(554);

        private final int code;

        ESMTP_STATUS(int code) {
            this.code=code;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PROTOCOL {
        ESMTP("ESMTP"),
        SMTP("SMTP");

        private final String protocol;

        PROTOCOL(String protocol) {
            this.protocol=protocol;
        }

        public String value() {
            return protocol;
        }
    }

    public enum ESMTP_COMMANDS {
        EHLO("EHLO"),
        HELO("HELO"),
        MAIL_FROM("MAIL"),
        RCPT_TO("RCPT"),
        DATA("DATA"),
        QUIT("QUIT"),
        RSET("RSET"),
        AUTH("AUTH"),
        STARTTLS("STARTTLS");

        private final String protocol;

        ESMTP_COMMANDS(String protocol) {
            this.protocol=protocol;
        }

        public String value() {
            return protocol;
        }
    }

    public enum ESMTP_BODY {
        _7BIT("7BIT"),
        _8BITMIME("8BITMIME");

        private final String value;

        ESMTP_BODY(String value) {
            this.value=value;
        }

        public String value() {
            return value;
        }

        public static ESMTP_BODY parseString(String str) {
            for(ESMTP_BODY body : ESMTP_BODY.values()) {
                if(body.value.equalsIgnoreCase(str)) {
                    return body;
                }
            }
            return null;
        }
    }

    public enum ESMTP_NOTIFY {
        SUCCESS,
        FAILURE,
        DELAY;

        public static Set<ESMTP_NOTIFY> parseString(String str) {
            Set<ESMTP_NOTIFY> result = new HashSet<>();
            String[] regex = str.split(",");
            regex:
            for(String r : regex) {
                r = r.trim();
                notif:
                    for(ESMTP_NOTIFY notify : ESMTP_NOTIFY.values()) {
                        if(notify.name().equalsIgnoreCase(r)) {
                            result.add(notify);
                            continue regex;
                        }
                    }
            }
            return result;
        }
    }

    public enum ESMTP_ORCPT {
        RFC822,
        X_TEXT("X-TEXT");

        private final String value;

        ESMTP_ORCPT() {
            this.value=name();
        }
        ESMTP_ORCPT(String value) {
            this.value=value;
        }

        public static ESMTP_ORCPT parseString(String str) {
            for(ESMTP_ORCPT orcpt : ESMTP_ORCPT.values()) {
                if(orcpt.name().equalsIgnoreCase(str)) {
                    return orcpt;
                }
            }
            return null;
        }
    }

    public enum RECIPIENT_STATE {
        TO,
        CC,
        BCC;

        public static RECIPIENT_STATE parseString(String str) {
            for(RECIPIENT_STATE val : RECIPIENT_STATE.values()) {
                if(val.name().equalsIgnoreCase(str)) {
                    return val;
                }
            }
            return null;
        }
    }

}
