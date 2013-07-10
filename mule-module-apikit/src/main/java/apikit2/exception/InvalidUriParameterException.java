package apikit2.exception;

public class InvalidUriParameterException extends BadRequestException
{

    public InvalidUriParameterException(String message)
    {
        super(message);
    }
}
