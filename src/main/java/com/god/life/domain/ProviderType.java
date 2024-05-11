package com.god.life.domain;

public enum ProviderType {
    KAKAO("KAKAO");

    private final String providerName;

    ProviderType(String providerName) {
        this.providerName = providerName;
    }
}
