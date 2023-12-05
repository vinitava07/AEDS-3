package dao;

import model.RSA;

import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;

public class RSADAO {

    private RSA rsa;

    public void criptografar() {

        try (RandomAccessFile raf = new RandomAccessFile("../resources/ListaAnimeBin.bin", "rw")) {
            StringBuilder sb = new StringBuilder();
            String line;
            System.out.println("lendo arq");
            while ((line = raf.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
//            rsa = new RSA(sb.toString());
            BigInteger myKey = rsa.getPublicKey();
            RandomAccessFile raf2 = new RandomAccessFile("../resources/AnimeCripto.bin", "rw");
//            rsa.cipherMessage(myKey);
//            for (int i = 0; i < rsa.getcMessage().length; i++) {
//                System.out.println("len: " + rsa.getcMessage()[i].toByteArray().length);
//                System.out.println("num: " + rsa.getcMessage()[i]);
//
//            }
            byte[] bArray;
            System.out.println("escrevendo comp");
            for (int i = 0; i < rsa.getcMessage().length; i++) {
                bArray = rsa.getcMessage()[i].toByteArray();
                raf2.write((byte) bArray.length);
                raf2.write(rsa.getcMessage()[i].toByteArray());
            }
            raf2.write(-1);
            RandomAccessFile raf3 = new RandomAccessFile("../resources/AnimeDescripto.bin", "rw");

            ArrayList<BigInteger> arrayList = new ArrayList<>();
            byte size;
            byte[] bigInt;
            System.out.println("escrevendp descomp:");
            raf2.seek(0);
            while ((size = raf2.readByte()) != -1) {
//                size = raf2.readByte();
                bigInt = new byte[size];
                raf2.read(bigInt, 0, size);
                arrayList.add(new BigInteger(bigInt));

            }
            String teste = "";
            BigInteger[] n = new BigInteger[arrayList.size()];
            for (int i = 0; i < arrayList.size(); i++) {
                n[i] = arrayList.get(i);
            }
            teste = rsa.uncipherMessage((n)).toString();
            raf3.write(teste.getBytes());


//            StringBuilder cypherTxt = new StringBuilder();
//            while ((line = raf2.readLine()) != null) {
//                cypherTxt.append(line);
//                cypherTxt.append('\n');
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
