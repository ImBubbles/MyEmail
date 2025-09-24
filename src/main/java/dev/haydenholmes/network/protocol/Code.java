package dev.haydenholmes.network.protocol;

import dev.haydenholmes.util.StringCast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Code {

    public enum SMTP_STATUS {
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

        SMTP_STATUS(int code) {
            this.code=code;
        }

        public int getCode() {
            return code;
        }

        public static SMTP_STATUS fromInt(int val) {
            return Arrays.stream(SMTP_STATUS.values()).filter(smtpStatus -> smtpStatus.getCode() == val).findFirst().orElse(null);
        }

        public static SMTP_STATUS fromString(String str) {
            int numVals = 0;

            for(char ch : str.toCharArray()) {
                boolean valid = StringCast.toInteger(String.valueOf(ch)) != null;
                if(!valid)
                    break;
                numVals++;
            }

            if(numVals == 0)
                return null;

            Integer integer = StringCast.toInteger(str.substring(0, numVals));
            if(integer == null)
                return null;

            int code = integer;
            return fromInt(code);

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

    public enum SMTP_COMMANDS {
        EHLO("EHLO"),
        HELO("HELO"),
        MAIL("MAIL"),
        MAIL_FROM("MAIL FROM"),
        RCPT("RCPT"),
        RCPT_TO("RCPT TO"),
        DATA("DATA"),
        QUIT("QUIT"),
        RSET("RSET"),
        AUTH("AUTH"),
        STARTTLS("STARTTLS");

        private final String protocol;

        SMTP_COMMANDS(String protocol) {
            this.protocol=protocol;
        }

        public String value() {
            return protocol;
        }

    }

    public enum SMTP_BODY {
        _7BIT("7BIT"),
        _8BITMIME("8BITMIME");

        private final String value;

        SMTP_BODY(String value) {
            this.value=value;
        }

        public String value() {
            return value;
        }

        public static SMTP_BODY parseString(String str) {
            for(SMTP_BODY body : SMTP_BODY.values()) {
                if(body.value.equalsIgnoreCase(str)) {
                    return body;
                }
            }
            return null;
        }
    }

    public enum SMTP_NOTIFY {
        SUCCESS,
        FAILURE,
        DELAY;

        public static Set<SMTP_NOTIFY> parseString(String str) {
            Set<SMTP_NOTIFY> result = new HashSet<>();
            String[] regex = str.split(",");
            regex:
            for(String r : regex) {
                r = r.trim();
                notif:
                    for(SMTP_NOTIFY notify : SMTP_NOTIFY.values()) {
                        if(notify.name().equalsIgnoreCase(r)) {
                            result.add(notify);
                            continue regex;
                        }
                    }
            }
            return result;
        }

    }

    public enum SMTP_ORCPT {
        RFC822,
        X_TEXT("X-TEXT");

        private final String value;

        SMTP_ORCPT() {
            this.value=name();
        }
        SMTP_ORCPT(String value) {
            this.value=value;
        }

        public static SMTP_ORCPT parseString(String str) {
            for(SMTP_ORCPT orcpt : SMTP_ORCPT.values()) {
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

    public enum SEQUENCE_STATE {
        DEAD,
        HELO, // Initial state, expecting HELO or EHLO
        AUTH,
        MAIL_FROM, // After HELO/EHLO, expecting MAIL FROM
        RCPT_TO, // After MAIL FROM, expecting RCPT TO or another RCPT TO
        DATA; // Even though DATA can be sent while in the RCPT state, this is to ensure sequence is kept and no more RCPT_TO can be sent
    }

}
