package io.snice.networking.diameter.peer;

public class PeerConfiguration {

    /**
     * Options for configuring the internal map of outstanding transactions.
     * In general, we would like to avoid re-hashing the internal tables since
     * it can be quite costly, specially at larger sizes. Therefore, ideally, we should
     * figure out an appropriate size so it is highly unlikely that we will ever re-hash.
     * Since that is dependent on a lot of different factors, it is impossible to have
     * a sane default value and therefore, it is configurable per peer.
     *
     * Remember, memory is cheap and typically you don't have that many peers.
     *
     * @return
     */
    public int getPeerTransactionTableInitialSize() {
        return 100;
    }

}
