package clases;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;  
import java.util.StringTokenizer;

import javax.mail.Message;  
import javax.mail.MessagingException;  
import javax.mail.PasswordAuthentication;
import javax.mail.Session;  
import javax.mail.Transport;  
import javax.mail.internet.InternetAddress;  
import javax.mail.internet.MimeMessage;  
  
public class Mailing {  
	
    //private final Properties properties = new Properties();  
      
    //private String password;  
  
    //private Session session; 
	
    private  String username,contraseña;
    private  Properties p;
    private  Session sesion;
  
    public static ArrayList<String> correos = new ArrayList<String>();
        
    public void setup() throws MessagingException {
 
    	 //datos de conexion
        username = "scrapingproject@gmail.com";
        contraseña = "scraping2015";
        //propiedades de la conexion
        p = new Properties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.host", "smtp.gmail.com");
        p.put("mail.smtp.port", "587");

        //creamos la sesion
        sesion = crearSesion();
    }

    public  Session crearSesion() {
        Session session = Session.getInstance(p,
          new javax.mail.Authenticator() {
            @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, contraseña);
                }
          });
        return session;
    }  
    
    public void crearMensaje(String txtMensaje){
    	
    	Date fecha = new Date();
    	Message mensaje = new MimeMessage(sesion);
    	String hola ="alex-08_89@hotmail.com,alex.camino.89@gmail.com,";
    	
    	try {
    		
    		if(hola.length()!=0){
    			
    			pruebaCorreos(hola);
    		}
    		
    		for(int i=0;i<correos.size();i++){
    			
    			System.out.println("HOLAA");
    			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correos.get(i)));
  
    		}	
    		
	    	mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress("scrapingproject@gmail.com"));
	    	mensaje.setSubject("Datos sobre Scraping --- "+fecha.toString());
	    	mensaje.setText(txtMensaje);
	    	//Enviamos el Mensaje
	    	Transport.send(mensaje);
	    	System.out.println("Email con toda la INFO del Scraping se ha enviado correctamente");;
    	} catch (Exception ex) {
    		
    		System.out.println("ERROR AL ENVIAR EL CORREO");
    		System.out.println("ERROR: "+ex.getMessage()+"\nTraza: "+ex.getStackTrace());
    	}
    }
    
    public static void pruebaCorreos(String hola){
		
		
		
		StringTokenizer tokens=new StringTokenizer(hola,",");
		
		
		while(tokens.hasMoreTokens()){
	        
			String cadena= tokens.nextToken();
			
			if(!cadena.equals("")){
				
				correos.add(cadena);
			}
	    }
		
	}
}