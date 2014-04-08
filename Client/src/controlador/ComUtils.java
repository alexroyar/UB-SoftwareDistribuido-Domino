package controlador;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ComUtils {
    // Atributos.
    private DataInputStream input;   // Lectura de datos.
    private DataOutputStream output; // Escritura de datos.
    private Socket socket;           // DataSocket.
    
    //Constructores.
    
    /**
     * Constructor de ComUtils.
     * @param socket DataSocket.
     */
    public ComUtils(Socket socket) {
        this.socket = socket;
    }
    
    /**
     * Segunda parte del constructor, para parámetros conflictivos.
     */
    public void init(){
        try {
            this.input = new DataInputStream(this.socket.getInputStream());        
            this.output = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(ComUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Métodos.
    
    public byte[] char_to_bytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }
    
    /* Llegir un enter de 32 bits */
    public int read_int32() throws IOException {
        byte bytes[] = new byte[4];
        bytes = read_bytes(4);

        return bytesToInt32(bytes, "be");
    }

    /* Escriure un enter de 32 bits */
    public void write_int32(int number) throws IOException {
        byte bytes[] = new byte[4];

        int32ToBytes(number, bytes, "be");
        output.write(bytes, 0, 4);
    }

    
    public void write_byte(Byte by) throws IOException {
        //System.out.println("BYTE: " + by);
        output.writeByte(by);
    }

    public void write_char(int i) throws IOException {
        output.writeByte(Character.forDigit(i, 10));
    }

    public void write_char(char i) throws IOException {
        output.writeByte((byte)i);
    }
    
    public char read_char() throws IOException {
        return (char)input.readByte();
    }
    
    public byte read_byte() throws IOException{
        return this.read_bytes(1)[0];
    }
    /* Llegir un string de mida STRSIZE */

    public String read_string() throws IOException {
        String str;
        byte bStr[] = new byte[Rules.STRSIZE];
        char cStr[] = new char[Rules.STRSIZE];

        bStr = read_bytes(Rules.STRSIZE);

        for (int i = 0; i < Rules.STRSIZE; i++) {
            cStr[i] = (char) bStr[i];
        }

        str = String.valueOf(cStr);

        return str.trim();
    }

    /* Escriure un string */
    public void write_string(String str) throws IOException {
        int numBytes, lenStr;
        byte bStr[] = new byte[Rules.STRSIZE];

        lenStr = str.length();

        if (lenStr > Rules.STRSIZE) {
            numBytes = Rules.STRSIZE;
        } else {
            numBytes = lenStr;
        }

        for (int i = 0; i < numBytes; i++) {
            bStr[i] = (byte) str.charAt(i);
        }

        for (int i = numBytes; i < Rules.STRSIZE; i++) {
            bStr[i] = (byte) ' ';
        }

        output.write(bStr, 0, Rules.STRSIZE);
    }

    /* Passar d'enters a bytes */
    private int int32ToBytes(int number, byte bytes[], String endianess) {
        if ("be".equals(endianess.toLowerCase())) {
            bytes[0] = (byte) ((number >> 24) & 0xFF);
            bytes[1] = (byte) ((number >> 16) & 0xFF);
            bytes[2] = (byte) ((number >> 8) & 0xFF);
            bytes[3] = (byte) (number & 0xFF);
        } else {
            bytes[0] = (byte) (number & 0xFF);
            bytes[1] = (byte) ((number >> 8) & 0xFF);
            bytes[2] = (byte) ((number >> 16) & 0xFF);
            bytes[3] = (byte) ((number >> 24) & 0xFF);
        }
        return 4;
    }

    /* Passar de bytes a enters */
    private int bytesToInt32(byte bytes[], String endianess) {
        int number;

        if ("be".equals(endianess.toLowerCase())) {
            number = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
                    | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        } else {
            number = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8)
                    | ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        }
        return number;
    }

    //llegir bytes.
    public byte[] read_bytes(int numBytes) throws IOException {
        int len = 0;
        byte bStr[] = new byte[numBytes];
        do {
            len += input.read(bStr, len, numBytes - len);
        } while (len < numBytes);
        return bStr;
    }

    /* Llegir un string  mida variable size = nombre de bytes especifica la longitud*/
    public String read_string_variable(int size) throws IOException {
        byte bHeader[] = new byte[size];
        char cHeader[] = new char[size];
        int numBytes = 0;

        // Llegim els bytes que indiquen la mida de l'string
        bHeader = read_bytes(size);
        // La mida de l'string ve en format text, per tant creem un string i el parsejem
        for (int i = 0; i < size; i++) {
            cHeader[i] = (char) bHeader[i];
        }
        numBytes = Integer.parseInt(new String(cHeader));

        // Llegim l'string
        byte bStr[] = new byte[numBytes];
        char cStr[] = new char[numBytes];
        bStr = read_bytes(numBytes);
        for (int i = 0; i < numBytes; i++) {
            cStr[i] = (char) bStr[i];
        }
        return String.valueOf(cStr);
    }

    /* Escriure un string mida variable, size = nombre de bytes especifica la longitud  */
    /* String str = string a escriure.*/
    public void write_string_variable(int size, String str) throws IOException {

        // Creem una seqüència amb la mida
        byte bHeader[] = new byte[size];
        String strHeader;
        int numBytes = 0;

        // Creem la capçalera amb el nombre de bytes que codifiquen la mida
        numBytes = str.length();

        strHeader = String.valueOf(numBytes);
        int len;
        if ((len = strHeader.length()) < size) {
            for (int i = len; i < size; i++) {
                strHeader = "0" + strHeader;
            }
        }
        System.out.println(strHeader);
        for (int i = 0; i < size; i++) {
            bHeader[i] = (byte) strHeader.charAt(i);
        }
        // Enviem la capçalera
        output.write(bHeader, 0, size);
        // Enviem l'string writeBytes de DataOutputStrem no envia el byte més alt dels chars.
        output.writeBytes(str);
    }
}
