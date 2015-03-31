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


public class Milanuncios {

    public static Connection conexion;
    public static String rutaCarpetaImagenes;
    public static ArrayList<String> listaFotos = new ArrayList<String>();
    public static ArrayList<String> contenedorAnuncio = new ArrayList<String>();
    
 	
	public static void iniciarScraping() {
		
		//Llamamos al metodo realizar conexion para poder conectarnos a la BD.
		conexion=Conexiones.realizarConexion();
		
		/*
		 * Array URLS:
		 * 	
		 * 		Posicion 0: Alquiler
		 * 		Posicion 1: Venta
		 * 	
		 * */
		String[] urlsWeb = {
				"http://www.milanuncios.com/alquiler-de-viviendas-en-javea|xabia-alicante/",
				"http://www.milanuncios.com/venta-de-viviendas-en-javea|xabia-alicante/"};
		
		
		for(int i=0;i<urlsWeb.length;i++){
			
			obtenerInfo(urlsWeb[i]);
			if(i==0){
				
				insertarInmueble(2,"Jávea/Xàbia");
			}
			
			if(i==1){
				
				insertarInmueble(1,"Jávea/Xàbia");
			}
		}
			
		System.out.println("Scraping a la web Segundamano.es ha terminado");
		
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
	public static void obtenerInfo(String urlWeb){
		
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
			for(int x=0;x<4;x++){
								
				doc = Jsoup.connect(urlWeb+arrayPaginas.get(x))
						.userAgent("Mozilla/5.0")
						.post();
				
				Elements listaAnuncios = doc.getElementsByClass("x1");
				
				for(Element anuncio : listaAnuncios){
					
					//Reiniciamos los arraylist.
					listaFotos.clear();
					contenedorAnuncio.clear();
					
					
					urlDetalles=url+anuncio.getElementsByClass("cti").attr("href");	
					
					//Recorremos la url de los detalles de cada anuncio.
					doc = Jsoup.connect(urlDetalles)
							.userAgent("Mozilla/5.0")
							.post();
					
					//Obtener la referencia
					Elements numReferencia = doc.getElementsByClass("anuRefBox");
					
					for(Element detalle : numReferencia){
						
						referencia=detalle.getElementsByTag("b").text();
										
					}
					
					fecha=doc.getElementsByClass("anuFecha").text();
					titulo=doc.getElementsByClass("pagAnuTituloBox").text();
					categoria=doc.getElementsByClass("anuTitulo").text();
					descripcion=doc.getElementsByClass("pagAnuCuerpoAnu").text();
					
					//Obtener Precio
					txtPrecio = doc.getElementsByClass("pr").text();
					
					if(txtPrecio.equals("")){
						
						txtPrecio="0";
					}
					for(int i=0;i<txtPrecio.length();i++){
						
						if(Character.isDigit(txtPrecio.charAt(i))){
							
							precioReal+=txtPrecio.charAt(i);
						}
					}
					
					
					//Obtener Metros 2
					superficie=doc.getElementsByClass("m2").text();
					
					if(superficie.equals("")){
						
						superficie="0 m2";
					}
							
					//Obtener Habitaciones
					txtHabitaciones=doc.getElementsByClass("dor").text();
					
					if(txtHabitaciones.equals("")){
						
						txtHabitaciones="0";
					}
					for(int i=0;i<txtHabitaciones.length();i++){
						
						if(Character.isDigit(txtHabitaciones.charAt(i))){
							
							numHabitaciones+=txtHabitaciones.charAt(i);
						}
					}
				
					//Obtener veces listado
					Elements elementoVecesListado = doc.getElementsByClass("dato");
					vecesListado=elementoVecesListado.get(0).getElementsByTag("strong").text();
					
					//Obtener fotos
					Elements fotos = doc.getElementsByClass("pagAnuFotoBox");
					
					if(fotos.size()!=0){
						
						for(Element detalle : fotos){
						
							Elements urlImagenes = detalle.getElementsByClass("pagAnuFoto");
						
							for(Element detalleUrl : urlImagenes){
								
								listaFotos.add(detalleUrl.getElementsByTag("img").attr("src"));
							}						
						}
					
					//Llamar metodo fotos.
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
					
					
					//Llamar al metodo que cree el anuncio
					//System.exit(0);
				}	
			}
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de milanuncios.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
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
			
				PreparedStatement pstmt= conexion.prepareStatement("insert into inmuebles(in_nombre, in_url, in_fecha, in_categoria, in_operacion"
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
			
			System.out.println("Error al insertar el inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
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
			
			conexion.commit();
			
			//Obtener el ID del inmueble creado anteriormente.
			ResultSet idGenerado = pstmt.getGeneratedKeys();
			while(idGenerado.next()){
				
				codigoDetallesInmueble=idGenerado.getInt(1);
			}
			
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
		
		return -1;
	}
	
	/*
	 * Metodo que va descargando una a una las imagenes.
	 * */
	public static void descargaImagenes(String idInmueble, int codigoInmueble, int codigoDetallesInmueble){
		
		String rutaCarpeta;
		
		try{
			
			if(listaFotos.size()!=0){
				
				//Creamos la carpeta donde guardaremos las imagenes.
				rutaCarpeta=crearCarpetaImagenes(idInmueble, Integer.toString(codigoInmueble));
			
				for(int i=0;i<listaFotos.size()&&i<4;i++){
					
					descargaImagenesInmueble(listaFotos.get(i), rutaCarpeta, Integer.toString(i));
				}	
			}
		}catch (IOException e) {
			
			System.out.println("Ha habido un error al intentar descargar la imagen.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}	
		
		
	}
	
	public static String crearCarpetaImagenes(String idInmueble, String codInmueble){
		
		String ruta = "/Applications/XAMPP/xamppfiles/htdocs/openshift/imagenesAnuncios/Milanuncios/".concat(codInmueble+"-"+idInmueble);
		rutaCarpetaImagenes=ruta;
		
		File directorio = new File(ruta);
		directorio.mkdir();
		
		return ruta;
	}
	
	
	public static void descargaImagenesInmueble(String src, String rutaCarpeta, String numImagen)  throws IOException {
	
        URL url = new URL(src);
        InputStream in = url.openStream();

        OutputStream out = new BufferedOutputStream(new FileOutputStream(rutaCarpeta.concat("/"+numImagen+".jpg")));

       
        for (int b; (b = in.read()) != -1;) {
            out.write(b);
        }
        out.close();
        in.close();

    }
}

