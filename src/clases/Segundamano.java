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


public class Segundamano {

    public static Connection conexion;
    public static String rutaCarpetaImagenes;
 	
	public static void iniciarScraping() {
		
		//Llamamos al metodo realizar conexion para poder conectarnos a la BD.
		conexion=Conexiones.realizarConexion();
		
		
		/*
		 * Array URLS:
		 * 	
		 * 		Posicion 0: Venta - Casa adosada
		 * 		Posicion 1: Venta - Chalés
		 * 		Posicion 2: Venta - Pisos
		 * 		Posicion 3: Alquiler - Para vacaciones
		 * 		Posicion 4: Alquiler - Apartamento
		 * 	
		 * */
		String[] urlsWeb = {
				"http://www.segundamano.es/venta-de-casas-y-chales-alicante/javea.htm?ca=3_s&itype=8&fPos=394&fOn=sb_inmo_type",
				"http://www.segundamano.es/venta-de-casas-y-chales-alicante/javea.htm?ca=3_s&itype=9&fPos=516&fOn=sb_inmo_type",
				"http://www.segundamano.es/venta-de-pisos-alicante/javea.htm?ca=3_s&fPos=425&fOn=sb_st",
				"http://www.segundamano.es/alquiler-de-vacaciones-y-apartamentos-alicante/javea.htm?ca=3_s&itype=17&fPos=343&fOn=sb_inmo_type",
				"http://www.segundamano.es/alquiler-de-vacaciones-y-apartamentos-alicante/javea.htm?ca=3_s&itype=18&fPos=516&fOn=sb_inmo_type"
		};
		
		/*
		 * For que recorre el array de urls llamando al metodo
		 * obtener info, al cual le pasa:
		 * 
		 * 		1: Url web
		 * 		2: Categoria anuncio
		 * 		3: Operacion anuncio
		 * */
		
		for(int i=0;i<urlsWeb.length;i++){
			
			switch(i){
			
				case 0:
						obtenerInfo(urlsWeb[0], 1, 1, "Jávea/Xàbia" );
					break;
				case 1:
						obtenerInfo(urlsWeb[1], 2, 1, "Jávea/Xàbia" );
					break;
				case 2:
						obtenerInfo(urlsWeb[2], 3, 1, "Jávea/Xàbia" );
					break;
				case 3:
						obtenerInfo(urlsWeb[3], 4, 2, "Jávea/Xàbia" );
					break;
				case 4:
						obtenerInfo(urlsWeb[4], 5, 2, "Jávea/Xàbia" );
					break;
				default:
			}
		}
			
		System.out.println("Scraping a la web Segundamano.es ha terminado");
		
	}
	
