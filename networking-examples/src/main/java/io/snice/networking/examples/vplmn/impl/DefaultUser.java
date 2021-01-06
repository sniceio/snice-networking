package io.snice.networking.examples.vplmn.impl;

import io.hektor.core.ActorRef;
import io.snice.networking.examples.vplmn.Device;
import io.snice.networking.examples.vplmn.SimCard;
import io.snice.networking.examples.vplmn.User;

public class DefaultUser implements User {

    private final ActorRef self;
    private final String name;
    private final Profile profile;
    private final Device device;
    private final SimCard simCard;


    public DefaultUser(final ActorRef self, final String name, final Profile profile, final Device device, final SimCard simCard) {
        this.self = self;
        this.name = name;
        this.profile = profile;
        this.device = device;
        this.simCard = simCard;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public SimCard getSimCard() {
        return simCard;
    }
}
