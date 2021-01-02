package io.snice.networking.examples.vplmn.fsm.users.user;

import java.time.Duration;

public interface UserEvent {

    TurnOn TURN_ON = new TurnOn();

    class Surf {
        private final int count;
        private final Duration timeInBetween;

        public Surf(final int count, final Duration timInBetween) {
            this.count = count < 0 ? 0 : count;
            this.timeInBetween = timInBetween == null ? Duration.ofMillis(100) : timInBetween;
        }

        public Surf decrement() {
            return new Surf(count - 1, timeInBetween);
        }

        public boolean surfsUp() {
            return count == 0;
        }

        public Duration getTimeInBetween() {
            return timeInBetween;
        }

        @Override
        public String toString() {
            return "SURF [" + count + ", " + timeInBetween + "]";
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
