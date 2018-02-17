package biz.jovido.fenicesfa;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author Stephan Grundner
 */
public class ContactForm {

    @Size(message = "Please enter a valid name",
            min = 3, max = 255 * 4)
    private String name;

    @Pattern(message = "Please enter a valid email address",
            regexp = "(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$)")
    private String email;

    private String subject;

    @Size(message = "The message must contain at least 8 characters", min = 8, max = 255 * 16)
    private String message;

    private boolean sent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
