public class InitializationException extends Exception
{
    public InitializationException(String alreadyInitialized) {

        super(alreadyInitialized);
    }
}
