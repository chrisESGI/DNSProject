package client;

import request.BitMask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: FuglyLionKing
 * Date: 22/04/14
 * Time: 09:59
 * To change this template use File | Settings | File Templates.
 */
public class Resolver {

    private static int port = 53;
    private static int buffSize = 256;

    byte[] generateId() {
        byte[] id = new byte[2];
        Random rnd = new Random();
        rnd.nextBytes(id);

        return id;
    }

    int populate(byte[] message, String url) throws UnsupportedEncodingException {
        byte[] id = generateId();


        byte first = 0,
                second = 0;

        first |= BitMask.QUERY.mask | BitMask.OP_QUERY.mask | BitMask.RD.mask;

        int QDCOUNT = 1;    //     an unsigned 16 bit integer specifying the number of entries in the question section.
        int ANCOUNT = 0;    //     an unsigned 16 bit integer specifying the number of resource records in the answer section.
        int NSCOUNT = 0;    //     an unsigned 16 bit integer specifying the number of name server resource records in the authority records section.
        int ARCOUNT = 0;    //     an unsigned 16 bit integer specifying the number of resource records in the additional records section.


        System.arraycopy(id, 0, message, 0, 2);
        message[2] = first;
        message[3] = second;
        System.arraycopy(id, 0, message, 0, 2);
        System.arraycopy(shorty(QDCOUNT), 0, message, 4, 2);
        System.arraycopy(shorty(ANCOUNT), 0, message, 6, 2);
        System.arraycopy(shorty(NSCOUNT), 0, message, 8, 2);
        System.arraycopy(shorty(ARCOUNT), 0, message, 10, 2);


        int offset = 12;
        //add question
        byte[] question = asQuestionName(url,1,1);
        byte[] QTYPE = new byte[2];
        byte[] QCLASS = new byte[2];
        System.arraycopy(question, 0, message, offset, question.length);
        offset += question.length;
        System.arraycopy(QTYPE, 0, message, offset, QTYPE.length);
        offset += QTYPE.length;
        System.arraycopy(QCLASS, 0, message, offset, QCLASS.length);
        offset += QCLASS.length;

        return offset;
    }

    byte[] ask(String dnsAdr, String url) throws IOException {
        final DatagramSocket dnsServer = new DatagramSocket(port);

        byte[] message = new byte[buffSize];



        int offset = populate(message,url);

        final DatagramPacket packet = new DatagramPacket(message, message.length, InetAddress.getByName(dnsAdr), port);
        final DatagramPacket received = new DatagramPacket(new byte[buffSize], offset);

        final Object wait = new Object();


        dnsServer.send(packet);
        dnsServer.receive(received);
        return received.getData();
    }

    byte[] asQuestionName(String adr,int type, int clazz) throws UnsupportedEncodingException {

        String[] splittedAdr = adr.split("\\.");


        int offset = 0;
        byte[] answer = new byte[50];

        for (String label : splittedAdr) {
            byte[] labelBytes = asciiByteTable(label);
            addBytes(answer, shorty((short) labelBytes.length), offset++);
            addBytes(answer, labelBytes, offset);
            offset += labelBytes.length;
        }
        addBytes(answer, shorty((short) 0), offset++);

        addBytes(answer,shorty(type),offset);
        offset+=2;
        addBytes(answer,shorty(clazz),offset);
        offset+=2;

        return Arrays.copyOfRange(answer, 0, ++offset);
    }

    byte[] addBytes(byte[] dest, byte[] src, int offset) {
        byte[] ans;

        if (offset + src.length <= dest.length) {
            ans = dest;
        } else {
            ans = Arrays.copyOfRange(dest, 0, dest.length * 2);
        }

        System.arraycopy(src, 0, ans, offset, src.length);

        return ans;
    }


    byte[] asciiByteTable(String str) throws UnsupportedEncodingException {
        return str.getBytes("ascii");
    }


    byte[] shorty(int number) {
        byte[] array = ByteBuffer.allocate(4).putInt(number).array();

        return Arrays.copyOfRange(array, 2, 4);
    }

    byte[] shorty(short number) {
        byte[] array = ByteBuffer.allocate(2).putShort(number).array();

        return new byte[]{array[1]};
    }

}
