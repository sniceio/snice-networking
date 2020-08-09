package io.snice.networking.diameter.event.impl;

import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.networking.diameter.event.DiameterMessageEvent;
import io.snice.preconditions.PreConditions;

public class DefaultDiameterMessageEvent implements DiameterMessageEvent {

    private final DiameterMessage msg;

    public static DiameterMessageEvent of(final DiameterMessage msg) {
        PreConditions.assertNotNull(msg, "The diameter message cannot be null");
        return new DefaultDiameterMessageEvent(msg);
    }

    private DefaultDiameterMessageEvent(final DiameterMessage msg) {
        this.msg = msg;
    }

    @Override
    public DiameterMessage getMessage() {
        return msg;
    }

    @Override
    public DiameterRequest getRequest() {
        return msg.toRequest();
    }

    @Override
    public DiameterAnswer getAnswer() {
        return msg.toAnswer();
    }

    @Override
    public boolean isRequest() {
        return msg.isRequest();
    }

    @Override
    public boolean isAnswer() {
        return isAnswer();
    }

    @Override
    public boolean isULR() {
        return msg.isULR();
    }

    @Override
    public boolean isULA() {
        return msg.isULA();
    }

    @Override
    public boolean isDWR() {
        return msg.isDWR();
    }

    @Override
    public boolean isDWA() {
        return msg.isDWA();
    }

    @Override
    public boolean isDPR() {
        return msg.isDPR();
    }

    @Override
    public boolean isDPA() {
        return msg.isDPA();
    }

    @Override
    public boolean isCER() {
        return msg.isCER();
    }

    @Override
    public boolean isCEA() {
        return msg.isCEA();
    }
}
