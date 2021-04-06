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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class User {
    private static HashMap<BigInteger,Helper.UTXO> utxoes =new HashMap<BigInteger, Helper.UTXO>();

    private static HashMap<BigInteger,Helper.usersUTXO>usersUTXOes=new HashMap<BigInteger, Helper.usersUTXO>();

    private static void addUTXO(Helper.UTXO utxo){
        utxoes.put(utxo.uuid,utxo);
    }


    private static class Helper{

        private static final int numeralSystem=16;

        private static Logger logger;
        private static FileHandler fh;

        private static void sendFile(String FILE,Socket socket) throws IOException {

            byte[] fileContent = Files.readAllBytes(Path.of(FILE));

//            output.write(fileContent);

//                output.write(fileContent, 0, fileContent.length);

//            output.close();
//            input.close();
//            socket.close();

            InputStream in = new FileInputStream(FILE);
            OutputStream out = socket.getOutputStream();
            byte[] bytes = new byte[16 * 1024];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
            out.write("/".getBytes());
        }

        private static void getFile(String FILE, Socket socket) throws IOException {
            File file = new File(FILE);
            FileOutputStream fos = new FileOutputStream(file.getPath());
            DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//            System.out.println(7);
//            byte[] bytes= input.readAllBytes();
//            fos.write(bytes);

//            System.out.println(7);
            int in = input.read();
//            System.out.println(7.5);
//            System.out.println(in);
            while (in !=(int)'/') {
//                                System.out.println(buffer);
                fos.write(in);
                in = input.read();
//                System.out.println(in);
//                                output.write(buffer, 0, count);
            }
            fos.close();
        }

        private static ECPoint getECPointFromString(String str){
            String[] split = str.substring(1, str.length() - 1).split(",");
            BigInteger Px=new BigInteger(split[0],numeralSystem);
            BigInteger Py=new BigInteger(split[1],numeralSystem);
            return Main.curve.validatePoint(Px,Py);
        }

        private static Helper.Signature getSignatureFromString(String str){
            String strkG= str.substring(1,str.indexOf(')')+1);
            String strs=str.substring(str.indexOf(')')+2,str.length()-1);
            ECPoint kG= getECPointFromString(strkG);
            BigInteger s=new BigInteger(strs);
            return Helper.sign(kG,s);
        }

        private static BigInteger getE(BigInteger M, ECPoint kG, ECPoint xG){
//        private static BigInteger getE(BigInteger lockHeight, ECPoint ksG,ECPoint rsG, ECPoint krG,ECPoint rrG){

//            ECPoint kG=ksG.add(krG).normalize();
//
//            ECPoint xG= rsG.add(rrG).normalize();

            String E=kG.toString()+"|"+xG+"|"+M;
//            System.out.println(E);

            SHA256Digest sha256Digest= new SHA256Digest();
            byte[] messageBytes=E.getBytes();
            byte[] out= new byte[sha256Digest.getDigestSize()];
            sha256Digest.update(messageBytes,0,messageBytes.length);
            sha256Digest.doFinal(out,0);
            return new  BigInteger(out);
        }

        private static BigInteger getPartialSignature(BigInteger lockHeight, ECPoint ksG,ECPoint xsG, ECPoint krG,ECPoint xrG,BigInteger k,BigInteger r){
            BigInteger e= getE(lockHeight, ksG.add(krG).normalize(),xsG.add(xrG).normalize());
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

        private static class Signature{
            private static BigInteger s;
            private static ECPoint kG;
            Signature(ECPoint kG1, BigInteger s1){
                s=s1;
                kG=kG1;
            }
            public String toString(){
                return "("+kG.toString()+","+s.toString()+")";
            }
            public static BigInteger getS(){
                return s;
            }
            public static ECPoint getKG(){
                return kG;
            }
        }

        private static boolean verifySignature(Signature signature, ECPoint xG, BigInteger e){
            return signature.getKG().add(xG.multiply(e)).normalize().equals(Main.G.multiply(signature.getS()).normalize());
        }
        public static Signature sign(ECPoint kG,BigInteger s) {
            return new Signature(kG,s);
        }
        private static class UTXO {
            BigInteger uuid;
            BigInteger commitment;
            //signature
        }

        private static class usersUTXO{
            BigInteger uuid;
            BigInteger amount;
            BigInteger privateKey;
        }


        private static void prepareLogger(){
            try {
                logger = Logger.getLogger("MyLog");
                fh= new FileHandler("Logger.log");;
                // This block configure the logger with handler and formatter
                logger.addHandler(fh);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);


                // the following statement is used to log any messages

            } catch (SecurityException | IOException e) {
                e.printStackTrace();
            }
        }

        private static void writeLog(String message) {
            try {
                logger.info(message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static class Sender {
        private static final String AMOUNT_TO_SEND = "amountToSend";
        private static final String UUID = "uuid";
        private static final String LOCKHEIGHT = "lockHeight";
        private static final String CHANGEOUTPUT = "changeOutput";
        private static final String XSG = "xSG";
        private static final String KSG = "kSG";
        private static final String OUTPUT="output";
        private static final String INPUT="input";
        private static final String SIGNATURE="signature";
        private static final String XG="xG";
        private static final String S="s";


        private static BigInteger amountToSend=new BigInteger("20");
        private static BigInteger amountTotal;
        private static BigInteger amountChange;
        private static BigInteger uuid;
        private static BigInteger lockHeight;
        private static final BigInteger xC = Helper.randomBigInteger();//rnew //xC
        private static final BigInteger kS = Helper.randomBigInteger();
        private static BigInteger xS1;
        private static BigInteger xS;
        private static BigInteger oS=new BigInteger("0");

        private static ECPoint xSG;
        private static final ECPoint kSG = Main.G.multiply(kS).normalize();
        private static ECPoint changeOutput;

        private static ECPoint kG;
        private static ECPoint xG;
        private static BigInteger s;
        private static Helper.Signature signature;


        private static ECPoint xRG;
        private static ECPoint kRG;
        private static BigInteger sR;
        private static ECPoint recipientOutput;




        private static final String RESPONSE_S="responseS.json";
        private static final String REQUEST_S="requestS.json";
        private static final String TRANSACTION="transaction.json";

//        Inputs for test
        private static ECPoint X1;
        private static ECPoint X2;
        private static ECPoint X3;


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
//        private static final String host = "192.168.43.57";//getHost(...);
        private static Socket socket;

        private static void sendToRecipient() throws IOException, InterruptedException {
            prepareRequest();
            makeRequestFile();
            sendRequestFile();
        }

        private static void makeTransaction() throws IOException {
//            makeConnection();
//            prepareRequest();
//            makeRequestFile();
//            sendRequestFile();
            getResponseFile();
            parseResponseFile();
            prepareTransaction();
        }

        private static void makeConnection() throws IOException {
            SocketFactory socketfactory = SocketFactory.getDefault();
            socket = socketfactory
                    .createSocket(host, port);

            Helper.writeLog("Connected to "+host+" on "+port);
//            System.out.println("Connected "+host+" on "+port);
        }

//        private static ArrayList<Helper.UTXO> getUTXOesForTransaction(){
//            ArrayList<BigInteger> uuids=new ArrayList<BigInteger>();
//            BigInteger blindingFactorsSum=new BigInteger("0");
//            BigInteger amount=new BigInteger("0");
//            while (amount.compareTo(amountToSend) < 0){
//                Helper.usersUTXO userUTXO=usersUTXOes.get(first);
//                uuids.add(userUTXO.uuid);
//                amount=amount.add(userUTXO.amount);
//                blindingFactorsSum=blindingFactorsSum.add(userUTXO.privateKey);
//            }
//        }


        private static void prepareRequest() {
//            TEST
            BigInteger x1=Helper.randomBigInteger();
            BigInteger v1= new BigInteger("18");

            X1= Main.G.multiply(x1).add(Main.H.multiply(v1)).normalize();

            BigInteger x2= Helper.randomBigInteger();
            BigInteger v2= new BigInteger("12");

            X2= Main.G.multiply(x2).add(Main.H.multiply(v2)).normalize();


            Helper.writeLog("Inputs for this transaction:\n" +"Input 1:\nX: \n"+X1+"\nblinding factor: "+x1+"\namount: "+v1+"\nInput 2:\nX: \n"+X2+"\nblinding factor: "+x2+"\namount: "+v2);


            lockHeight = new BigInteger("25");
            uuid = new BigInteger("123445234003");
            BigInteger xl=x1.add(x2);
//            TEST

//            BigInteger xl = new BigInteger("0");

            amountTotal =v1.add(v2);
            amountChange=amountTotal.subtract(amountToSend);

            xS1 = xC.subtract(xl);
            xS= xS1.subtract(oS);
            Helper.writeLog("rs: "+ xS);
            xSG = Main.G.multiply(xS).normalize();

//            change_output = Main.G;
            changeOutput =  Main.G.multiply(xC).add(Main.H.multiply(amountChange)).normalize();
            Helper.writeLog("Sum of input blinding factors (xl): "+xl +"\nTotal input amount: "+amountTotal +"\nAmount to send: "+amountToSend+ "\nChange: "+ amountChange+"\nNew blinding factor for Change output: "+xC+"\nChange output: "+ changeOutput);
        }

        private static void makeRequestFile() throws IOException {
            File file = new File(REQUEST_S);

            JSONObject jo = new JSONObject();
            jo.put(AMOUNT_TO_SEND, amountToSend.toString());
            jo.put(UUID, uuid.toString());
            jo.put(LOCKHEIGHT, lockHeight.toString());
            jo.put(CHANGEOUTPUT, changeOutput.toString());
            jo.put(XSG, xSG.toString());
            jo.put(KSG, kSG.toString());
//            try (FileWriter file = new FileWriter("request.json")) {
            try (FileWriter fileWriter = new FileWriter(REQUEST_S)) {

                fileWriter.write(jo.toString());
                fileWriter.flush();
//                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            Helper.writeLog("Request request file is made");
        }

        private static void sendRequestFile() throws IOException, InterruptedException {
//            String str= "Hello";
//            byte[] fileContent = Files.readAllBytes(Path.of(REQUEST_S));
//
////            output.write(fileContent);
//
////                output.write(fileContent, 0, fileContent.length);
//
////            output.close();
////            input.close();
////            socket.close();
//
//            InputStream in = new FileInputStream(REQUEST_S);
//            OutputStream out = socket.getOutputStream();
//            byte[] bytes = new byte[16 * 1024];
//
//            int count;
//            while ((count = in.read(bytes)) > 0) {
//                out.write(bytes, 0, count);
//            }
//            out.write("/".getBytes());
            socket.getOutputStream().write((byte)0);
              Helper.sendFile(REQUEST_S,socket);
//            Sender.send();
            Helper.writeLog("Request file is sent\nWaiting for response\n\n\n");
        }

        private static void getResponseFile() throws IOException {
//
//            File file = new File(RESPONSE_S);
//            FileOutputStream fos = new FileOutputStream(file.getPath());
//            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
////            System.out.println(7);
////            byte[] bytes= input.readAllBytes();
////            fos.write(bytes);
//
////            System.out.println(7);
//            int in = input.read();
////            System.out.println(7.5);
////            System.out.println(in);
//            while (in !=(int)'/') {
////                                System.out.println(buffer);
//                fos.write(in);
//                in = input.read();
////                System.out.println(in);
////                                output.write(buffer, 0, count);
//            }

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
//            fos.close();
            parseResponseFile();
//            prepareTransaction();
            Helper.getFile(RESPONSE_S,socket);
            Helper.writeLog("Response file is got");
            socket.close();
        }

        private static void parseResponseFile(){

            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader(RESPONSE_S)) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);

                JSONObject sendersRequest = (JSONObject) obj;

                xRG =Helper.getECPointFromString(sendersRequest.get(Recipient.XRG).toString());
                kRG =Helper.getECPointFromString(sendersRequest.get(Recipient.KRG).toString());
                sR =new BigInteger(sendersRequest.get(Recipient.SR).toString());
                recipientOutput=Helper.getECPointFromString(sendersRequest.get(Recipient.RECIPIENTOUTPUT).toString());

                Helper.writeLog("Response file is parsed");

//                System.out.println(rrG.toString());
//                System.out.println(sendersRequest.get(Receiver.RRG));
//                System.out.println(rrG.equals(Receiver.rrG));
//                prepareTransaction();
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        private static void prepareTransaction(){
            BigInteger sS=Helper.getPartialSignature(lockHeight, kSG, xSG, kRG, xRG, kS, xS1);
            Helper.writeLog("Sender Schnorr signature (sS): "+sS);

            s= sR.add(sS);
            kG= kSG.add(kRG).normalize();
            xG= xRG.add(xSG).normalize();

            signature = Helper.sign(kG,s);

            BigInteger e=Helper.getE(lockHeight, kSG.add(kRG).normalize(),xSG.add(xRG).normalize());

            Helper.writeLog("Final signature: "+ signature.toString());

            Helper.writeLog("Verification of signature: \n"+Helper.verifySignature(signature,xG,e)+
                    "\nkG+xG*e: "+kG.add(xG.multiply(e)).normalize()+
                    "\ns*G: "+Main.G.multiply(s).normalize());
//            sendTransaction();
//            System.out.println(Main.G.multiply(s).normalize());
//            System.out.println(kG.add(rG.multiply(e)).normalize());
//            System.out.println(kG.add(rG.multiply(e)).normalize().equals(Main.G.multiply(s).normalize()));
        }

        private static void makeTransactionFile(){
//            File file = new File(TRANSACTION);

            JSONObject jo = new JSONObject();
            jo.put(OUTPUT+"0", changeOutput.toString());
            jo.put(OUTPUT+"1",recipientOutput.toString());
            jo.put(INPUT+"0",X1.toString());
            jo.put(INPUT+"1",X2.toString());
//            jo.put(UUID, uuid.toString());

//            Transaction kernel
            jo.put(SIGNATURE,signature.toString());
            jo.put(LOCKHEIGHT, lockHeight.toString());
            jo.put(XG, xG.toString());
            try (FileWriter fileWriter = new FileWriter(TRANSACTION)) {

                fileWriter.write(jo.toString());
                fileWriter.flush();
//                fileWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }

        }












        private static ArrayList<Integer> ports=new ArrayList<Integer>(){{add(90);}};//getPort(...);
        //        private static final String host = "127.0.0.1";//getHost(...);
        private static ArrayList<String> hosts = new ArrayList<String>(){
            {add("192.168.43.57");}
        };//getHost(...);
        private static void sendTransactionFile() throws IOException {

            SocketFactory socketfactory = SocketFactory.getDefault();
            socket = socketfactory
                    .createSocket(hosts.get(0), ports.get(0));

            Helper.sendFile(TRANSACTION,socket);
            Helper.writeLog("Transaction file is sent to "+hosts.get(0)+" on "+ports.get(0));
        }
    }

    private static class Recipient {
        private static final String SR="sr";
        private static final String KRG="kRG";
        private static final String XRG ="xRG";
        private static final String RECIPIENTOUTPUT="recipientOutput";

        private static final BigInteger xR = Helper.randomBigInteger();
        private static final BigInteger kR = Helper.randomBigInteger();

        private static BigInteger sR;
        private static final ECPoint xRG =Main.G.multiply(xR).normalize();
        private static final ECPoint kRG =Main.G.multiply(kR).normalize();

        private static ECPoint xSG;
        private static ECPoint kSG;
        private static ECPoint recipientOutput;

        private static BigInteger amountToSend;
        private static BigInteger uuid;
        private static BigInteger lockHeight;

        private static final String RESPONSE_R="responseR.json";
        private static final String REQUEST_R="requestR.json";

        private static Server server;

//        public Recipient(){
//            server= new Server();
//        }


        private static void makeConnection(){
            server=new Server();
            server.start();
            Helper.writeLog("Recipient server is started");
        }

        private static class Verifier {
            private static ArrayList<ECPoint> Xi=new ArrayList<ECPoint>();//Inputs
            private static ArrayList<ECPoint> Yi=new ArrayList<ECPoint>();//Outputs

            private static BigInteger lockHeightVerifying;
            private static ECPoint xG;
            private static Helper.Signature signature;

            private static void parseTransactionFile() {
                JSONParser jsonParser = new JSONParser();

                try (FileReader reader = new FileReader(Sender.TRANSACTION)) {
                    //Read JSON file
                    Object obj = jsonParser.parse(reader);

                    JSONObject sendersRequest = (JSONObject) obj;
                    ECPoint X1 = Helper.getECPointFromString(sendersRequest.get(Sender.INPUT + "0").toString());
                    ECPoint X2 = Helper.getECPointFromString(sendersRequest.get(Sender.INPUT + "1").toString());

                    Xi.add(X1);
                    Xi.add(X2);

                    ECPoint Y1 = Helper.getECPointFromString(sendersRequest.get(Sender.OUTPUT + "0").toString());
                    ECPoint Y2 = Helper.getECPointFromString(sendersRequest.get(Sender.OUTPUT + "1").toString());

                    Yi.add(Y1);
                    Yi.add(Y2);


                    lockHeightVerifying = new BigInteger(sendersRequest.get(Sender.LOCKHEIGHT).toString());
                    xG = Helper.getECPointFromString(sendersRequest.get(Sender.XG).toString());
                    signature = Helper.getSignatureFromString(sendersRequest.get(Sender.SIGNATURE).toString());


                    Helper.writeLog("Transaction file is parsed");
//                Helper.verifySignature()
//                System.out.println(rsG.toString());
//                System.out.println(sendersRequest.get(Sender.RSG));
//                System.out.println(rsG.equals(Sender.rsG));

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }

            private static boolean verify() {
                ECPoint X= Xi.get(0);
                for (int i = 1; i < Xi.size(); i++) {
                    X=X.add(Xi.get(i)).normalize();
                    System.out.println(X);
                }
                ECPoint Y= Yi.get(0);
                for (int i = 1; i < Yi.size(); i++) {
                    Y=Y.add(Yi.get(i)).normalize();
                    System.out.println(Y);
                }

                ECPoint Y_X=Y.subtract(X).normalize();

                BigInteger e=Helper.getE(lockHeightVerifying,signature.getKG(),xG);

                System.out.println();
                System.out.println(Y_X);
                System.out.println(xG);
                System.out.println(Main.G.multiply(signature.getS()).normalize());
                System.out.println(signature.getKG().add(xG.multiply(e)).normalize());
                return true;
            }
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
                        client.id=clients.size();
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
//                private DataInputStream input;
//                private DataOutputStream output;
                private int id;



                private ConnectedClient(Socket s) throws IOException {
                    socket = s;
//                    System.out.println("new user connected from " + s.getInetAddress().toString());
                    Helper.writeLog("Node "+s.getInetAddress().toString() +" connected");

//                    output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//                    input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                }

                @Override
                public void run() {
                    try {

//                        File file = new File(REQUEST_R);
//                        FileOutputStream fos = new FileOutputStream(file.getPath());
//
//
//
////                            while (socket.isConnected()) {
////                                byte[] bytes= input.readAllBytes();
////                                System.out.println(bytes);
////                                fos.write(bytes);
////                                break;
////                            }
////                        FileOutputStream fos = new FileOutputStream("test.json");requestR
////                        FileOutputStream fos = new FileOutputStream("requestR.json");
//////                        System.out.println(socket.isClosed());
////
//                                int in = input.read();
////                                System.out.println(in);
//                                while (in != (int)'/') {
////                                System.out.println(buffer);
//                                    fos.write(in);
//                                    in = input.read();
////                                    System.out.println(in);
////                                output.write(buffer, 0, count);
//                                }
                        int in = socket.getInputStream().read();
                        if(in==0){
                            //request for transfer
                            Helper.getFile(REQUEST_R,socket);
                            Helper.writeLog("Request file is received");
                            parseRequestFile();
                            prepareResponseFile();
                        }
                        else if(in==1){
                            Helper.getFile(Sender.TRANSACTION,socket);
                            Recipient.Verifier.parseTransactionFile();
                            Recipient.Verifier.verify();
                            Helper.writeLog("Transaction file is received");
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


//                        while (!socket.isClosed());

                    } catch (IOException e) {
                        System.out.println(e.toString());
                    } finally {

                        server.clients.remove(this.id);
                        Helper.writeLog("Node "+  socket.getInetAddress().toString()+" disconnected");
                    }
                }

//                public void send(String s) throws IOException {
//                    output.write(s.getBytes());
////                    output.flush();
//                }
            }
        }

        private static void parseRequestFile() {


            JSONParser jsonParser = new JSONParser();

            try (FileReader reader = new FileReader(REQUEST_R)) {
                //Read JSON file
                Object obj = jsonParser.parse(reader);

                JSONObject sendersRequest = (JSONObject) obj;

                amountToSend=new BigInteger(sendersRequest.get(Sender.AMOUNT_TO_SEND).toString());
                uuid=new BigInteger(sendersRequest.get(Sender.UUID).toString());
                lockHeight=new BigInteger(sendersRequest.get(Sender.LOCKHEIGHT).toString());

                xSG =Helper.getECPointFromString(sendersRequest.get(Sender.XSG).toString());
                kSG =Helper.getECPointFromString(sendersRequest.get(Sender.KSG).toString());
//                co=Helper.getECPointFromString(sendersRequest.get(Sender.CO).toString());

                Helper.writeLog("Request file is parsed");
//                System.out.println(rsG.toString());
//                System.out.println(sendersRequest.get(Sender.RSG));
//                System.out.println(rsG.equals(Sender.rsG));

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }

        private static void prepareResponseFile(){
            recipientOutput =Main.G.multiply(xR).add(Main.H.multiply(amountToSend)).normalize();
            Helper.writeLog("Recipient output: "+ recipientOutput.toString());

            sR =Helper.getPartialSignature(lockHeight, kSG, xSG, kRG, xRG, kR, xR);

            Helper.writeLog("Recipient new blinding factor for output (xR): "+ xR +"\nPoint xRG: "+ xRG +"\nRecipient's nonce (kR): "+ kR +"\nPoint kRG: "+ kSG);
            Helper.writeLog("Recipient Schnorr signature (sR): "+ sR);



            JSONObject jo = new JSONObject();
            jo.put(SR, sR.toString());
            jo.put(KRG, kRG.toString());
            jo.put(XRG, xRG.toString());
            jo.put(RECIPIENTOUTPUT, recipientOutput.toString());
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
            Helper.writeLog("Response file is created");
        }

        private static void sendResponseFile() throws IOException {
//            File file = new File(RESPONSE_R);


//            byte[] fileContent = Files.readAllBytes(Path.of(RESPONSE_R));

//            System.out.println(6);


//            InputStream in = new FileInputStream(RESPONSE_R);
//            OutputStream out = server.clients.get(0).socket.getOutputStream();
//            byte[] bytes = new byte[16 * 1024];
//
//            int count;
//            while ((count = in.read(bytes)) > 0) {
//                out.write(bytes, 0, count);
//            }
//            out.write("/".getBytes());
            Helper.sendFile(RESPONSE_R,server.clients.get(0).socket);
            Helper.writeLog("Response file is sent\n\n\n");
//            System.out.println(6.5);

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


//        Logs
//                connention
//                forming
//                send

        Helper.prepareLogger();
        Helper.writeLog("Start");

        Recipient.makeConnection();
        Sender.makeConnection();
//        System.out.println(1);
        Sender.sendToRecipient();
        Sender.makeTransaction();
        Sender.makeTransactionFile();

        Recipient.Verifier.parseTransactionFile();
        Recipient.Verifier.verify();


//        Sender.makeConnection();

//        Sender.prepareRequest();
//        Recipient.parceRequestFile();
//        Sender.getResponse();
    }
//    User.Sender.Send(addressToSend,amount);
}
