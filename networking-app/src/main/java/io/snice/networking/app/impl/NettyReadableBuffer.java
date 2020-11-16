package io.snice.networking.app.impl;

import io.netty.buffer.ByteBuf;
import io.snice.buffer.Buffer;
import io.snice.buffer.ByteNotFoundException;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.WritableBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class NettyReadableBuffer implements ReadableBuffer {

    private final ByteBuf buffer;

    public static NettyReadableBuffer of(final ByteBuf buffer) {
        return new NettyReadableBuffer(buffer);
    }

    private NettyReadableBuffer(final ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getReaderIndex() {
        return buffer.readerIndex();
    }

    @Override
    public ReadableBuffer setReaderIndex(final int index) {
        buffer.readerIndex(index);
        return this;
    }

    @Override
    public ReadableBuffer markReaderIndex() {
        buffer.markReaderIndex();
        return this;
    }

    @Override
    public ReadableBuffer resetReaderIndex() {
        buffer.resetReaderIndex();
        return this;
    }

    @Override
    public byte readByte() throws IndexOutOfBoundsException {
        return buffer.readByte();
    }

    @Override
    public byte peekByte() throws IndexOutOfBoundsException {
        buffer.markReaderIndex();
        final byte b = buffer.readByte();
        buffer.resetReaderIndex();
        return b;
    }

    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        return buffer.readUnsignedInt();
    }

    @Override
    public int readInt() throws IndexOutOfBoundsException {
        return buffer.readInt();
    }

    @Override
    public int readIntFromThreeOctets() throws IndexOutOfBoundsException {
        final byte a = buffer.readByte();
        final byte b = buffer.readByte();
        final byte c = buffer.readByte();
        return Buffer.signedInt(a, b, c);
    }

    @Override
    public long readLong() throws IndexOutOfBoundsException {
        return buffer.readLong();
    }

    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException {
        return NettyReadableBuffer.of(buffer.readBytes(length));
    }

    @Override
    public Buffer readLine() {
        throw new RuntimeException("ReadLine not yet implemented");
    }

    @Override
    public Buffer readUntilSingleCRLF() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer readUntilDoubleCRLF() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int getReadableBytes() {
        return buffer.readableBytes();
    }

    @Override
    public boolean hasReadableBytes() {
        return buffer.readableBytes() > 0;
    }

    @Override
    public Buffer readUntilWhiteSpace() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer readUntil(final byte b) throws ByteNotFoundException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer readUntil(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer readUntilSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int readUnsignedShort() throws IndexOutOfBoundsException {
        return buffer.readUnsignedShort();
    }

    @Override
    public short readShort() throws IndexOutOfBoundsException {
        return buffer.readShort();
    }

    @Override
    public short readUnsignedByte() throws IndexOutOfBoundsException {
        return buffer.readUnsignedByte();
    }

    @Override
    public Object clone() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int countWhiteSpace(final int startIndex) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer toBuffer() {
        return Buffer.of(sliceToSize());
    }

    @Override
    public byte[] getContent() {
        throw new RuntimeException("not yet implemented");
    }

    private byte[] sliceToSize() {
        final int size = buffer.readableBytes();
        buffer.markReaderIndex();
        final byte[] b = new byte[size];
        buffer.readBytes(b);
        buffer.resetReaderIndex();
        return b;
    }

    @Override
    public ReadableBuffer toReadableBuffer() {
        return toBuffer().toReadableBuffer();
    }

    @Override
    public WritableBuffer toWritableBuffer() {
        final WritableBuffer buf = WritableBuffer.of(sliceToSize());
        buf.fastForwardWriterIndex();
        return buf;
    }

    @Override
    public int indexOfSingleCRLF() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer indexOfDoubleCRLF() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean isEmpty() {
        return buffer.readableBytes() == 0;
    }

    @Override
    public int capacity() {
        return buffer.capacity();
    }

    @Override
    public int indexdOfSafe(final int maxBytes, final byte... bytes) throws IllegalArgumentException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int indexOf(final int startIndex, final int maxBytes, final byte... bytes) throws ByteNotFoundException, IllegalArgumentException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int indexOf(final byte b) throws ByteNotFoundException, IllegalArgumentException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int countOccurences(final int startIndex, final int maxBytes, final byte b) throws IndexOutOfBoundsException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public void writeTo(final WritableBuffer out) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer slice(final int start, final int stop) throws IndexOutOfBoundsException, IllegalArgumentException {
        return NettyReadableBuffer.of(buffer.slice(start, stop));
        // throw new RuntimeException("not yet implemented");
    }

    @Override
    public Buffer slice(final int stop) {
        return NettyReadableBuffer.of(buffer.slice(buffer.readerIndex(), stop));

    }

    @Override
    public Buffer slice() {
        return NettyReadableBuffer.of(buffer.slice());
    }

    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        return buffer.getByte(index);
    }

    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        return buffer.getInt(index);
    }

    @Override
    public long getLong(final int index) throws IndexOutOfBoundsException {
        return buffer.getLong(index);
    }

    @Override
    public int getIntFromThreeOctets(final int index) throws IndexOutOfBoundsException {
        final byte a = buffer.getByte(index);
        final byte b = buffer.getByte(index + 1);
        final byte c = buffer.getByte(index + 2);
        return Buffer.signedInt(a, b, c);
    }

    @Override
    public long getLongFromFiveOctets(final int index) throws IndexOutOfBoundsException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public long getUnsignedInt(final int index) throws IndexOutOfBoundsException {
        return buffer.getUnsignedInt(index);
    }

    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        return buffer.getShort(index);
    }

    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        return buffer.getUnsignedShort(index);
    }

    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        return buffer.getUnsignedByte(index);
    }

    @Override
    public int parseToInt() throws NumberFormatException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public int parseToInt(final int radix) throws NumberFormatException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean endsWith(final byte[] content) throws IllegalArgumentException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean endsWith(final byte b) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean endsWith(final byte b1, final byte b2, final byte b3, final byte b4) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public String dumpAsHex() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public boolean equalsIgnoreCase(final Object b) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public String toUTF8String() {
        throw new RuntimeException("not yet implemented");
    }
}
