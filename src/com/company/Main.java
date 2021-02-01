package com.company;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Main {


    static final BigInteger m1=new BigInteger("-1");
    static final String curve_name="secp256k1";

    static final X9ECParameters curveParams = ECNamedCurveTable.getByName(curve_name);
    static final ECCurve curve= curveParams.getCurve();
    static final ECParameterSpec parameterSpec= new ECParameterSpec(curveParams.getCurve(),curveParams.getG(),curveParams.getN(),curveParams.getH());
    static final ECDomainParameters domain= new ECDomainParameters(curveParams.getCurve(),curveParams.getG(),curveParams.getN(),curveParams.getH());

    static final ECPoint G=curveParams.getG();
    static final ECPoint H=G.multiply(new BigInteger("3")).normalize();

    public static void main(String[] args){
        //        Test t=new Test();
//        User u= new User();
//        u.Test();
    }
}