	/*
	 * METODO: ObtenerInfo
	 * ACCIONES: Se encarga de conectar con la página web de segundamano y hacer el Scraping
	 * para obtener las caracteristicas que queramos.
	 * */
	public static void obtenerInfo(String url, int categoria, int operacion, String poblacion){
		
		
		String titulo="", fecha="", enlace="", idInmueble="";
		
		try {
			
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0")
					.post();
			
			//Analizamos estructura, parece que por cada anuncio muestra:
			//	<ul id="65252781" class="basicList list_ads_row  "  style="position:relative">
			
			Elements listaAnuncios = doc.getElementsByClass("basicList");
			
			for(Element anuncio : listaAnuncios){
				
				/*
				 * Obtendremos de cada anuncio
				 * 
				 * - Titulo
				 * - Fecha
				 * - Enlace
				 * 
				 * */
				
				titulo = anuncio.getElementsByClass("subjectTitle").text();			
				fecha = anuncio.getElementsByClass("date").text();				
				enlace = anuncio.getElementsByClass("subjectTitle").attr("href");
				idInmueble=obtenerIdInmueble(enlace);
				
				
				//Insertar info en la BBDD
				insertarInmueble(titulo, fecha, enlace, categoria, operacion, idInmueble, poblacion);
				
				
				
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
	public static void insertarInmueble(String titulo, String fecha, String enlace, int categoria, int operacion, String idInmueble, String poblacion){
		
		int codInmuebleAnterior=0, idCaracteristicasAnuncio;
		String descripInmueble="";
		ArrayList<String> detallesAnuncio = new ArrayList<String>();
		
		try{
						
				//INSERTAR INMUEBLE
			
				PreparedStatement pstmt= conexion.prepareStatement("insert into inmuebles(in_nombre, in_url, in_fecha, in_categoria, in_operacion, in_idInmueble, in_web) values(?,?,?,?,?,?,?)"
					   ,Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, titulo);
				pstmt.setString(2, enlace);
				pstmt.setString(3, fecha);
				pstmt.setInt(4, categoria);
				pstmt.setInt(5, operacion);
				pstmt.setString(6, idInmueble);
				pstmt.setString(7, "Segundamano");
				pstmt.executeUpdate();
				
				//Obtener el ID del inmueble creado anteriormente.
				ResultSet idGenerado = pstmt.getGeneratedKeys();
				while(idGenerado.next()){
					
					codInmuebleAnterior=idGenerado.getInt(1);
				}
				idGenerado.close();
				// FIN INSERTAR INMUEBLE ----------------------------------------
				
				
				// INSERTAR CARACTERISTICAS ANUNCIO.
				idCaracteristicasAnuncio=insertarCaracteristicasInmueble(enlace);
				
	
				// OBTENER CARACTERISTICAS DEL ANUNCIO Y INSERTAR LOS DETALLES.
				detallesAnuncio=obtenerInfoDetallesInmueble(enlace,idInmueble,Integer.toString(codInmuebleAnterior));
				
				insertarDetallesInmueble(detallesAnuncio, codInmuebleAnterior,idCaracteristicasAnuncio, poblacion);
				
					
			
				pstmt.close();
			
		}catch(SQLException ex){
			
			System.out.println("Error al insertar el inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
		}
	}
	
	
	/*
	 * METODO PARA INSERTAR LAS CARACTERISTICAS DEL INMUEBLE
	 * */
	public static int insertarCaracteristicasInmueble(String urlDetalles){
		
		boolean existenCaracteristicas=false;
		String caracteristicas="";
		int idCaracteristicasAnuncio=0;
				
		try{
			
			Document doc = Jsoup.connect(urlDetalles)
					.userAgent("Mozilla/5.0")
					.post();
			
			//-----------CARACTERISTICAS ANUNCIO-------------------------------------------
			Elements elemento = doc.getElementsByClass("extra_features_detail_sel");
			
			for(int i=0;i<elemento.size();i++){
				
				existenCaracteristicas=true;
				if(i==elemento.size()-1){
					caracteristicas += elemento.get(i).text()+".";
				}else{
					caracteristicas += elemento.get(i).text()+", ";
				}
				
			}
			
			if(!existenCaracteristicas){
				
				caracteristicas="No hay caracteristicas";
			}
			//------------------------------------------------------------------------------
			
			//Statement.RETURN_GENERATED_KEYS --> Nos permite obtener el ultimo id generado.
			PreparedStatement pstmt= conexion.prepareStatement("insert into caracteristicasInmueble(car_detalles) values(?)",Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, caracteristicas);
			pstmt.executeUpdate();
			
			ResultSet idCaracAnuncio = pstmt.getGeneratedKeys();
			while(idCaracAnuncio.next()){
				
				idCaracteristicasAnuncio=idCaracAnuncio.getInt(1);
			}
			
			idCaracAnuncio.close();
				
			pstmt.close();
		}catch(SQLException ex){
			
			System.out.println("Error al insertar las caracteristicas del inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}	
		
		return idCaracteristicasAnuncio;
		
	}
	
	
	/*
	 * METODO PARA INSERTAR LOS DETALLES DEL INMUEBLE
	 * */
	public static void insertarDetallesInmueble(ArrayList<String> detallesAnuncio, int idInmueble, int idCaracInmueble, String poblacion){
		
		String descripInmueble;
		
		try{
			
			PreparedStatement pstmt= conexion.prepareStatement("insert into detallesInmueble(det_codInmueble, det_descripcion, det_vendedor, det_numImagenes, det_habitaciones,"
					+"det_superficie,det_terreno, det_poblacion, det_vecesVisitado, det_precio, det_caracteristicas) values(?,?,?,?,?,?,?,?,?,?,?)");
			
			
			pstmt.setInt(1, idInmueble);
			pstmt.setString(2, detallesAnuncio.get(0));
			pstmt.setString(3, detallesAnuncio.get(1));
			pstmt.setInt(4, Integer.parseInt(detallesAnuncio.get(2)));
			
			if(detallesAnuncio.get(3).equals("")){
				
				pstmt.setInt(5, 0);
			}else{
				
				pstmt.setInt(5, Integer.parseInt(detallesAnuncio.get(3)));
			}
			
			pstmt.setString(6, detallesAnuncio.get(4));
			pstmt.setString(7, detallesAnuncio.get(5));
			pstmt.setString(8, poblacion);
			pstmt.setInt(9, Integer.parseInt(detallesAnuncio.get(6)));
			pstmt.setInt(10, Integer.parseInt(detallesAnuncio.get(7)));
			pstmt.setInt(11, idCaracInmueble);
			pstmt.executeUpdate();
				
				
			conexion.commit();
			pstmt.close();
			
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
				
				//Borramos la carpeta con las fotos.
				File directorio = new File(rutaCarpetaImagenes);
				borrarCarpetaImagenAnuncio(directorio);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
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
	 * 				Posicion 2: Numero Imagenes
	 * 				Posicion 3: Nº Habitaciones
	 * 				Posicion 4: Superficie
	 * 				Posicion 5: Terreno
	 * 				Posicion 6: Numero de veces visitado.
	 * 				Posicion 7: Precio.
	 * */
	public static ArrayList<String> obtenerInfoDetallesInmueble(String urlDetalles,String idInmueble, String codInmueble){
		
		
		String descripcion="", vendedor="", habitaciones="", superficie="", terreno="", cadena="", precio="", precioFinal="";
		ArrayList<String> urlImagenes = new ArrayList<String>();
		ArrayList<String> caracteristicasAnuncio = new ArrayList<String>();
		int numImagenesDescargadas=0;
		
		int i;
		boolean existenImagenes=true;
		
		
		try {
			
			Document doc = Jsoup.connect(urlDetalles)
					.userAgent("Mozilla/5.0")
					.post();
			
					
			Elements listaAnuncios = doc.getElementsByClass("view");
						
			for(Element anuncio : listaAnuncios){
				
				i=0;
				/*
				 * Obtendremos de cada anuncio sus detalles
				 * 
				 * - Descripcion
				 * - Vendedor
				 * - Url Imagenes
				 * 
				 * */
				
				descripcion = anuncio.getElementById("descriptionText").text();			
				vendedor = anuncio.getElementsByClass("Cname").text();	
				
				try{
					
					while(existenImagenes){
						
						urlImagenes.add(anuncio.getElementById("thumb"+i).attr("onclick"));
						i++;
					}
					
				}catch(NullPointerException ex){
					
					existenImagenes=false;
				}
				
				
				//Guardamos las imagenes correspondientes al anucio.
				numImagenesDescargadas=obtenerUrlImagenes(urlDetalles,urlImagenes, idInmueble, codInmueble);
				
				//Añadimos todas las caracteristicas al array.
				caracteristicasAnuncio.add(descripcion);
				caracteristicasAnuncio.add(vendedor);
				caracteristicasAnuncio.add(Integer.toString(numImagenesDescargadas));
				
			}
			
			Elements listaDetalles = doc.getElementsByClass("descriptionFeatures");
			
			for(Element detalle : listaDetalles){
				
				Elements listaTitulos = detalle.getElementsByTag("dt");
				Elements listasDetalles = detalle.getElementsByTag("dd");
				
				
				for(int x=0;x<listaTitulos.size();x++){
					
					cadena=listaTitulos.get(x).text();
					
					switch(cadena){
					
						case "nº hab":
									habitaciones=listasDetalles.get(x*2).text();
							break;
						
						case "superficie":
									superficie=listasDetalles.get(x*2).text();
							break;
							
						case "terreno":
									terreno=listasDetalles.get(x*2).text();
							break;
						
						default:
					}
				}
				
				caracteristicasAnuncio.add(habitaciones);
				caracteristicasAnuncio.add(superficie);
				caracteristicasAnuncio.add(terreno);
				
			}
			
			//Cuantas veces se ha visitado el anuncio
			String visitado= doc.getElementsByClass("TimesSeen").text();
			String numVeces="";
			
			for(int x=0;x<visitado.length();x++){
				
				if(Character.isDigit(visitado.charAt(x))){
					
					numVeces += visitado.charAt(x);
					
				}
			}
			
			caracteristicasAnuncio.add(numVeces);
			//-----------------------------------------------
			
			//OBTENER PRECIO
			precio=doc.getElementsByClass("price").text();
			
			if(precio.equals("")){
				
				precioFinal="0";
				
			}else{
				
				for(int x=0;x<precio.length();x++){
					
					if(Character.isDigit(precio.charAt(x))){
						
						precioFinal += precio.charAt(x);
						
					}
				}
				
			}
			
			caracteristicasAnuncio.add(precioFinal);
			
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de Detalles");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}	
			
		
		return caracteristicasAnuncio;
	}
	
	/*
	 * METODO QUE OBTIENE LA URL DE LAS IMAGENES.
	 * */
	public static int obtenerUrlImagenes(String urlDetalles, ArrayList<String> urlImagenes, String idImueble, String codInmueble){
		
		ArrayList<String> listaImagenes = new ArrayList<String>();
		ArrayList<String> partes = new ArrayList<String>();
		boolean imagenDescargada=false;
		int numImagenesDescargadas=0;
		
		for(int i=0;i<urlImagenes.size();i++){
			
			StringTokenizer st = new StringTokenizer(urlImagenes.get(i), "'");

			while(st.hasMoreTokens()) {

				partes.add(st.nextToken());

			}
			
			listaImagenes.add(partes.get(1));
			
			//Borramos el contenido
			partes.clear();
		}
			
		for(int i=0;i<listaImagenes.size()&&numImagenesDescargadas<5;i++){
        	
        	imagenDescargada=descargaImagenes(urlDetalles,listaImagenes.get(i),idImueble, codInmueble, Integer.toString(i));
        	
        	if(imagenDescargada){
        		
        		numImagenesDescargadas++;
        	}
        }
		
        return numImagenesDescargadas;
	}
	
	public static String obtenerIdInmueble(String enlace){
		
		ArrayList<String> partes = new ArrayList<String>();
				
		StringTokenizer st = new StringTokenizer(enlace, "/");

        while(st.hasMoreTokens()) {

        partes.add(st.nextToken());

        }
        
        return partes.get(4);
	}
	
	
	
	public static boolean descargaImagenes(String urlDetalles, String enlaceFoto, String idInmueble, String codInmueble, String numImagen){
		
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
			
			return true;
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al intentar descargar las Imagenes, probaremos con la siguiente...");
			System.out.println("Mensaje: "+e.getMessage() + "\nTraza: "+e.getStackTrace());
			
			
			return false;
		}catch(NullPointerException e){
			
			
		}
		return false;
		
	}
	
	public static String crearCarpetaImagenes(String idInmueble, String codInmueble){
		
		String ruta = "/Applications/XAMPP/xamppfiles/htdocs/openshift/imagenesAnuncios/Segundamano/".concat(codInmueble+"-"+idInmueble);
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
	
	public static void borrarCarpetaImagenAnuncio(File carpeta){
		
		File directorio = new File(rutaCarpetaImagenes);
		
		File[] ficheros = directorio.listFiles();
		
		for(int i=0;i<ficheros.length;i++){
			
			if(ficheros[i].isDirectory()){
				
				borrarCarpetaImagenAnuncio(ficheros[i]);

			}
				
			ficheros[i].delete();	
			
		}
	}
	
	
	/*
	 *  CODIGO QUE SE ENCARGA DE REDIMENSIONAR LAS IMAGENES
	 * 
	//Este método se utiliza para cargar la imagen de disco
    public static BufferedImage loadImage(String pathName) {
        BufferedImage bimage = null;
        try {
            bimage = ImageIO.read(new File(pathName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bimage;
    }
    
    
    Este método se utiliza para almacenar la imagen en disco
    
    public static void saveImage(BufferedImage bufferedImage, String pathName) {
        
    	try {
        	//String format = (pathName.endsWith(".png")) ? "png" : "jpg";
            
    		String format = (pathName.endsWith(".png")) ? "png" : "jpg";
            File file =new File(pathName);
            file.getParentFile().mkdirs();
            ImageIO.write(bufferedImage, format, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
	Este método se utiliza para redimensionar la imagen
    public static BufferedImage resize(BufferedImage bufferedImage) {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        BufferedImage bufim = new BufferedImage(600, 400, bufferedImage.getType());
        Graphics2D g = bufim.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bufferedImage, 0, 0, 600, 400, 0, 0, w, h, null);
        g.dispose();
        return bufim;
    }
	*/
}


