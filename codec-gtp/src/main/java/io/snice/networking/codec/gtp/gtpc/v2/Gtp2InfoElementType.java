package io.snice.networking.codec.gtp.gtpc.v2;

public enum Gtp2InfoElementType {

    RESERVED(0, "Reserved", false, -1),
    IMSI(1, "International Mobile Subscriber Identity", true, -1),
    EBI(73, "EPS Bearer ID", true, -1);

    private final int typeAsDecimal;
    private final byte type;

    private final String friendlyName;

    private final boolean isFixed;
    private final boolean isExtendable;

    Gtp2InfoElementType(final int type, final String friendlyName, final boolean isExtendable, final int octets) {
        this.typeAsDecimal = type;
        this.type = (byte) type;
        this.friendlyName = friendlyName;
        this.isFixed = octets > 0;
        this.isExtendable = isExtendable;
    }

    public int getTypeAsDecimal() {
        return typeAsDecimal;
    }

    public byte getType() {
        return type;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean isVariable() {
        return !isFixed;
    }

    public boolean isExtendable() {
        return isExtendable;
    }
}
