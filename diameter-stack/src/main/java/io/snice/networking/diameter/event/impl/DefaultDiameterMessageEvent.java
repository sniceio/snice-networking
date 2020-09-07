package io.snice.networking.diameter.event.impl;

import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.networking.diameter.event.DiameterMessageEvent;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;
import io.snice.networking.diameter.event.DiameterMessageWriteEvent;
import io.snice.networking.diameter.tx.Transaction;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultDiameterMessageEvent implements DiameterMessageEvent {

    private final DiameterMessage msg;
    private final Optional<Transaction> transaction;

    public static DiameterMessageWriteEvent newWriteEvent(final DiameterMessage msg) {
        assertNotNull(msg, "The diameter message cannot be null");
        return new MessageWriteEvent(msg);
    }

    public static DiameterMessageWriteEvent newWriteEvent(final Transaction transaction) {
        assertNotNull(transaction, "The transaction cannot be null");
        return new MessageWriteEvent(transaction);
    }

    public static DiameterMessageReadEvent newReadEvent(final DiameterMessage msg) {
        assertNotNull(msg, "The diameter message cannot be null");
        return new MessageReadEvent(msg);
    }

    public static DiameterMessageReadEvent newReadEvent(final Transaction transaction) {
        assertNotNull(transaction, "The transaction cannot be null");
        return new MessageReadEvent(transaction);
    }

    public static DiameterMessageReadEvent newReadEvent(final DiameterMessage msg, final Transaction transaction) {
        assertNotNull(msg, "The diameter message cannot be null");
        assertNotNull(transaction, "The transaction cannot be null");
        return new MessageReadEvent(msg, transaction);
    }

    @Override
    public Optional<Transaction> getTransaction() {
        return transaction;
    }

    private DefaultDiameterMessageEvent(final DiameterMessage msg) {
        this.msg = msg;
        transaction = Optional.empty();
    }

    /**
     * Constructor used when the given message is associated with the given transaction but is not
     * the message that created the transaction. The normal use case is of course when we receive a
     * {@link DiameterAnswer} that is matching an existing {@link Transaction} so these needs to be
     * "kept" together.
     *
     * @param msg
     * @param transaction
     */
    private DefaultDiameterMessageEvent(final DiameterMessage msg, final Transaction transaction) {
        this.msg = msg;
        this.transaction = Optional.of(transaction);
    }

    private DefaultDiameterMessageEvent(final Transaction transaction) {
        this.msg = transaction.getRequest();
        this.transaction = Optional.of(transaction);
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

    private static class MessageWriteEvent extends DefaultDiameterMessageEvent implements DiameterMessageWriteEvent {

        private MessageWriteEvent(final DiameterMessage msg) {
            super(msg);
        }

        private MessageWriteEvent(final Transaction transaction) {
            super(transaction);
        }
    }

    private static class MessageReadEvent extends DefaultDiameterMessageEvent implements DiameterMessageReadEvent {

        private MessageReadEvent(final DiameterMessage msg, final Transaction transaction) {
            super(msg, transaction);
        }

        private MessageReadEvent(final DiameterMessage msg) {
            super(msg);
        }

        private MessageReadEvent(final Transaction transaction) {
            super(transaction);
        }
    }
}
