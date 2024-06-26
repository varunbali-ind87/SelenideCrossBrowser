package assertionhandler;

public class CustomAssertion extends AssertionError
{
    private String message;

    public CustomAssertion(String message)
    {
        super(message);
        this.message = message;
    }

    @Override
    public String toString()
    {
        String baseMsg = message + System.lineSeparator();
        String stacktrace = super.getMessage();
        return stacktrace.startsWith("java.") ? String.join(baseMsg, stacktrace.substring(26))
                : String.join(baseMsg, stacktrace);
        //return String.join(baseMsg, stacktrace);
    }
}
