package clases;

public class Main {

	public static long inicioSegundamano, inicioMilanuncios, finalSegundamano, finalMilanuncios;
	public static void main(String[] args) {
		
		//Llamamos a la clase Segundamano, para hacerle el Scraping
		
		
		Segundamano scrapingSegundamano = new Segundamano();
		//Milanuncios scrapingMilanuncios = new Milanuncios();
		
		
		try {
			
			scrapingSegundamano.start();
			//scrapingMilanuncios.start();
			
			scrapingSegundamano.join();
			//scrapingMilanuncios.join();
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//ZilyoApi api = new ZilyoApi();
		//api.iniciarApi();
		
	}
}
