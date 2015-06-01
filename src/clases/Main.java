package clases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.mail.MessagingException;

public class Main {
	
	public static Connection conexion;

	//public static long inicioSegundamano, inicioMilanuncios, finalSegundamano, finalMilanuncios;
	public static ArrayList<String> mensajesError = new ArrayList<String>();
	public static ArrayList<String[]> listaCoordenadas = new ArrayList<String[]>();
	public static ArrayList<String> listaLocalidades = new ArrayList<String>();
	public static int numPaginas, cantAnuncios;
	public static String horaInicio="";
    //	String[] localidades = {"Javea", "Denia", "Calpe", "Benidorm"};
	
	public static void main(String[] args) {
		
		//Reseteamos las listas.
		mensajesError.clear();
		listaCoordenadas.clear();
		listaLocalidades.clear();
		
		Calendar cal1 = Calendar.getInstance();
	    horaInicio=""+cal1.get(Calendar.DATE)+"/"+(cal1.get(Calendar.MONTH)+1)
	    +"/"+cal1.get(Calendar.YEAR)+" "+cal1.get(Calendar.HOUR)
	    +":"+cal1.get(Calendar.MINUTE)+":"+cal1.get(Calendar.SECOND)
	    +":"+cal1.get(Calendar.MILLISECOND);
		
		conexion=Conexiones.realizarConexion();
		
		/* COORDENADAS 
		 *
		 * POS-0: neLatitud
		 * POS-1: neLongitud
		 * POS-2: swLatitud
		 * POS-3: swLongitud
		 */
		String[]   coordenadasJavea    = {"38.806908","0.100792","38.725262","0.241554"},
				   coordenadasDenia    = {"38.858985", "0.032417", "38.825292", "0.110008"},
				   coordenadasCalpe    = {"38.668248", "0.038597", "38.638622", "0.071384"},
				   coordenadasBenidorm = {"38.561884", "-0.164479", "38.536619", "-0.118717"};
		
		String localidad, webs;
		int paginas, anuncios;
		boolean existeSegundamano=false, existeMilanuncios=false, existeApi=false;
		
		
		localidad ="Javea,Denia";
		anuncios = 5;
		paginas = 2;
		webs="Segundamano, Api, Milanuncios";
		
		//Añadir información a las listas
		
		if(localidad.contains("Javea")){
			
			listaLocalidades.add("Javea");
			listaCoordenadas.add(coordenadasJavea);
		}else if(localidad.contains("Calpe")){
			
			listaLocalidades.add("Calpe");
			listaCoordenadas.add(coordenadasCalpe);
		}else if(localidad.contains("Denia")){
			
			listaLocalidades.add("Denia");
			listaCoordenadas.add(coordenadasDenia);
		}else if(localidad.contains("Benidorm")){
				
			listaLocalidades.add("Benidorm");
			listaCoordenadas.add(coordenadasBenidorm);
		}
		
		numPaginas = paginas;
		cantAnuncios = anuncios;
				
			
		try {
			
				Segundamano scrapingSegundamano = null;
				Milanuncios scrapingMilanuncios = null;
				ZilyoApi  apiZilyo = null;
				
				//CREAMOS LOS HILOS.
				if(webs.contains("Segundamano")){
					
					existeSegundamano = true;
					
					scrapingSegundamano = new Segundamano();
					scrapingSegundamano.start();
					//scrapingSegundamano.iniciarScraping();
				}
				if(webs.contains("Milanuncios")){
					
					existeMilanuncios = true;
					
					scrapingMilanuncios = new Milanuncios();
					scrapingMilanuncios.start();	
					//scrapingMilanuncios.iniciarScraping();
				}
				if(webs.contains("Api")){
					
					existeApi = true;
					
					apiZilyo = new ZilyoApi();
					apiZilyo.start();
					//apiZilyo.iniciarApi();
				}
				
				
				
				//CREAMOS LOS JOIN SOBRE LOS HILOS.
				if(existeSegundamano){
					
					scrapingSegundamano.join();
					
					mensajesError.add("\nAcaba el Scraping a la web de Segundamano");
				}
				
				if(existeMilanuncios){
						
					scrapingMilanuncios.join();
					
					mensajesError.add("\nAcaba el Scraping a la web de Milanuncios");

				}
								
				if(existeApi){
					
					apiZilyo.join();
					
					mensajesError.add("\nAcaba el uso de la Api de Zilyo");

				}
			
				
							
		} catch (InterruptedException ex) {
			
			mensajesError.add("\nError al crear los hilos de las clases"+"\nMessage: "+ex.getMessage()+"\nTraza: "+ex.getStackTrace());
		} 		
		
			//Mandamos el correo con los errores que hayan habido.
			enviarCorreo();
	}
	
	public static void enviarCorreo(){
		
		Calendar cal1 = Calendar.getInstance();
	    String horaFinal=""+cal1.get(Calendar.DATE)+"/"+(cal1.get(Calendar.MONTH)+1)
	    +"/"+cal1.get(Calendar.YEAR)+" "+cal1.get(Calendar.HOUR)
	    +":"+cal1.get(Calendar.MINUTE)+":"+cal1.get(Calendar.SECOND)
	    +":"+cal1.get(Calendar.MILLISECOND);
	    
		String mensajeEnviar="";
		PreparedStatement pstmt;
		int cantInmuebles=0;
		
		try {
			
			pstmt= conexion.prepareStatement("select count(*) as inmuebles from anunciosInsertados");
			ResultSet inmuebles =pstmt.executeQuery();
			
			while(inmuebles.next()){
				
				cantInmuebles= inmuebles.getInt("inmuebles");
			}
			
			//Cerramos la conexion
			conexion.close();
			
			mensajeEnviar = "APP FINDHOUSE --- PORTAL INMOBILIARIO"
						   +"\n=====================================\n"
						   +"\nResultados sobre el Scraping realizado:\n"
						   +"\n- Hora de Inicio: "+horaInicio
						   +"\n- Hora de Finalización: "+horaFinal
						   +"\n- Numero de Inmuebles descargados: "+cantInmuebles
						   +"\n\n\nMensajes de información o de errores acerca del Scraping:"
						         +"=========================================================\n"
						   +"\n";
			
			for(int i=0;i<mensajesError.size();i++){
				
				mensajeEnviar += mensajesError.get(i);
			}
		 
		
			mensajeEnviar += "\n"+"\nInforme finalizado.";
		
			
			Mailing nuevoCorreo = new Mailing();
			nuevoCorreo.setup();
			
			nuevoCorreo.crearMensaje(mensajeEnviar);
		} catch (MessagingException e) {
			
			System.out.println("ERROR AL CREAR EL MENSAJE");
    		System.out.println("ERROR: "+e.getMessage()+"\nTraza: "+e.getStackTrace());
		} catch (SQLException ex) {
			
			System.out.println("Error al consultar la tabla anunciosInsertados de la BD."
							+"Error: "+ex.getErrorCode()+"\nMensaje: "+ex.getMessage());
		}
		
	}
}
