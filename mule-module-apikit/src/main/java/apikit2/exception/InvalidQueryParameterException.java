package apikit2.exception;

public class InvalidQueryParameterException extends BadRequestException
{

    public InvalidQueryParameterException(String message)
    {
        super(message);
    }
}
