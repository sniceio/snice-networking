package io.snice.networking.examples.vplmn.fsm.users;

import io.snice.networking.examples.vplmn.User;

public interface UserManagerEvent {

    class Init {
        @Override
        public String toString() {
            return "INIT";
        }
    }

    class AddUser {
        public final String name;
        public final User.Profile profile;

        public AddUser(final String name, final User.Profile profile) {
            this.name = name;
            this.profile = profile;
        }

        @Override
        public String toString() {
            return "AddUser [" + name + ", " + profile + "]";
        }
    }

    class Terminate {
        @Override
        public String toString() {
            return "TERMINATE";
        }
    }

    class Terminated {
        @Override
        public String toString() {
            return "TERMINATED";
        }
    }
}
