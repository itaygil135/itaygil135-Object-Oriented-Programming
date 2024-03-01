import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Sjavac {
    private static final int FILE_OK = 0;

    private static final int ILLEGAL_RULES_IN_FILE = 1;

    private static final int IO_ERROR = 2;

    public static void main(String[] args) {
        try {
            if(args.length != 1){
                throw new IllegalArgumentException("insufficient arg num");
            }

        FileReader fileReader = new FileReader(args[0]);
        VerificationEngine verificationEngine = new VerificationEngine(fileReader);
        verificationEngine.firstParse();
        verificationEngine.secondParse();
        System.out.println(FILE_OK);
            }

        catch (IOException e)
        {
            System.err.println(e);
            System.out.println(IO_ERROR);
        }
        catch (Exception e)
        {
            System.err.println(e);
            System.out.println(ILLEGAL_RULES_IN_FILE);
        }
    }
}