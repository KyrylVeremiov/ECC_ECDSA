package com.company;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.math.ec.ECPoint;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

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

//        private static void sendFile(Socket socket, File file) throws IOException {
//            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//
//
//            byte[] fileContent = Files.readAllBytes(file.toPath());
//            output.write(fileContent);
//        }
//
//        private static void getFile(Socket socket, File file) throws IOException {
//            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//            input.readAllBytes();
//
//        }

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



        private static final String RESPONSE_S="responseS.json";
        private static final String REQUEST_S="requestS.json";

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

        private static final int port=90;//getPort(...);
        private static final String host = "127.0.0.1";//getHost(...);
        private static Socket socket;
        private static DataOutputStream output;
        private static DataInputStream input;

        private static void sendToRecipient() throws IOException, InterruptedException {
            prepareRequest();
            makeRequestFile();
            sendRequestFile();
        }

        private static void send() throws IOException {
//            makeConnection();
//            prepareRequest();
//            makeRequestFile();
//            sendRequestFile();
            getResponseFile();
            parseResponseFile();
            prepareTransaction();
            sendTransaction();
        }


        private static void makeConnection() throws IOException {
            SocketFactory socketfactory = SocketFactory.getDefault();
            socket = socketfactory
                    .createSocket(host, port);
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }

        private static void prepareRequest() {
            BigInteger blindingFactorsSum = new BigInteger("0");//xl
//!!!!!!!!!!!!!
            rs = rn.subtract(blindingFactorsSum);
            rsG = Main.G.multiply(rs).normalize();

            co = Main.G;
            lockHeight = new BigInteger("1");
            uuid = new BigInteger("1");
            amountToSend = new BigInteger("1");
        }

        private static void makeRequestFile() throws IOException {
            File file = new File(REQUEST_S);

            JSONObject jo = new JSONObject();
            jo.put(AMOUNT_TO_SEND, amountToSend.toString());
            jo.put(UUID, uuid.toString());
            jo.put(LOCKHEIGHT, lockHeight.toString());
            jo.put(CO, co.toString());
            jo.put(RSG, rsG.toString());
            jo.put(KSG, ksG.toString());
//            try (FileWriter file = new FileWriter("request.json")) {
            try (FileWriter fileWriter = new FileWriter(REQUEST_S)) {

                fileWriter.write(jo.toString());
                fileWriter.flush();
//                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        private static void sendRequestFile() throws IOException, InterruptedException {
//            String str= "Hello";
            byte[] fileContent = Files.readAllBytes(Path.of(REQUEST_S));

//            output.write(fileContent);

//                output.write(fileContent, 0, fileContent.length);

//            output.close();
//            input.close();
//            socket.close();

            InputStream in = new FileInputStream(REQUEST_S);
            OutputStream out = socket.getOutputStream();
            byte[] bytes = new byte[16 * 1024];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.write("/".getBytes());
            System.out.println(3);
            Sender.send();
        }

        private static void getResponseFile() throws IOException {

            File file = new File(RESPONSE_S);
            FileOutputStream fos = new FileOutputStream(file.getPath());
//            System.out.println(7);
//            byte[] bytes= input.readAllBytes();
//            fos.write(bytes);

            System.out.println(7);
            int in = input.read();
            System.out.println(7.5);
//            System.out.println(in);
            while (in !=(int)'/') {
//                                System.out.println(buffer);
                fos.write(in);
                in = input.read();
//                System.out.println(in);
//                                output.write(buffer, 0, count);
            }
            System.out.println(7.7);

//            int in=input.read();
//            while (in!=-1)
//            {
////                                System.out.println(buffer);
//                fos.write(in);
//                in=input.read();
////                            System.out.println(in);
////                                output.write(buffer, 0, count);
//            }

//            input.close();
            fos.close();
            parseResponseFile();
//            prepareTransaction();
            socket.close();
        }

        private static void parseResponseFile(){

            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader(RESPONSE_S)) {
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
        private static ECPoint co;//Does Receiver need it indeed?
        private static BigInteger amountToSend;
        private static BigInteger uuid;
        private static BigInteger lockHeight;

        private static final String RESPONSE_R="responseR.json";
        private static final String REQUEST_R="requestR.json";

        private static Server server;

//        public Recipient(){
//            server= new Server();
//        }

        private static void send(){
//            makeConnection();
//            parseRequestFile();
//            prepareResponseFile();
            try {
                sendResponseFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        private static void makeConnection(){
            server=new Server();
            server.start();
        }

        private static class Server extends Thread {
            static final int port= 90;


            private ServerSocket serversocket;
            private List<ConnectedClient> clients = new ArrayList<>();


            @Override
            public void run() {

                ServerSocketFactory factory = (ServerSocketFactory) ServerSocketFactory.getDefault();
                try {
                    serversocket = (ServerSocket) factory.createServerSocket(port);

//                Socket socket = (Socket) serversocket.accept();
//                socket = new SServerSocket();

                    while (true) {
                        Socket socket2=serversocket.accept();
                        ConnectedClient client = new ConnectedClient(socket2);
                        clients.add(client);
                        client.start();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            private void main() throws IOException {
                this.start();
            }



            private class ConnectedClient extends Thread {
                private Socket socket;
                private DataInputStream input;
                private DataOutputStream output;


                private ConnectedClient(Socket s) throws IOException {
                    socket = s;
                    System.out.println("new user connected from " + s.getInetAddress().toString());
                    output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                }

                @Override
                public void run() {
                    try {
                        System.out.println(2);

                        File file = new File(REQUEST_R);
                        FileOutputStream fos = new FileOutputStream(file.getPath());



//                            while (socket.isConnected()) {
//                                byte[] bytes= input.readAllBytes();
//                                System.out.println(bytes);
//                                fos.write(bytes);
//                                break;
//                            }
//                        FileOutputStream fos = new FileOutputStream("test.json");requestR
//                        FileOutputStream fos = new FileOutputStream("requestR.json");
////                        System.out.println(socket.isClosed());
//
                                int in = input.read();
                                System.out.println(3.5);
//                                System.out.println(in);
                                while (in != (int)'/') {
//                                System.out.println(buffer);
                                    fos.write(in);
                                    in = input.read();
//                                    System.out.println(in);
//                                output.write(buffer, 0, count);
                                }

//                        InputStream in = socket.getInputStream();
//                        OutputStream out=new FileOutputStream(REQUEST_R);
//
//
////                    while (socket.isConnected()) {
//                        int count;
//                        System.out.println(2.5);
//                        byte[] buffer = new byte[8192]; // or 4096, or more
//                        while ((count = in.read(buffer)) > 0)
//                        {
//                            System.out.println(2.7);
//                            out.write(buffer, 0, count);
//                        }
//                        break;
//                    }
//                        in.close();
//                        out.close();

//                        input.close();
//                        fos.close();
////                        socket.close();
                        System.out.println(4);

                        parseRequestFile();
                        prepareResponseFile();

//                        while (!socket.isClosed());

                    } catch (IOException e) {
                        System.out.println(e.toString());
                    } finally {
                        System.out.println("user disconnected from " + socket.getInetAddress().toString());
                    }
                }

//                public void send(String s) throws IOException {
//                    output.write(s.getBytes());
////                    output.flush();
//                }
            }
        }

        private static void parseRequestFile() {

            System.out.println(5);

            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader(REQUEST_R)) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);

                JSONObject sendersRequest = (JSONObject) obj;

                amountToSend=new BigInteger(sendersRequest.get(Sender.AMOUNT_TO_SEND).toString());
                uuid=new BigInteger(sendersRequest.get(Sender.UUID).toString());
                lockHeight=new BigInteger(sendersRequest.get(Sender.LOCKHEIGHT).toString());

                rsG=Helper.getECPointFromString(sendersRequest.get(Sender.RSG).toString());
                ksG=Helper.getECPointFromString(sendersRequest.get(Sender.KSG).toString());
                co=Helper.getECPointFromString(sendersRequest.get(Sender.CO).toString());

//                System.out.println(rsG.toString());
//                System.out.println(sendersRequest.get(Sender.RSG));
//                System.out.println(rsG.equals(Sender.rsG));

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        private static void prepareResponseFile(){
            sr=Helper.getPartialSignature(lockHeight,ksG,rsG,krG,rrG,kr,rr);

            JSONObject jo = new JSONObject();
            jo.put(SR, sr.toString());
            jo.put(KRG, krG.toString());
            jo.put(RRG, rrG.toString());
            try {
                File file = new File(RESPONSE_R);
                FileWriter fileWriter = new FileWriter(file.getPath());
                fileWriter.write(jo.toString());
                fileWriter.flush();
                fileWriter.close();

            } catch (IOException exception) {
                exception.printStackTrace();
            }
            try {
                sendResponseFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        private static void sendResponseFile() throws IOException {
//            File file = new File(RESPONSE_R);


//            byte[] fileContent = Files.readAllBytes(Path.of(RESPONSE_R));

            System.out.println(6);


            InputStream in = new FileInputStream(RESPONSE_R);
            OutputStream out = server.clients.get(0).socket.getOutputStream();
            byte[] bytes = new byte[16 * 1024];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.write("/".getBytes());
            System.out.println(6.5);

//            server.clients.get(0).output.write(fileContent);


//            server.clients.get(0).output.close();
//            server.clients.get(0).input.close();
//            server.clients.get(0).socket.close();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
//        Recipient.makeConnection();

//        Recipient.Server server =new Recipient.Server();
//        server.main();
        Recipient.makeConnection();
        Sender.makeConnection();
        System.out.println(1);
        Sender.sendToRecipient();
//        Recipient.send();
//        Sender.send();


//        Sender.makeConnection();

//        Sender.prepareRequest();
//        Recipient.parceRequestFile();
//        Sender.getResponse();
    }
//    User.Sender.Send(addressToSend,amount);
}
