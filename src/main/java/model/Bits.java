package model;


import java.util.ArrayList;

public class Bits extends ByteBits {
    private ArrayList<Byte> bitsArray;
    private long readAmount;
    public Bits() {
        super();
        readAmount = 1;
        bitsArray = new ArrayList<>();
        bitsArray.add((byte) 0);
    }

    public void setBitsArray(byte[] bitsArray) {
        this.bitsArray.clear();
        for (int i = 0; i < bitsArray.length; i++) {
            this.bitsArray.add(bitsArray[i]);
        }
    }

    public byte[] getBitsArray() {
        byte[] bytes = new byte[bitsArray.size()];
        for (int i = 0; i < bitsArray.size(); i++) {
            bytes[i] = bitsArray.get(i);
        }
        return bytes;
    }

    public void addBit(int bit) {
        if(bit != 0 && bit != 1) {
            System.out.println("ERROR!!!");
        } else {
            this.addBit((byte) bit);
        }
        if(isFull()) {
            bitsArray.add(this.bits);
            reSetBits();
        }
    }

    private void build() {
        adjustBits();
        bitsArray.add(this.bits);
    }
    public byte[] getFinalArray() {
        bitsArray.set(0, (byte) (8 - amount));
        if(!empty()) build();
        byte[] bytes = new byte[bitsArray.size()];
        for (int i = 0; i < bitsArray.size(); i++) {
            bytes[i] = bitsArray.get(i);
        }
        return bytes;
    }

    public void print() {
        for (Byte b :
                bitsArray) {
            StringBuilder string;

            string = new StringBuilder(Integer.toUnsignedString(b, 2));
            if (string.length() < 8) {
                for (int i = string.length(); i < 8; i++) {
                    string.insert(0, '0');
                }
            } else {
                if (string.length() > 8) {
                    string.delete(0 , string.length() - 8);
                }
            }

            System.out.printf("%8s" , string);
        }
        System.out.println();
    }
    public boolean canRead() {
        return readAmount < (bitsArray.size() - 1);
    }
    public byte readByte() {
        return bitsArray.get((int) ++readAmount);
    }

    public byte get(long index) {
        long x = index / 8;
        int i = (int) (index % 8);
        byte b = this.getBitsArray()[(int) x + 1];
//        System.out.println(Integer.toUnsignedString(b & 0b11111111, 2));
        b >>>= (7 - i);
        b &= 0b0001;

        return b;
    }
}

class ByteBits {
    protected byte bits;
    public byte amount;
    private boolean full;
    protected ByteBits() {
        bits = 0;
        amount = 0;
        full = false;
    }
    protected void addBit(byte bit) {
        if(!full) {
            bits <<= 1;
            bits |= bit;
            full = (++amount == 8);
        } else {
            System.out.println("ERROR!!! byte full!");
        }
    }
    protected void adjustBits() {
        bits <<= (8 - amount);
        amount = 0;
    }
    protected void reSetBits() {
        bits = 0;
        full = false;
        amount = 0;
    }
    protected boolean isFull() {
        return full;
    }
    protected boolean empty() {
        return amount == 0;
    }
}