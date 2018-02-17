package biz.jovido.fenicesfa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

/**
 * @author Stephan Grundner
 */
@Controller
public class ContactController {

    private static final Logger LOG = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    public JavaMailSender mailSender;

    @ModelAttribute
    protected ContactForm form() {
        return new ContactForm();
    }

    @GetMapping(path = "/contact")
    protected String index(@ModelAttribute ContactForm form) {

        return "contact";
    }

    @PostMapping(path = "/contact")
    protected String sent(@Valid @ModelAttribute ContactForm form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        if (!bindingResult.hasErrors() && !form.isSent()) {
            form.setSent(true);

            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo("stephan.grundner@gmail.com");
                mail.setFrom("stephan.grundner@gmail.com");
//                mail.setTo("info@fenicesfa.it");
//                mail.setFrom("info@fenicesfa.it");
                mail.setReplyTo(String.format("\"%s\" <%s>", form.getName(), form.getEmail()));
                mail.setSubject(String.format("Message from %s ", form.getName()));

                StringBuilder textBuilder = new StringBuilder()
                        .append(form.getMessage());

                mail.setText(textBuilder.toString());

                mailSender.send(mail);
            } catch (Exception e) {
                LOG.error("Error on sending mail", e);
            }
        }

        return "contact";
    }
}
