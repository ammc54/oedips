package oedips.challenge.model;

/**
 * Simple Error model.
 */
public class ErrorMessage {

    private String errorMessage;
    /**
     * Creates a standard error message.
     * @param errorMessage
     */
    public ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return String.format("{ \"Error\" : \"%s\"}",this.errorMessage);
    }
}
