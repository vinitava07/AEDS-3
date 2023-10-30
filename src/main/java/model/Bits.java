package model;


import java.util.ArrayList;

public class Bits extends ByteBits {

    private ArrayList<Byte> bitsArray;
    public Bits() {
        super();
        bitsArray = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            bitsArray.add((byte) 0);
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

    public void build() {
        if (!empty()) {
            adjustBits();
            bitsArray.add(this.bits);
        }
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