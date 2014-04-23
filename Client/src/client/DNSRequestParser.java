package client;

import request.BitMask;
import request.Header;
import request.Question;
import request.Request;
import utils.ByteArrayConversionTool;

import java.util.List;

/**
 * Created by Julien on 23/04/2014.
 */
public class DNSRequestParser {

    public static Request parseRequest(byte[] unparsedRequest){
        return null;
    }

    public static Header parseHeader(byte[] unparsedRequest){
        Header header = new Header();

        header.ID = ByteArrayConversionTool.byteArrayToInt(unparsedRequest[1],unparsedRequest[0]);

        header.QRisResponse = BitMask.RESPONSE.isSetIn(unparsedRequest[2]);
        header.AA = BitMask.AA.isSetIn(unparsedRequest[2]);
        header.TC = BitMask.TC.isSetIn(unparsedRequest[2]);
        header.RD = BitMask.RD.isSetIn(unparsedRequest[2]);
        header.RA = BitMask.RA.isSetIn(unparsedRequest[2]);

        header.QDCOUNT = ByteArrayConversionTool.byteArrayToInt(unparsedRequest[5], unparsedRequest[4]);
        header.ANCOUNT = ByteArrayConversionTool.byteArrayToInt(unparsedRequest[7], unparsedRequest[6]);
        header.NSCOUNT = ByteArrayConversionTool.byteArrayToInt(unparsedRequest[9], unparsedRequest[8]);
        header.ARCOUNT = ByteArrayConversionTool.byteArrayToInt(unparsedRequest[11], unparsedRequest[10]);

        return header;
    }

    /**
     *
     * @param output : the initialised list to fill with parsed questions
     * @return the number of byte read
     */
    public int  parseAnswers(int number, byte[] unparsedRequest , List<Question> output){
        assert output != null;

        int byteRead=0;
        int wordLength;
        String currentWord="";
        Question question;

        for(int cursor = 12; cursor<unparsedRequest.length;cursor++){
            for(int q =0; q<number;q++) {
                question = new Question();
                while (unparsedRequest[cursor] != 0) {
                    wordLength = unparsedRequest[cursor];
                    cursor += wordLength;

                    for (int i = 0; i < wordLength; i++)
                        currentWord += (char) unparsedRequest[i];

                    question.QNAME.append(currentWord+".");
                    byteRead += cursor;
                }
                question.QTYPE = String.valueOf(ByteArrayConversionTool.byteArrayToInt(unparsedRequest[cursor+1], unparsedRequest[cursor]));
                question.QCLASS = String.valueOf(ByteArrayConversionTool.byteArrayToInt(unparsedRequest[cursor+3], unparsedRequest[cursor+2]));

                output.add(q,question);
            }
        }
        return byteRead;
    }
}
