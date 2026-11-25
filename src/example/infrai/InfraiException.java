package example.infrai;

public class InfraiException extends Exception {
    private final int status;
    public InfraiException(String message, int status) { super(message); this.status = status; }
    public int status() { return status; }
}
