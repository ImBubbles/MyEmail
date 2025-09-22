package dev.haydenholmes.email;

// This is for CC / BCC support
public record Recipient(String address, boolean blind)
{}