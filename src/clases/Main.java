package clases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {

	public static long inicioSegundamano, inicioMilanuncios, finalSegundamano, finalMilanuncios;
	public static void main(String[] args) {
		
		//Llamamos a la clase Segundamano, para hacerle el Scraping
		
		Segundamano scrapingSegundamano = new Segundamano();
		Milanuncios scrapingMilanuncios = new Milanuncios();
		//ZilyoApi api = new ZilyoApi();
		//api.iniciarApi();
				
		try {
			
			scrapingSegundamano.start();
			scrapingMilanuncios.start();
			
			scrapingSegundamano.join();
			scrapingMilanuncios.join();
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}
