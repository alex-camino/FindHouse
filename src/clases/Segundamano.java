package clases;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.jdbc.Statement;

import clases.Conexiones;


public class Segundamano {

    public static Connection conexion;
 	
	public static void iniciarScraping() {
		
		//Llamamos al metodo realizar conexion para poder conectarnos a la BD.
		conexion=Conexiones.realizarConexion();
		obtenerInfo();
		System.out.println("Scraping a la web Segundamano.es ha terminado");
		
	}
	
	/*
	 * METODO: ObtenerInfo
	 * ACCIONES: Se encarga de conectar con la página web de segundamano y hacer el Scraping
	 * para obtener las caracteristicas que queramos.
	 * */
	public static void obtenerInfo(){
		
		
		String titulo, fecha, precio, enlace, categoria, imagenPrincipal, idInmueble;
		
		try {
			
			Document doc = Jsoup.connect("http://www.segundamano.es/pisos-y-casas-alicante/javea.htm?ca=3_s&fPos=168&fOn=sb_searchtext")
					.userAgent("Mozilla/5.0")
					.post();
			
			//Analizamos estructura, parece que por cada anuncio muestra:
			//	<ul id="65252781" class="basicList list_ads_row  "  style="position:relative">
			
			Elements listaAnuncios = doc.getElementsByClass("basicList");
			boolean isPrimerAnuncio = true;
			
			for(Element anuncio : listaAnuncios){
				
				/*
				 * Obtendremos de cada anuncio
				 * 
				 * - Titulo
				 * - Fecha
				 * - Precio
				 * - Enlace
				 * - Categoria
				 * - Enlace primera Imagen
				 * 
				 * */
				
				titulo = anuncio.getElementsByClass("subjectTitle").text();			
				fecha = anuncio.getElementsByClass("date").text();				
				precio = anuncio.getElementsByClass("subjectPrice").text();
				enlace = anuncio.getElementsByClass("subjectTitle").attr("href");
				categoria = anuncio.getElementsByClass("infoBottom").text();
				idInmueble=obtenerIdInmueble(enlace);
				

				//Insertar info en la BBDD
				addInfoBD(titulo, fecha, precio.substring(0, precio.indexOf('\u20AC')), enlace, categoria, idInmueble);
				
				
				
			}
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}		
	}
	
	
	/*
	 * METODO: addInfoBD
	 * ACCIONES: Recibe las caracteristicas del anuncio y crea un inmueble a partir de todas ellas.
	 * 
	 * */
	public static void addInfoBD(String titulo, String fecha, String precio, String enlace, String categoria, String idInmueble){
		
		int cod_categoria=0, codInmuebleAnterior=0;
		boolean encontrado=false;
		String descripInmueble;
		ArrayList<String> caracteristicasAnuncio = new ArrayList<String>();
		
		try{
			
			PreparedStatement pstmt= conexion.prepareStatement("select cat_codigo from categoria where cat_nombre like '"+categoria+"%'");
			ResultSet rsCategorias =pstmt.executeQuery();
			
	
			while(rsCategorias.next()){
			
				cod_categoria=rsCategorias.getInt("cat_codigo");
				encontrado=true;
				
			}
		
			rsCategorias.close();
			
			if(encontrado){
				
								
				pstmt= conexion.prepareStatement("insert into inmuebles(in_nombre, in_url, in_fecha, in_categoria, in_idInmueble) values(?,?,?,?,?)"
					   ,Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, titulo);
				pstmt.setString(2, enlace);
				pstmt.setString(3, fecha);
				pstmt.setInt(4, cod_categoria);
				pstmt.setString(5, idInmueble);
				pstmt.executeUpdate();
				
				//Obtener el ID del inmueble creado anteriormente.
				ResultSet idGenerado = pstmt.getGeneratedKeys();
				while(idGenerado.next()){
					
					codInmuebleAnterior=idGenerado.getInt(1);
				}
				idGenerado.close();
				//-----------------------------------------------
				
				// OBTENER CARACTERISTICAS DEL ANUNCIO.
				
				caracteristicasAnuncio=obtenerInfoDetallesInmueble(enlace,idInmueble,Integer.toString(codInmuebleAnterior));
				
				//Añadir detalles del inmueble
				
				pstmt= conexion.prepareStatement("insert into detallesInmueble(det_descripcion, det_precio, det_codInmueble, det_vendedor) values(?,?,?,?)");
				
				if(caracteristicasAnuncio.get(0).length()>900){
					
					descripInmueble = caracteristicasAnuncio.get(0).substring(0, 900);
					
					pstmt.setString(1, descripInmueble);
					pstmt.setString(2, precio);
					pstmt.setInt(3, codInmuebleAnterior);
					pstmt.setString(4, caracteristicasAnuncio.get(1));
					pstmt.executeUpdate();
					
				}else{
					
					pstmt.setString(1, caracteristicasAnuncio.get(0));
					pstmt.setString(2, precio);
					pstmt.setInt(3, codInmuebleAnterior);
					pstmt.setString(4, caracteristicasAnuncio.get(1));
					pstmt.executeUpdate();
					
				}	
			}
			
		
			pstmt.close();
			
		}catch(SQLException ex){
			
			System.out.println("Error al insertar el inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
		}
	}
	
	
	/*
	 * METODO: ObtenerInfoDetallesInmueble
	 * Acciones: Se encarga de conectar con la url de los detalles del anuncio y de obtener la información que queramos y
	 * de llamar al metodo de las imagenes para pasarle el arraylist con las URL de las imagenes de cada inmueble
	 * 
	 * DEVUELVE: ArrayList con:
	 * 				Posicion 0: Descripcion.
	 * 				Posicion 1: Vendedor.
	 * */
	public static ArrayList<String> obtenerInfoDetallesInmueble(String urlDetalles,String idInmueble, String codInmueble){
		
		
		String descripcion, vendedor, caracteristicas, descripCaract, telefono, imagenes;
		ArrayList<String> urlImagenes = new ArrayList<String>();
		ArrayList<String> caracteristicasAnuncio = new ArrayList<String>();
		
		boolean existenImagenes=true;
		
		
		try {
			
			Document doc = Jsoup.connect(urlDetalles)
					.userAgent("Mozilla/5.0")
					.post();
			
					
			Elements listaAnuncios = doc.getElementsByClass("view");
						
			for(Element anuncio : listaAnuncios){
				
				/*
				 * Obtendremos de cada anuncio sus detalles
				 * 
				 * - Descripcion
				 * - Vendedor
				 * - Caracteristicas
				 * - Descripcion Caracteristicas
				 * - Telefono
				 * - Url Imagenes
				 * 
				 * */
				
				descripcion = anuncio.getElementById("descriptionText").text();			
				vendedor = anuncio.getElementsByClass("Cname").text();	
				//caracteristicas = anuncio.getElementsByClass("extra_features_detail_sel").text();
				//descripCaract = anuncio.getElementsByClass("descriptionRightContainer").text();
				//telefono = anuncio.getElementsByClass("tel").text();
				
				try{
					
					for(int i=0;existenImagenes;i++){
						
						urlImagenes.add(anuncio.getElementById("thumb"+i).attr("onclick"));
					}
					
				}catch(NullPointerException ex){
					
					existenImagenes=false;
				}
				
				
				//Guardamos las imagenes correspondientes al anucio.
				obtenerUrlImagenes(urlDetalles,urlImagenes, idInmueble, codInmueble);
				
				//Añadimos todas las caracteristicas al array.
				caracteristicasAnuncio.add(descripcion);
				caracteristicasAnuncio.add(vendedor);
				
			}
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de Detalles");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}	
			
		
		return caracteristicasAnuncio;
	}
	
	public static void obtenerUrlImagenes(String urlDetalles, ArrayList<String> urlImagenes, String idImueble, String codInmueble){
		
		ArrayList<String> listaImagenes = new ArrayList<String>();
		ArrayList<String> partes = new ArrayList<String>();
		
		for(int i=0;i<urlImagenes.size();i++){
			
			StringTokenizer st = new StringTokenizer(urlImagenes.get(i), "'");

			while(st.hasMoreTokens()) {

				partes.add(st.nextToken());

			}
			
			listaImagenes.add(partes.get(1));
			
			//Borramos el contenido
			partes.clear();
		}
			
		//Descargamos 5 imagenes por inmueble
		if(listaImagenes.size()<5){
			
			for(int i=0;i<listaImagenes.size();i++){
	        	
	        	descargaImagenes(urlDetalles,listaImagenes.get(i),idImueble, codInmueble, Integer.toString(i));
	        }
			
		}else{
			
			for(int i=0;i<5;i++){
				descargaImagenes(urlDetalles,listaImagenes.get(i),idImueble, codInmueble, Integer.toString(i));
	        }
		}
        
        
		
	}
	
	public static String obtenerIdInmueble(String enlace){
		
		ArrayList<String> partes = new ArrayList<String>();
				
		StringTokenizer st = new StringTokenizer(enlace, "/");

        while(st.hasMoreTokens()) {

        partes.add(st.nextToken());

        }
        
        return partes.get(4);
	}
	
	
	
	public static void descargaImagenes(String urlDetalles, String enlaceFoto, String idInmueble, String codInmueble, String numImagen){
		
		String rutaCarpeta;
		
		try {
			Document doc = Jsoup.connect(urlDetalles)
					.userAgent("Mozilla/5.0")
					.post();
			
			String existenImagenes="";
			
			existenImagenes=doc.getElementsByClass("centerMachine").attr("alt");
			
			if(!existenImagenes.equalsIgnoreCase("este anuncio no tiene foto")){
				
				//Creamos la carpeta donde guardaremos las imagenes.
				rutaCarpeta=crearCarpetaImagenes(idInmueble, codInmueble);
			
				descargaImagenesInmueble(enlaceFoto, rutaCarpeta, numImagen);
				
			}
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al intentar descargar las Imagenes.......");
			System.out.println("Mensaje: "+e.getMessage() + "\nTraza: "+e.getStackTrace());
			
		}catch(NullPointerException e){
			
			
		}
		
	}
	
	public static String crearCarpetaImagenes(String idInmueble, String codInmueble){
		
		String ruta = "/Applications/XAMPP/xamppfiles/htdocs/FindHouse/imagenesAnuncios/".concat(codInmueble+"-"+idInmueble);
		
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


