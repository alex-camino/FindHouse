package clases;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {

	public static long inicioSegundamano, inicioMilanuncios, finalSegundamano, finalMilanuncios;
	public static void main(String[] args) {
		
		//Llamamos a la clase Segundamano, para hacerle el Scraping
		
		/*
		Segundamano scrapingSegundamano = new Segundamano();
		Milanuncios scrapingMilanuncios = new Milanuncios();
		
		
		try {
			
			scrapingSegundamano.start();
			//scrapingMilanuncios.start();
			
			scrapingSegundamano.join();
			//scrapingMilanuncios.join();
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		Connection conexion=Conexiones.realizarConexion();
		
		PreparedStatement pstmt;
		
		try {
			pstmt = conexion.prepareStatement("insert into inmuebles(in_titulo) values(?)");
			pstmt.setString(1, "Esto es una prueba");
			pstmt.executeUpdate();
			
			pstmt.close();
		
		}catch(SQLException ex){
			
			System.out.println("Error al insertar el inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
		}
			
		System.out.println("HOLA MUNDO");
		
		//ZilyoApi api = new ZilyoApi();
		//api.iniciarApi();
		
	}
}
