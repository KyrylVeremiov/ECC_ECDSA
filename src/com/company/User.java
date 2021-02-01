package com.company;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.math.ec.ECPoint;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayDeque;

public class User {
    private static BigInteger usersAmount;
    private static ArrayDeque<Helper.UTXO> utxos;

    private static class Helper{

        private static final int numeralSystem=16;

        private static ECPoint getECPointFromString(String str){
            String[] split = str.substring(1, str.length() - 1).split(",");
            BigInteger Px=new BigInteger(split[0],numeralSystem);
            BigInteger Py=new BigInteger(split[1],numeralSystem);
            return Main.curve.validatePoint(Px,Py);
        }

        private static BigInteger getE(BigInteger lockHeight, ECPoint ksG,ECPoint rsG, ECPoint krG,ECPoint rrG){
            String M=lockHeight.toString();

            ECPoint ksGkrG=ksG.add(krG).normalize();

            ECPoint rsGrrG= rsG.add(rrG).normalize();

            String E=M+"|"+ksGkrG.toString()+"|"+rsGrrG;
//            System.out.println(E);

            SHA256Digest sha256Digest= new SHA256Digest();
            byte[] messageBytes=E.getBytes();
            byte[] out= new byte[sha256Digest.getDigestSize()];
            sha256Digest.update(messageBytes,0,messageBytes.length);
            sha256Digest.doFinal(out,0);
            return new  BigInteger(out);
        }

        private static BigInteger getPartialSignature(BigInteger lockHeight, ECPoint ksG,ECPoint rsG, ECPoint krG,ECPoint rrG,BigInteger k,BigInteger r){
            BigInteger e= getE(lockHeight, ksG,rsG, krG,rrG);
            return k.add(e.multiply(r));
        }

        private static BigInteger randomBigInteger(){
//        private static final int d=256;
//        private static final BigInteger n= new BigInteger(abs(new SecureRandom().nextInt())%d,new SecureRandom());//newBlindingFactor
           BigInteger n= new BigInteger(String.valueOf((new SecureRandom().nextLong())));
            return n.signum()>0?n:n.multiply(new BigInteger("-1"));
        }

        private static class UTXO {
            BigInteger TransactionUUID;
            BigInteger privateKey;
            BigInteger amount;
        }
        private static class Signature{
            private static BigInteger s;
            private static ECPoint kG;
            Signature(BigInteger s1, ECPoint kG1){
                s=s1;
                kG=kG1;
            }
            public static BigInteger getS(){
                return s;
            }
            public static ECPoint getKG(){
                return kG;
            }
        }

        private static boolean verifySignature(Signature signature, ECPoint rG, BigInteger e){
            return signature.getKG().add(rG.multiply(e)).normalize().equals(Main.G.multiply(signature.getS()).normalize());
        }
        public static Signature sign(BigInteger s, ECPoint kG) {
            return new Signature(s,kG);
        }
    }


    private static class Sender {
        private static final String AMOUNT_TO_SEND = "amountToSend";
        private static final String UUID = "uuid";
        private static final String LOCKHEIGHT = "lockHeight";
        private static final String CO = "CO";
        private static final String RSG = "rsG";
        private static final String KSG = "ksG";

        private static BigInteger amountToSend;
        private static BigInteger uuid;
        private static BigInteger lockHeight;
        private static final BigInteger rn = Helper.randomBigInteger();
        private static final BigInteger ks = Helper.randomBigInteger();
        private static BigInteger rs;

        private static ECPoint rsG;
        private static final ECPoint ksG = Main.G.multiply(ks).normalize();
        private static ECPoint co;//changeOutput

        private static ECPoint rrG;
        private static ECPoint krG;
        private static BigInteger sr;

//        private static [] s;//signature

        //    private static transactionInputs;


        private Sender() {
//            amountToSend=
        }


//        private void Send(addressToSend,BigInteger amount){
//            PrepareRequest();
//            SendFile();
//            Sender.(addressToSend,amount);
//            Sender
//            if(succes){
//                  this.utxos.add()
//            }
//        }

        private static void prepareRequest() {
            BigInteger blindingFactorsSum = new BigInteger("0");//xl
//!!!!!!!!!!!!!
            rs = rn.subtract(blindingFactorsSum);
            rsG = Main.G.multiply(rs).normalize();

            co = Main.G;
            lockHeight = new BigInteger("1");
            uuid = new BigInteger("1");

            amountToSend = new BigInteger("1");
            sendRequest();
        }

