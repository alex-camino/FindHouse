package clases;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.Statement;

import clases.Conexiones;


public class Milanuncios extends Thread{

    public static Connection conexion;
    public static String rutaCarpetaImagenes;
    public static ArrayList<String> listaFotos = new ArrayList<String>();
    public static ArrayList<String> contenedorAnuncio = new ArrayList<String>();
    
 	public void run()
 	{
 		Main.inicioMilanuncios=System.currentTimeMillis();
 		iniciarScraping();
 		System.out.println("Scraping a la web Milanuncios.com ha terminado");
 		Main.finalMilanuncios=System.currentTimeMillis();
 	}
	public static void iniciarScraping() 
	{
		
		//Llamamos al metodo realizar conexion para poder conectarnos a la BD.
		conexion=Conexiones.realizarConexion();
		
		/*
		 * Array URLS:
		 * 	
		 * 		Posicion 0: Alquiler
		 * 		Posicion 1: Venta
		 * 	
		 * */
		String[] localidades = {"Javea", "Denia", "Calpe", "Benidorm"};
		
		for(int i=0;i<localidades.length;i++){
			
			if(i==0){
				
				obtenerInfo("http://www.milanuncios.com/venta-de-viviendas-en-javea|xabia-alicante/", 1, localidades[i]);
				obtenerInfo("http://www.milanuncios.com/alquiler-de-viviendas-en-javea|xabia-alicante/", 2, localidades[i]);
				
			}else{
				
				obtenerInfo("http://www.milanuncios.com/venta-de-viviendas-en-"+localidades[i]+"-alicante/", 1, localidades[i]);
				obtenerInfo("http://www.milanuncios.com/alquiler-de-viviendas-en-"+localidades[i]+"-alicante/", 2, localidades[i]);
			}
			
		}
				
	}
	
