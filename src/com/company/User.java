package com.company;

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
    private static ArrayDeque<UTXO> utxos;

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
        private static final BigInteger rn = new BigInteger(String.valueOf((new SecureRandom().nextLong())));
        private static final BigInteger ks = new BigInteger(String.valueOf((new SecureRandom().nextLong())));
//        private static final int d=256;
//        private static final BigInteger rn= new BigInteger(abs(new SecureRandom().nextInt())%d,new SecureRandom());//newBlindingFactor

        private static ECPoint rsG;
        private static final ECPoint ksG = Main.G.multiply(ks);
        private static ECPoint co;//changeOutput

        //    private static transactionInputs;


        private Sender() {
//            amountToSend=
        }


//        private void Send(addressToSend,BigInteger amount){
//            Sender.(addressToSend,amount);
//            Sender
//            if(succes){
//                  this.utxos.add()
//            }
//        }

        private static void makeRequest() {

        }

        private static void CreateTransactionRequest() {
            BigInteger blindingFactorsSum = new BigInteger("0");//xl
//!!!!!!!!!!!!!
            BigInteger rs = rn.subtract(blindingFactorsSum);
            rsG = Main.G.multiply(rs);

            co = Main.G;
            lockHeight = new BigInteger("1");
            uuid = new BigInteger("1");

            amountToSend = new BigInteger("1");
        }

        private static void CreateFile() {
            CreateTransactionRequest();
            JSONObject jo = new JSONObject();
            jo.put(AMOUNT_TO_SEND, amountToSend);
            jo.put(UUID, uuid);
            jo.put(LOCKHEIGHT, lockHeight);
            jo.put(CO, co.toString());
            jo.put(RSG, rsG.toString());
            jo.put(KSG, ksG.toString());
            try (FileWriter file = new FileWriter("transaction_request.json")) {
                file.write(jo.toString());
                file.flush();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static class Receiver{
        private static ECPoint rsG;
        private static ECPoint ksG;
        private static ECPoint co;
        private static BigInteger amountToSend;
        private static BigInteger uuid;
        private static BigInteger lockHeight;

        private static void getRequestFile() {

            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader("transaction_request.json")) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);

                JSONObject sendersRequest = (JSONObject) obj;

                 amountToSend=new BigInteger(sendersRequest.get(Sender.AMOUNT_TO_SEND).toString());
                 uuid=new BigInteger(sendersRequest.get(Sender.UUID).toString());
                 lockHeight=new BigInteger(sendersRequest.get(Sender.LOCKHEIGHT).toString());
//                 rsG=
//                 System.out.println(uuid.toString() + amountToSend +lockHeight);
                //Iterate over employee array
//                employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args){
        Sender.CreateFile();
        Receiver.getRequestFile();
    }
//    User.Sender.Send(addressToSend,amount);
}