        private static void sendRequest() {
            JSONObject jo = new JSONObject();
            jo.put(AMOUNT_TO_SEND, amountToSend.toString());
            jo.put(UUID, uuid.toString());
            jo.put(LOCKHEIGHT, lockHeight.toString());
            jo.put(CO, co.toString());
            jo.put(RSG, rsG.toString());
            jo.put(KSG, ksG.toString());
            try (FileWriter file = new FileWriter("request.json")) {
                file.write(jo.toString());
                file.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        private static void getResponse(){

            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader("response.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);

                JSONObject sendersRequest = (JSONObject) obj;

                rrG=Helper.getECPointFromString(sendersRequest.get(Recipient.RRG).toString());
                krG=Helper.getECPointFromString(sendersRequest.get(Recipient.KRG).toString());
                sr=new BigInteger(sendersRequest.get(Recipient.SR).toString());

//                System.out.println(rrG.toString());
//                System.out.println(sendersRequest.get(Receiver.RRG));
//                System.out.println(rrG.equals(Receiver.rrG));
                prepareTransaction();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

        }

        private static void prepareTransaction(){
            BigInteger ss=Helper.getPartialSignature(lockHeight,ksG,rsG,krG,rrG,ks,rs);

            BigInteger s=sr.add(ss);
            ECPoint kG=ksG.add(krG).normalize();
            ECPoint rG=rrG.add(rsG).normalize();

            Helper.Signature signature = Helper.sign(s,kG);
            BigInteger e=Helper.getE(lockHeight,ksG,rsG,krG,rrG);

            System.out.println(Helper.verifySignature(signature,rG,e));
            sendTransaction();
//            System.out.println(Main.G.multiply(s).normalize());
//            System.out.println(kG.add(rG.multiply(e)).normalize());
//            System.out.println(kG.add(rG.multiply(e)).normalize().equals(Main.G.multiply(s).normalize()));
        }
        private static void sendTransaction(){

        }
    }

    private static class Recipient {
        private static final String SR="sr";
        private static final String KRG="krG";
        private static final String RRG="rrG";


        private static final BigInteger rr = Helper.randomBigInteger();
        private static final BigInteger kr = Helper.randomBigInteger();

        private static BigInteger sr;
        private static final ECPoint rrG=Main.G.multiply(rr).normalize();
        private static final ECPoint krG=Main.G.multiply(kr).normalize();

        private static ECPoint rsG;
        private static ECPoint ksG;
        private static ECPoint co;//Receiver needs it indeed ?
        private static BigInteger amountToSend;
        private static BigInteger uuid;
        private static BigInteger lockHeight;


        private static void getRequestFile() {

            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader("request.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);

                JSONObject sendersRequest = (JSONObject) obj;

                amountToSend=new BigInteger(sendersRequest.get(Sender.AMOUNT_TO_SEND).toString());
                uuid=new BigInteger(sendersRequest.get(Sender.UUID).toString());
                lockHeight=new BigInteger(sendersRequest.get(Sender.LOCKHEIGHT).toString());

                rsG=Helper.getECPointFromString(sendersRequest.get(Sender.RSG).toString());
                ksG=Helper.getECPointFromString(sendersRequest.get(Sender.KSG).toString());
                co=Helper.getECPointFromString(sendersRequest.get(Sender.CO).toString());

                prepareResponse();
//                System.out.println(rsG.toString());
//                System.out.println(sendersRequest.get(Sender.RSG));
//                System.out.println(rsG.equals(Sender.rsG));

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
        private static void prepareResponse(){
            sr=Helper.getPartialSignature(lockHeight,ksG,rsG,krG,rrG,kr,rr);
            sendResponse();
        }
        private static void sendResponse(){
            JSONObject jo = new JSONObject();
            jo.put(SR, sr.toString());
            jo.put(KRG, krG.toString());
            jo.put(RRG, rrG.toString());
            try (FileWriter file = new FileWriter("response.json")) {
                file.write(jo.toString());
                file.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }


    public static void main(String[] args){
        Sender.prepareRequest();
        Recipient.getRequestFile();
        Sender.getResponse();
    }
//    User.Sender.Send(addressToSend,amount);
}