	/*
	 * METODO: ObtenerInfo
	 * Acciones: Se encarga de conectar con la página web de milanuncios y hacer el Scraping
	 * para obtener todas las URL de cada anuncio. Luego ire conectandose a la url de los detalles de cada anuncio
	 * para poder obtener la información que nosotros quereamos.
	 * 
	 * DEVUELVE: ArrayList con:
	 * 				Posicion 0: urlDetalles
	 * 				Posicion 1: referencia
	 * 				Posicion 2: fecha 
	 * 				Posicion 3: titulo 
	 * 				Posicion 4: categoria
	 * 				Posicion 5: descripcion
	 * 				Posicion 6: precioReal   
	 * 				Posicion 7: numHabitaciones
	 * 				Posicion 8: vecesListado
	 * 				Posicion 9: Superficie
	 * */
	public static void obtenerInfo(String urlWeb, int operacion, String poblacion){
		
		String url="http://www.milanuncios.com";
		String urlDetalles="", referencia="", fecha="", titulo="", categoria="", descripcion="", txtPrecio="", precioReal="", txtHabitaciones="",
			   numHabitaciones="", superficie="", vecesListado="";
		ArrayList<String> arrayPaginas = new ArrayList<String>();
		

		try {
			
			Document doc = Jsoup.connect(urlWeb)
					.userAgent("Mozilla/5.0")
					.post();
			
			
			//Añado una posicion vacia para poder recorrer la pagina 1 de los anuncios, que es en la que nos encontramos.
			arrayPaginas.add("");
			
			//Obtener cuantas paginas hay.
			Elements listaPaginas = doc.getElementsByClass("p2");
			
			for(Element anuncio : listaPaginas){
				
				arrayPaginas.add(anuncio.getElementsByAttribute("href").attr("href"));	
				
			}
			
			//Recorrer cada pagina obteniendo los anuncios.
			for(int x=0;x<(arrayPaginas.size()-1);x++){
				
				if(x==0){
					
					doc = Jsoup.connect(urlWeb+"?demanda=n")
							.userAgent("Mozilla/5.0")
							.post();
				}else{
					
					
					doc = Jsoup.connect(urlWeb+arrayPaginas.get(x)+"&demanda=n")
							.userAgent("Mozilla/5.0")
							.post();
				}
				
				
				Elements listaAnuncios = doc.getElementsByClass("x1");
				
				for(Element anuncio : listaAnuncios){
					
					//Reiniciamos los arraylist y algunas variables.
					listaFotos.clear();
					contenedorAnuncio.clear();
					precioReal="";
					numHabitaciones="";
					
					urlDetalles=url+anuncio.getElementsByClass("cti").attr("href");	
					
					//Recorremos la url de los detalles de cada anuncio.
					Document docDetalles = Jsoup.connect(urlDetalles)
							.userAgent("Mozilla/5.0")
							.post();
					
					//Obtener fotos
					Elements fotos = docDetalles.getElementsByClass("pagAnuFotoBox");
					
					//Si tiene fotos el anuncio lo guardamos.
					if(fotos.size()!=0){
						
						//Obtener la referencia
						Elements numReferencia = docDetalles.getElementsByClass("anuRefBox");
						
						for(Element detalle : numReferencia){
							
							referencia=detalle.getElementsByTag("b").text();
											
						}
						
						fecha=docDetalles.getElementsByClass("anuFecha").text();
						titulo=docDetalles.getElementsByClass("pagAnuTituloBox").text();
						categoria=docDetalles.getElementsByClass("anuTitulo").text();
						descripcion=docDetalles.getElementsByClass("pagAnuCuerpoAnu").text();
						
						//Obtener Precio
						txtPrecio = docDetalles.getElementsByClass("pr").text();
						
						if(txtPrecio.equals("")){
							
							txtPrecio="0";
						}
						for(int i=0;i<txtPrecio.length();i++){
							
							if(Character.isDigit(txtPrecio.charAt(i))){
								
								precioReal+=txtPrecio.charAt(i);
							}
						}
						
						
						//Obtener Metros 2
						superficie=docDetalles.getElementsByClass("m2").text();
						
						if(superficie.equals("")){
							
							superficie="0 m2";
						}
								
						//Obtener Habitaciones
						txtHabitaciones=docDetalles.getElementsByClass("dor").text();
						
						if(txtHabitaciones.equals("")){
							
							txtHabitaciones="0";
						}
						for(int i=0;i<txtHabitaciones.length();i++){
							
							if(Character.isDigit(txtHabitaciones.charAt(i))){
								
								numHabitaciones+=txtHabitaciones.charAt(i);
							}
						}
					
						//Obtener veces listado
						Elements elementoVecesListado = docDetalles.getElementsByClass("dato");
						vecesListado=elementoVecesListado.get(0).getElementsByTag("strong").text();
						
						//Obtener fotos
						fotos = docDetalles.getElementsByClass("pagAnuFotoBox");
							
						for(Element detalle : fotos){
						
							Elements urlImagenes = detalle.getElementsByClass("pagAnuFoto");
						
							for(Element detalleUrl : urlImagenes){
								
								listaFotos.add(detalleUrl.getElementsByTag("img").attr("src"));
							}						
						}
						
								
						
						//Añadir variables al ContenedorAnuncio
						contenedorAnuncio.add(urlDetalles);
						contenedorAnuncio.add(referencia);
						contenedorAnuncio.add(fecha);
						contenedorAnuncio.add(titulo);
						contenedorAnuncio.add(categoria);
						contenedorAnuncio.add(descripcion);
						contenedorAnuncio.add(precioReal);
						contenedorAnuncio.add(numHabitaciones);
						contenedorAnuncio.add(vecesListado);
						contenedorAnuncio.add(superficie);
						
						insertarInmueble(operacion, poblacion);
					}
					
				}	
			}
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de milanuncios.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
			
			/*
			System.out.println("Esto es lo que contiene el ultimo anuncio.");
			for(int i=0;i<contenedorAnuncio.size();i++){
				
				System.out.println(contenedorAnuncio.get(i));
			}*/
		}		
	}
	
	
	/*
	 * METODO: addInfoBD
	 * ACCIONES: Recibe las caracteristicas del anuncio y crea un inmueble a partir de todas ellas.
	 * 
	 * */
	public static void insertarInmueble( int operacion, String poblacion){
		
		int codigoInmueble=0;
		
		try{
						
				//INSERTAR INMUEBLE
			
				PreparedStatement pstmt= conexion.prepareStatement("insert into inmuebles(in_titulo, in_url, in_fecha, in_categoria, in_operacion"
						+ ", in_idInmueble, in_web) values(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
				
				pstmt.setString(1, contenedorAnuncio.get(3));
				pstmt.setString(2, contenedorAnuncio.get(0));
				pstmt.setString(3, contenedorAnuncio.get(2));
				pstmt.setInt(4, detectarCategoria());
				pstmt.setInt(5, operacion);
				pstmt.setString(6, contenedorAnuncio.get(1));
				pstmt.setString(7, "Milanuncios");
				
				pstmt.executeUpdate();
				
				//Obtener el ID del inmueble creado anteriormente.
				ResultSet idGenerado = pstmt.getGeneratedKeys();
				while(idGenerado.next()){
					
					codigoInmueble=idGenerado.getInt(1);
				}
				idGenerado.close();
				// FIN INSERTAR INMUEBLE ----------------------------------------
				
				insertarDetallesInmueble(poblacion, codigoInmueble);
			
				pstmt.close();
			
		}catch(SQLException ex){
			
			System.out.println("Error al insertar el inmueble en la Base de Datos, anuncio no insertado.");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
			if(ex.getErrorCode()==1452){
				
				System.out.println("Se ha intentado duplicar un anuncio. El anuncio no se ha insertado.");
			}
		}
	}
	
	
	/*
	 * METODO PARA INSERTAR LOS DETALLES DEL INMUEBLE
	 * */
	public static void insertarDetallesInmueble(String poblacion, int codigoInmueble){
		
		int codigoDetallesInmueble=0;
		
		try{
			
			PreparedStatement pstmt= conexion.prepareStatement("insert into detallesInmueble(det_codInmueble, det_descripcion, det_habitaciones,"
					+"det_superficie, det_poblacion, det_vecesVisitado, det_precio) values(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			

			pstmt.setInt(1, codigoInmueble);
			pstmt.setString(2, contenedorAnuncio.get(5));
			pstmt.setInt(3,Integer.parseInt(contenedorAnuncio.get(7)));
			pstmt.setString(4, contenedorAnuncio.get(9));
			pstmt.setString(5, poblacion);
			pstmt.setInt(6, Integer.parseInt(contenedorAnuncio.get(8)));
			pstmt.setInt(7, Integer.parseInt(contenedorAnuncio.get(6)));	
			pstmt.executeUpdate();
			
			//Obtener el ID del inmueble creado anteriormente.
			ResultSet idGenerado = pstmt.getGeneratedKeys();
			while(idGenerado.next()){
				
				codigoDetallesInmueble=idGenerado.getInt(1);
			}
			
			conexion.commit();
			idGenerado.close();
			pstmt.close();
			
			//Descargamos las imagenes del anuncio.
			descargaImagenes(contenedorAnuncio.get(1), codigoInmueble, codigoDetallesInmueble);
			
			
		}catch(SQLException ex){
			
			System.out.println("Error al insertar los detalles del inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
			
			try {
				
				conexion.rollback();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}catch(IndexOutOfBoundsException ex){
		
			System.out.println("Indice fuera de los limites, los detalles del anuncio no se han insertado, el anuncio se borrará");
			
			try {
				
				conexion.rollback();
	
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static int detectarCategoria(){
		
		String categoria=contenedorAnuncio.get(4);
		
		if(categoria.contains("casa"))
			return 1;
		else if(categoria.contains("chale"))
			return 2;
		else if(categoria.contains("piso"))
				return 3;
		else if(categoria.contains("vacacion"))
				return 4;
		else if(categoria.contains("apartamento"))
				return 5;
		else if(categoria.contains("estudio"))
				return 6;
		else if(categoria.contains("ático"))
			return 7;
		else if(categoria.contains("loft"))
			return 9;
		
		return -1;
	}
	
	/*
	 * Metodo que va descargando una a una las imagenes.
	 * */
	public static void descargaImagenes(String idInmueble, int codigoInmueble, int codigoDetallesInmueble){
		
		String rutaCarpeta;
		boolean imagenDescargada;
		int numImagenesDescargadas=0;
		
		if(listaFotos.size()!=0){
			
			//Creamos la carpeta donde guardaremos las imagenes.
			rutaCarpeta=crearCarpetaImagenes(idInmueble, Integer.toString(codigoInmueble));
		
			for(int i=0;i<listaFotos.size()&&numImagenesDescargadas<5;i++){
				
				imagenDescargada=descargaImagenesInmueble(listaFotos.get(i), rutaCarpeta, Integer.toString(numImagenesDescargadas), idInmueble);
				
				if(imagenDescargada)
					numImagenesDescargadas++;
				
			}	
		}
		
		//Actualizar campo numImagenes de la tabla detallesInmueble.
		try{
			
			PreparedStatement pstmt= conexion.prepareStatement("UPDATE detallesInmueble SET det_numImagenes="+numImagenesDescargadas+" where det_codigo="
									+codigoDetallesInmueble);
			pstmt.executeUpdate();
		}catch(SQLException ex){
			
			System.out.println("Error al insertar el numero de Imagenes descargadas, el la tabla detallesInmueble de la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
		}	
	}
	
	public static String crearCarpetaImagenes(String idInmueble, String codInmueble){
		
		
		//String ruta = "/Applications/XAMPP/xamppfiles/htdocs/openshift/imagenesAnuncios/Milanuncios/".concat(codInmueble+"-"+idInmueble);
		String ruta = "/var/lib/openshift/553157b65973ca9f040000fb/app-root/repo/diy/imagenesAnuncios/Milanuncios/".concat(codInmueble+"-"+idInmueble);
		rutaCarpetaImagenes=ruta;
		
		File directorio = new File(ruta);
		directorio.mkdir();
		
		return ruta;
	}
	
	
	public static boolean descargaImagenesInmueble(String src, String rutaCarpeta, String numImagen, String idInmueble){
	
		try{
			
			 URL url = new URL(src);
		        InputStream in = url.openStream();

		        String ruta=rutaCarpeta.concat("/"+numImagen+".jpg");
		        
		        OutputStream out = new BufferedOutputStream(new FileOutputStream(ruta));

		       
		        for (int b; (b = in.read()) != -1;) {
		            out.write(b);
		        }
		        out.close();
		        in.close();

		        ImageResizer nuevaImagen = new ImageResizer();
		        
		        nuevaImagen.cargarImagen(ruta, 1000, 644);
		        
		        return true;
		}catch (IOException e) {
			
			System.out.println("Ha habido un error al intentar descargar la imagen de Milanuncios, ID Inmueble: "+idInmueble);
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
			
			return false;
		}
       
    }
}

