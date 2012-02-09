package gr.infolytics.recommendations.web;

import gr.infolytics.recommendations.entity.Tenant;

import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;


public class NotificationManagerImpl implements NotificationManager {

	private JavaMailSender mailSender;
	private VelocityEngine velocityEngine;
	
	public void setMailSender(JavaMailSender mailSender) {
      this.mailSender = mailSender;
   }

   public void setVelocityEngine(VelocityEngine velocityEngine) {
      this.velocityEngine = velocityEngine;
   }
    
	public void sendConfirmationEmail(final Tenant tenant) {
	        
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
				InternetAddress[] addresses = new InternetAddress[2];
				addresses[0] = new InternetAddress(tenant.getEmail());
				addresses[1] = new InternetAddress("info@cleargist.com");
				message.setTo(addresses);
				message.setFrom("signup@cleargist.com"); // could be parameterized...
				message.setSubject("Welcome to ClearGist Personalization Services");
				Map<String,Tenant> model = new HashMap<String, Tenant>();
				model.put("tenant", tenant);
				String text = VelocityEngineUtils.mergeTemplateIntoString(
						velocityEngine, "/signup_email.vm", model);
				message.setText(text, true);
			}
		};
		this.mailSender.send(preparator);
		
	}
	
	public void sendResetPassEmail(final Tenant tenant) {
        
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
				InternetAddress[] addresses = new InternetAddress[2];
				addresses[0] = new InternetAddress(tenant.getEmail());
				addresses[1] = new InternetAddress("info@cleargist.com");
				message.setTo(addresses);
				message.setFrom("info@cleargist.com"); // could be parameterized...
				message.setSubject("ClearGist Forgot Password");
				Map<String,Tenant> model = new HashMap<String, Tenant>();
				model.put("tenant", tenant);
				String text = VelocityEngineUtils.mergeTemplateIntoString(
						velocityEngine, "/reset_pass_email.vm", model);
				message.setText(text, true);
			}
		};
		this.mailSender.send(preparator);
		
	}
	
}
