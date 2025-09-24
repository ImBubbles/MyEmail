package dev.haydenholmes.email;

import dev.haydenholmes.network.protocol.Code;

import java.util.Set;

// This is for CC / BCC support
public record Recipient(String address, boolean blind, Set<Code.ESMTP_NOTIFY> notifications, Code.ESMTP_ORCPT ORCPT, String orcptAddress) {}