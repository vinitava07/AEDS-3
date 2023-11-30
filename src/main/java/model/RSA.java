package model;

import util.ProgressMonitor;

import java.math.BigInteger;

public class RSA {
    private BigInteger publicKey; // E (e * d) mod z == 1; algoritimo de euclides
    private BigInteger privateKey; // D (primo em relação a z)
    private BigInteger p; // primo grande n1
    private BigInteger q; // primo grande n2
    private BigInteger n; // p * q
    private BigInteger z; // (p-1) * (q-1)
    private String message;
    private BigInteger[] cMessage;
    private long cMessageSize;
    // 329999
    // 300043

    public RSA() {
        publicKey = new BigInteger("0");
        privateKey = new BigInteger("0");
        cMessage = null;
        calculateNumbers();
    }

    public BigInteger[] getcMessage() {
        return cMessage;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public long getcMessageSize() {
        return cMessageSize;
    }

    public void setcMessage(BigInteger[] cMessage) {
        this.cMessage = cMessage;
    }

    public void cipherMessage(BigInteger pubKey, String uMessage) {
        message = uMessage;
        cMessage = new BigInteger[uMessage.length()];
        ProgressMonitor progressMonitor = new ProgressMonitor("Criptografando mensagem: ");
        progressMonitor.start();
        for (int i = 0; i < message.length(); i++) {
            cMessage[i] = new BigInteger(String.valueOf(Integer.valueOf(message.charAt(i)))).modPow(pubKey, n);
        }
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Mensagem criptografada");
        cMessageSize = cMessage.length;

    }

    public StringBuilder uncipherMessage(BigInteger[] cipher) {
        if (cMessage == null) {
            System.out.println("Não há mensagem criptografada");
            return null;
        }
        ProgressMonitor progressMonitor = new ProgressMonitor("Descriptografando mensagem: ");
        progressMonitor.start();
        StringBuilder uncMessage = new StringBuilder();
        for (int i = 0; i < cMessage.length; i++) {
            uncMessage.append((char) (cMessage[i].modPow(privateKey, n).intValue()));
        }
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uncMessage;

    }

    private void calculateNumbers() {
        p = new BigInteger("300043");
        q = new BigInteger("329999");
        n = p.multiply(q);
        z = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        privateKey = createPrivateKey();
//        System.out.println("PrivateK: " + privateKey.toString(0));
        publicKey = createPublicKey();
        System.out.println("Sua chave pública gerada foi: " + publicKey);

    }

    private BigInteger createPublicKey() {

        BigInteger e = null;
        e = privateKey.modInverse(z);
        return e;
    }

    private BigInteger createPrivateKey() {

        BigInteger coprime = null;
        BigInteger aux = z;
        int cont = 0;
        BigInteger b = new BigInteger(z.toString(0));

        for (BigInteger i = new BigInteger("2"); cont != 5670 && i.compareTo(aux) < 1; i = i.add(BigInteger.ONE)) {

            if (isCoprime(i, b)) {
                cont += 1;
                coprime = new BigInteger(i.toString(0));
            }

        }
        return coprime;
    }

    private boolean isCoprime(BigInteger a, BigInteger b) {

        while (!b.equals(BigInteger.ZERO)) {
            BigInteger temp = b;
            b = a.mod(b);
            a = temp;
        }
        if (a.equals(BigInteger.ONE)) {
            return true;
        }
        return false;
    }

}
