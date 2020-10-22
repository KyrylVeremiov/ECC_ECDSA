package com.company;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECCurve.Fp;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECMultiplier;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Signature;

public class Main {

    public static void main(String[] args) {

        BigInteger m1=new BigInteger("-1");


        X9ECParameters curve_params =ECNamedCurveTable.getByName("secp256k1");
        ECCurve curve= curve_params.getCurve();

        ECPoint G0=curve_params.getG();

        ECPoint G=G0.multiply(new BigInteger("2")).normalize();
        ECPoint H=G0.multiply(new BigInteger("3")).normalize();
        curve.validatePoint(G.getXCoord().toBigInteger(),G.getYCoord().toBigInteger());
        curve.validatePoint(H.getXCoord().toBigInteger(),H.getYCoord().toBigInteger());






//        BigInteger prime = new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564821041");
//        BigInteger A = new BigInteger("7");
//        BigInteger B = new BigInteger("43308876546767276905765904595650931995942111794451039583252968842033849580414");
//
//        ECCurve curve = new ECCurve.Fp(prime, A, B);
//
//        BigInteger Gx = new BigInteger("2");
//        BigInteger Gy = new BigInteger("4018974056539037503335449422937059775635739389905545080690979365213431566280");
//        BigInteger Hx = new BigInteger("57520216126176808443631405023338071176630104906313632182896741342206604859403");
//        BigInteger Hy = new BigInteger("17614944419213781543809391949654080031942662045363639260709847859438286763994");
//
//        ECPoint G = curve.validatePoint(Gx, Gy);
//        ECPoint H = curve.validatePoint(Hx, Hy);
////        ECPoint R = G.add(H).normalize();
////        R=R.multiply(new BigInteger("12")).normalize();
////
////        curve.validatePoint(R.getXCoord().toBigInteger(),R.getYCoord().toBigInteger());


















//1
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

        System.out.println("p:"+ p);
        System.out.println("Y2_1t1:");

        System.out.println(Y2_1t1x);
        System.out.println(Y2_1t1y);
        System.out.println("Y2_1t2:");
        System.out.println(Y2_1t2x);
        System.out.println(Y2_1t2y);



//2

        //order of a point
//        BigInteger N1=curve.getOrder();
//        BigInteger N1= curve_params.getN();
//        BigInteger n=dividors_N
//        System.out.println(N1);

//        ECDSASigner signer = new ECDSASigner (new HMacDSAKCalculator(new SHA256Digest()));
//        ECDomainParameters parametrs=new new ECDomainParameters(curve,G,N);
//        signer.init (true, new ECPrivateKeyParameters(p,parametrs));
//        BigInteger[] signature = signer.generateSignature (hash);
//        ByteArrayOutputStream s = new ByteArrayOutputStream ();
//        try
//        {
//            DERSequenceGenerator seq = new DERSequenceGenerator (s);
//            seq.addObject (new ASN1Integer(signature[0]));
//            seq.addObject (new ASN1Integer (signature[1]));
//            seq.close ();
//            s.toByteArray ();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }
}
