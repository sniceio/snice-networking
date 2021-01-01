package io.snice.networking.examples.vplmn.fsm.users.user;

public interface UserEvent {

    class Surf {
        @Override
        public String toString() {
            return "SURF";
        }
    }

    class Idle {
        @Override
        public String toString() {
            return "IDLE";
        }
    }

    class AirplaneMode {
        @Override
        public String toString() {
            return "AIRPLANE_MODE";
        }
    }

    class TurnOn {
        @Override
        public String toString() {
            return "TURN_ON";
        }
    }

    class TurnOff {
        @Override
        public String toString() {
            return "TURN_OFF";
        }
    }

    class Bye {
        @Override
        public String toString() {
            return "BYE";
        }
    }
}
