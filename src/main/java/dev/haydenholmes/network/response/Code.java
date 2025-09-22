package dev.haydenholmes.network.response;

public class Code {

    public enum ESMTP_STATUS {
        OK(200),
        READY(220),
        ACKNOWLEDGE(250),
        NOT_FOUND(404),
        UNAVAILABLE(421),
        INTERNAL_SERVER_ERROR(500),
        BAD_SYNTAX(501),
        BAD_SEQUENCE(503),
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
        MAIL_FROM("MAIL FROM"),
        RCPT_TO("RCPT TO"),
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

}
