package dev.haydenholmes.email;

import dev.haydenholmes.network.protocol.Code;

import java.util.Set;

// This is for CC / BCC support
public record Recipient(String address, boolean blind, Set<Code.SMTP_NOTIFY> notifications, Code.SMTP_ORCPT ORCPT, String orcptAddress) {}