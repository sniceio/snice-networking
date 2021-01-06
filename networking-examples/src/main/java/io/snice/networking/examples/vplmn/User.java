package io.snice.networking.examples.vplmn;

import io.snice.networking.examples.vplmn.fsm.users.user.AliceFsm;

public interface User {

    /**
     * Just a human readable name of the eventual {@link User}. Only used for logging etc.
     */
    String getName();

    Profile getProfile();

    Device getDevice();

    /**
     * The {@link SimCard} that is being used by this {@link Persona} and is what is used
     * with the actual {@link Device}.
     */
    SimCard getSimCard();

    Profile ALICE = new Profile() {
        @Override
        public Persona getPersona() {
            return Persona.ALICE;
        }

        @Override
        public Device.Type getDeviceType() {
            return Device.Type.IPHONE;
        }

        @Override
        public String toString() {
            return " Profile [" + getPersona() + ", " + getDeviceType() + "]";
        }
    };

    Profile SCOOTER = new Profile() {
        @Override
        public Persona getPersona() {
            return Persona.SCOOTER;
        }

        @Override
        public Device.Type getDeviceType() {
            return Device.Type.BG96;
        }

        @Override
        public String toString() {
            return " Profile [" + getPersona() + ", " + getDeviceType() + "]";
        }
    };

    enum Persona {
        ALICE, SCOOTER;
    }

    interface Profile {

        /**
         * The {@link Persona} controls the behavior of the {@link User}, which is ultimately
         * implemented as a state machine. E.g., the Alice persona is implemented in the {@link AliceFsm}.
         */
        Persona getPersona();

        /**
         * The type of {@link Device} that the persona is using. Various devices has different behavior
         * and capabilities and therefore, somewhat dictates what the {@link Persona} can/can't do.
         * Some devices are also misbehaving.
         */
        Device.Type getDeviceType();
    }
}
