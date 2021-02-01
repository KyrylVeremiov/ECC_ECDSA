package com.company;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Test {
    static final BigInteger m1=new BigInteger("-1");
    static final String curve_name="secp256k1";

    static final X9ECParameters curveParams = ECNamedCurveTable.getByName(curve_name);
    static final ECCurve curve= curveParams.getCurve();

    static final ECDomainParameters domain= new ECDomainParameters(curveParams.getCurve(),curveParams.getG(),curveParams.getN(),curveParams.getH());
    static final ECParameterSpec parameterSpec= new ECParameterSpec(curveParams.getCurve(),curveParams.getG(),curveParams.getN(),curveParams.getH());
    static final ECPoint G=curveParams.getG();

    //      H may by any point
    static final ECPoint H=G.multiply(new BigInteger("3")).normalize();

//    static byte[] Hb= new byte[]{ 0x04,
//            0x50, (byte) 0x92, (byte) 0x9b, 0x74, (byte) 0xc1, (byte) 0xa0, 0x49, 0x54,
//            (byte) 0xb7, (byte) 0x8b, 0x4b, 0x60, 0x35, (byte) 0xe9, 0x7a, 0x5e,
//            0x07, (byte) 0x8a, 0x5a, 0x0f, 0x28, (byte) 0xec, (byte) 0x96, (byte) 0xd5,
//            0x47, (byte) 0xbf, (byte) 0xee, (byte) 0x9a, (byte) 0xce, (byte) 0x80, 0x3a, (byte) 0xc0,
//            0x31, (byte) 0xd3, (byte) 0xc6, (byte) 0x86, 0x39, 0x73, (byte) 0x92, 0x6e,
//            0x04, (byte) 0x9e, 0x63, 0x7c, (byte) 0xb1, (byte) 0xb5, (byte) 0xf4, 0x0a,
//            0x36, (byte) 0xda, (byte) 0xc2, (byte) 0x8a, (byte) 0xf1, 0x76, 0x69, 0x68,
//            (byte) 0xc3, 0x0c, 0x23, 0x13, (byte) 0xf3, (byte) 0xa3, (byte) 0x89, 0x04};
//
//    static ECPoint H=curve.decodePoint(Hb);


    private static BigInteger[] sign(final byte[] dataHash, final ECPrivateKeySpec privateKey) {
        final ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        final ECPrivateKeyParameters privateKeyParameters =
                new ECPrivateKeyParameters(privateKey.getD(), domain);

        signer.init(true, privateKeyParameters);

        return signer.generateSignature(dataHash);
    }
    private static boolean verify (final byte[] dataHash, BigInteger r,BigInteger s, final ECPublicKeySpec publicKey){
        final ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        final ECPublicKeyParameters publicKeyParameters =
                new ECPublicKeyParameters(publicKey.getQ(), domain);
        signer.init(false,publicKeyParameters);

        return signer.verifySignature(dataHash,r,s);
    }

    public static void main(String[] args) {
        curve.validatePoint(G.getXCoord().toBigInteger(),G.getYCoord().toBigInteger());
        curve.validatePoint(H.getXCoord().toBigInteger(),H.getYCoord().toBigInteger());



//1
        System.out.println("№1\n\n");

        BigInteger r1= new BigInteger("28");
        BigInteger v1= new BigInteger("3");
        ECPoint Gr1=G.multiply(r1);
        ECPoint Hv1=H.multiply(v1);
        ECPoint Y1=Gr1.add(Hv1);
        BigInteger Y1x=Y1.getXCoord().toBigInteger();
        BigInteger Y1y=Y1.getYCoord().toBigInteger();
        System.out.println("Y1:");
        System.out.println(Y1x);
        System.out.println(Y1y);

        BigInteger r2= new BigInteger("113");
        BigInteger v2= new BigInteger("3");
        ECPoint Gr2=G.multiply(r2).normalize();
        ECPoint Hv2=H.multiply(v2).normalize();
        ECPoint Y2=Gr2.add(Hv2).normalize();
        BigInteger Y2x=Y2.getXCoord().toBigInteger();
        BigInteger Y2y=Y2.getYCoord().toBigInteger();
        System.out.println("Y2:");
        System.out.println(Y2x);
        System.out.println(Y2y);
        System.out.println();

        BigInteger p=r2.add(r1.multiply(m1));
        ECPoint Y2_1t1= Y2.add(Y1.multiply(m1).normalize()).normalize();
        ECPoint Y2_1t2=G.multiply(p).normalize();

        BigInteger Y2_1t1x=Y2_1t1.getXCoord().toBigInteger();
        BigInteger Y2_1t1y=Y2_1t1.getYCoord().toBigInteger();
        BigInteger Y2_1t2x=Y2_1t2.getXCoord().toBigInteger();
        BigInteger Y2_1t2y=Y2_1t2.getYCoord().toBigInteger();


//        ECPoint Y2_1t1= Y2.add(Y1.multiply(m1));
//        ECPoint Y2_1t2=G.multiply(p);
//
//        BigInteger Y2_1t1x=Y2_1t1.normalize().getAffineXCoord().toBigInteger();
//        BigInteger Y2_1t1y=Y2_1t1.normalize().getAffineYCoord().toBigInteger();
//        BigInteger Y2_1t2x=Y2_1t2.normalize().getAffineXCoord().toBigInteger();
//        BigInteger Y2_1t2y=Y2_1t2.normalize().getAffineYCoord().toBigInteger();

        System.out.println("p:"+ p);

        System.out.println("Y2_1t1:");
        System.out.println(Y2_1t1x);
        System.out.println(Y2_1t1y);

        System.out.println("Y2_1t2:");
        System.out.println(Y2_1t2x);
        System.out.println(Y2_1t2y);



//2

        System.out.println("\n\n№2\n\n");

        String dataStr="12345";
        byte[] data=dataStr.getBytes();

        ECPublicKeySpec publicKey1= new ECPublicKeySpec(Y2_1t1,parameterSpec);
        ECPublicKeySpec publicKey2= new ECPublicKeySpec(Y2_1t2,parameterSpec);
        ECPrivateKeySpec privateKey=new ECPrivateKeySpec(p,parameterSpec);

        BigInteger[] signature= sign(data,privateKey);

        System.out.println("Y2_1t1:");
        System.out.println(verify(data,signature[0],signature[1],publicKey1));

        System.out.println("Y2_1t2:");
        System.out.println(verify(data,signature[0],signature[1],publicKey2));
    }
}
