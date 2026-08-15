package net.akive.api.common.domain;

import java.util.UUID;

public class ClientInstanceIdFixture {

    public static ClientInstanceId createClientInstanceId() {
        return new ClientInstanceId(UUID.randomUUID());
    }
}
