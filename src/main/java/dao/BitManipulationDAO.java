package dao;

import java.io.File;
import java.io.RandomAccessFile;

class BitManipulationDAO {
    int[] bitArray = new int[8];
    int bite;
    int controlReadBit = 0;

    public void readByte(RandomAccessFile raf) {
        try {
            bite = raf.readByte();
            for (int j = 7; j >= 0; j--) {
                bitArray[j] = bite & 0b1;
                bite = bite >> (1);
                System.out.println(bite);
                System.out.println("b[i] " + bitArray[j]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int readBit(RandomAccessFile raf) {
        int bit = -1;
        if (controlReadBit == 8) {
            controlReadBit = 0;
        }
        try {
            if (controlReadBit == 0) {

                bite = raf.readByte();
                for (int j = 7; j >= 0; j--) {
                    bitArray[j] = bite & 0b1;
                    bite = bite >> (1);
                    // System.out.println(bite);
                    // System.out.println("b[i] " + bitArray[j]);
                }
            }
            bit = bitArray[controlReadBit++];

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bit;
    }

    public void writeBytes(RandomAccessFile raf, String bits) {

        try {
            byte[] b = new byte[1];
            b[0] = 0b0;
            byte a = 0;
            int control = 0;
            Integer aux;
            // aux = Integer.valueOf(bits);
            // bits.length() / 8
            for (int j = 0; j < bits.length() / 8; j++) {
                for (int i = control; i < control + 8; i++) {
                    b[0] = (byte) (b[0] << 1);
                    if (bits.charAt(i) == '1') {
                        b[0] = (byte) (b[0] | 1);
                    } else {
                        b[0] = (byte) (b[0] | 0);
                    }

                }
                // if (bits.charAt(control + 7) == '1') {
                // b[0] = (byte) (b[0] | 1);
                // } else {
                // b[0] = (byte) (b[0] | 0);
                // }
                // b[0] = (byte) (b[0] >> 1);

                control += 8;
//                String s1 = String.format("%8s", Integer.toBinaryString(b[0] &
//                        0xFF)).replace(' ', '0');
//                System.out.print(s1);
                // System.out.println(bits.charAt(control-1));
                raf.write(b[0]);
                b[0] = 0b0;
            }
            // String s1 = String.format("%8s", Integer.toBinaryString(b[0] &
            // 0xFF)).replace(' ', '0');
            // System.out.println(s1);
            // System.out.println(b[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
