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


public class Segundamano extends Thread{

    public static Connection conexion;
    public static String rutaCarpetaImagenes;
    
    //    VARIABLES GLOBALES NECESARIAS POR ANUNCIO
    public static ArrayList<String> urlImagenes = new ArrayList<String>();
    public static ArrayList<String> caracteristicasAnuncio = new ArrayList<String>();
    public static boolean descargarImagenes=false;
 	public static int operacion, categoria;
 	public static String poblacion="";
 	
 	public void run()
 	{
 		Main.inicioSegundamano = System.currentTimeMillis();
 		iniciarScraping();
 		System.out.println("El Scraping a la web Segundamano.es ha terminado");
 		Main.finalSegundamano = System.currentTimeMillis();
 	}
 	
	public static void iniciarScraping() {
		
		//Llamamos al metodo realizar conexion para poder conectarnos a la BD.
		conexion=Conexiones.realizarConexion();
		String[] localidades = {"Javea", "Denia", "Calpe", "Benidorm"};
		
		
		for(int i=0;i<localidades.length;i++){
			
			//Venta
			operacion=1;
			poblacion=localidades[i];
			obtenerInfo("http://www.segundamano.es/venta-de-pisos-y-casas-alicante/"+localidades[i]+".htm?ca=3_s&fPos=601&fOn=sb_st");
			
			//Alquiler
			operacion=2;
			poblacion=localidades[i];
			obtenerInfo("http://www.segundamano.es/alquiler-de-pisos-y-casas-alicante/"+localidades[i]+".htm?ca=3_s&fPos=576&fOn=sb_st");
			
		}
		
	}
	
	/*
	 * METODO: ObtenerInfo
	 * ACCIONES: Se encarga de conectar con la página web de segundamano y hacer el Scraping
	 * para obtener las caracteristicas que queramos.
	 * */
	public static void obtenerInfo(String url){
		
		
		ArrayList<String> listaPaginas = new ArrayList<String>();
		ArrayList<String> listaURLAnuncios = new ArrayList<String>();
		ArrayList<String> listaFechasAnuncios = new ArrayList<String>();
		
		try {
			
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0")
					.post();
			
			//Añadimos una posicion vacia en la lista de paginas, que reemplaza a la primera pagina
			listaPaginas.add("");
			
			
			Elements listaPaginasDisponibles = doc.getElementsByClass("paginationLink");
			
			for(Element detalle : listaPaginasDisponibles){
				
				listaPaginas.add(detalle.getElementsByTag("a").attr("href"));
							
			}
			
			//Se pone '-2' porque corresponden a las urls de boton siguiente y boton ultima pagina.
			for(int i=0;i<(listaPaginas.size()-2);i++){
				
				//Reiniciar arrayList
				listaFechasAnuncios.clear();
				listaURLAnuncios.clear();
				
				if(i==0){
					
					doc = Jsoup.connect(url)
						.userAgent("Mozilla/5.0")
						.post();
				}else{
					
					doc = Jsoup.connect(listaPaginas.get(i))
						.userAgent("Mozilla/5.0")
						.post();
				}
				
				//Recorremos todo el listado de anuncios.
				Elements listaAnuncios = doc.getElementsByClass("basicList");
				
				for(Element anuncio : listaAnuncios){
					
					
					listaFechasAnuncios.add(anuncio.getElementsByClass("date").text());	
					listaURLAnuncios.add(anuncio.getElementsByClass("subjectTitle").attr("href"));
							
				}
				
				for(int x=0;x<listaURLAnuncios.size();x++){
					
					//Reiniciar ArrayList.
					caracteristicasAnuncio.clear();
					urlImagenes.clear();
					
					obtenerInfoDetallesInmueble(listaURLAnuncios.get(x), listaFechasAnuncios.get(x));
					
				}
			}
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}		
	}
	
	
	/*
	 * METODO: ObtenerInfoDetallesInmueble
	 * Acciones: Se encarga de conectar con la url de los detalles del anuncio y de obtener la información que queramos y
	 * de llamar al metodo de las imagenes para pasarle el arraylist con las URL de las imagenes de cada inmueble
	 * 
	 * */
	public static void obtenerInfoDetallesInmueble(String url, String fechaAnuncio){
				
		String  cadena="", precio="", precioFinal="", cuadroImagenes="", titulo=""
				,descripcion="", vendedor="", habitaciones="", superficie="", terreno="", categoria="", tipo="", fecha="", urlAnuncio="";
		int i=0;
		boolean existenImagenes=true, anuncioCorrecto=false;
		
		fecha=fechaAnuncio;
		
		try {
			
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0")
					.post();
			
					
			
			Elements listaAnuncios = doc.getElementsByClass("view");
						
			for(Element anuncio : listaAnuncios){
								
				
				//Controlaar si hay imagenes en el anuncio.
                cuadroImagenes=doc.getElementsByClass("centerMachine").attr("alt");
				if(!cuadroImagenes.equalsIgnoreCase("este anuncio no tiene foto")){
					
					anuncioCorrecto=true;
					
					// CARACTERISTICAS ANUNCIO - POS-0 - Titulo Anuncio 
					titulo=anuncio.getElementsByClass("productTitle").text();
					if(titulo.equals(""))
						caracteristicasAnuncio.add("No hay titulo");
					else
						caracteristicasAnuncio.add(titulo);
					
					
					// CARACTERISTICAS ANUNCIO - POS-1 - Descripcion Anuncio 
					descripcion=anuncio.getElementById("descriptionText").text();
					if(descripcion.equals(""))
						caracteristicasAnuncio.add("No hay descripcion del anuncio.");
					else
						caracteristicasAnuncio.add(descripcion);
					
					
					// CARACTERISTICAS ANUNCIO - POS-2 - Vendedor
					vendedor=anuncio.getElementsByClass("Cname").text();
					if(vendedor.equals(""))
						caracteristicasAnuncio.add("Sin nombre");
					else
						caracteristicasAnuncio.add(vendedor);
					
					
                	try{
						
						while(existenImagenes){
							
							descargarImagenes=true;
							
							urlImagenes.add(anuncio.getElementById("thumb"+i).attr("onclick"));
							i++;
						}
						
					}catch(NullPointerException ex){
						
						existenImagenes=false;
					}
	    			
				}
				
			}
			
			//Si el anuncio tiene fotos entramos.
			if(anuncioCorrecto){
				
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
							
							case "categoría":
											categoria=listasDetalles.get(x*2).text();
								break;
							
							case "tipo":
											tipo=listasDetalles.get(x*2).text();
								break;
								
							default:
						}
					}
				}
				
				 // CARACTERISTICAS ANUNCIO - POS-3 - Habitaciones 
				if(habitaciones.equals(""))
					caracteristicasAnuncio.add("0");
				else
					caracteristicasAnuncio.add(habitaciones);
				
				// CARACTERISTICAS ANUNCIO - POS-4 - Superficie
				if(superficie.equals(""))
					caracteristicasAnuncio.add("0 m2");
				else
					caracteristicasAnuncio.add(superficie);
				
				// CARACTERISTICAS ANUNCIO - POS-5 - Terreno
				if(terreno.equals(""))
					caracteristicasAnuncio.add("0 m2");
				else
					caracteristicasAnuncio.add(terreno);
				
				 // CARACTERISTICAS ANUNCIO - POS-6 - Categoria 
				if(categoria.equals(""))
					caracteristicasAnuncio.add("No hay categoria");
				else
					caracteristicasAnuncio.add(categoria);
				
				// CARACTERISTICAS ANUNCIO - POS-7 - Tipo 
				if(tipo.equals(""))
					caracteristicasAnuncio.add("No hay tipo");
				else
					caracteristicasAnuncio.add(tipo);
				
				
				//Cuantas veces se ha visitado el anuncio
				String visitado= doc.getElementsByClass("TimesSeen").text();
				String numVeces="";
				
				if(visitado.length()>0){
					
					for(int x=0;x<visitado.length();x++){
						
						if(Character.isDigit(visitado.charAt(x))){
							
							numVeces += visitado.charAt(x);
							
						}
					}
					
					// CARACTERISTICAS ANUNCIO - POS-8 - Numero de veces Visitado 
					caracteristicasAnuncio.add(numVeces);
				}else{
					
					caracteristicasAnuncio.add("0");
				}
				
				
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
				
				// CARACTERISTICAS ANUNCIO - POS-9 - Precio Inmueble
				caracteristicasAnuncio.add(precioFinal);
				
				// CARACTERISTICAS ANUNCIO - POS-10 - ID Inmueble
				caracteristicasAnuncio.add(obtenerIdInmueble(url));
				
				// CARACTERISTICAS ANUNCIO - POS-11 - Fecha Anuncio
				caracteristicasAnuncio.add(fecha);
				
				// CARACTERISTICAS ANUNCIO - POS-12 - URL Inmueble
				caracteristicasAnuncio.add(url);
				
				
				
				//Añadir info a la Base de datos
				addInfoBD();
			}
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de Detalles del Inmueble");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}	
			
	}
	
	//METODO QUE INSERTA LA INFORMACION EN LA BD.
	public static void addInfoBD(){
		
		try{
			
			//INSERTAR INMUEBLE
		
			PreparedStatement pstmt= conexion.prepareStatement("insert into inmuebles(in_titulo, in_fecha, in_url, in_operacion, in_idInmueble, in_web) values(?,?,?,?,?,?)"
				   ,Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, caracteristicasAnuncio.get(0));
			pstmt.setString(2, caracteristicasAnuncio.get(11));
			pstmt.setString(3, caracteristicasAnuncio.get(12));
			pstmt.setInt(4, operacion);
			pstmt.setString(5, caracteristicasAnuncio.get(10));
			pstmt.setString(6, "Segundamano");
			pstmt.executeUpdate();
			
			//Obtener el ID del inmueble creado anteriormente.
			ResultSet idGenerado = pstmt.getGeneratedKeys();
			while(idGenerado.next()){
				
				// CARACTERISTICAS ANUNCIO - POS-13 CodigoInmueble
				caracteristicasAnuncio.add(Integer.toString(idGenerado.getInt(1)));
			}
			idGenerado.close();
			// FIN INSERTAR INMUEBLE ----------------------------------------
			
			// INSERTAR CARACTERISTICAS ANUNCIO.
			// CARACTERISTICAS ANUNCIO - POS-14 - idCaracteristicasInmueble
			caracteristicasAnuncio.add(Integer.toString(insertarCaracteristicasInmueble()));
			

			//Insertar Detalles Inmueble
			insertarDetallesInmueble();
			
			
			pstmt.close();
			
		}catch(SQLException ex){
			
			System.out.println("Error al insertar el inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
		}
		
	}
		
	
	/*
	 * METODO PARA INSERTAR LAS CARACTERISTICAS DEL INMUEBLE
	 * */
	public static int insertarCaracteristicasInmueble(){
		
		boolean existenCaracteristicas=false;
		String caracteristicas="";
		int idCaracteristicasAnuncio=0;
				
		try{
			
			//Le paso la URL Detalles
			Document doc = Jsoup.connect(caracteristicasAnuncio.get(12))
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
	public static void insertarDetallesInmueble(){
		
		String descripInmueble;
		
		try{
			
			PreparedStatement pstmt= conexion.prepareStatement("insert into detallesInmueble(det_codInmueble, det_descripcion, det_vendedor, det_habitaciones,"
					+"det_superficie,det_terreno, det_poblacion, det_vecesVisitado, det_precio, det_caracteristicas) values(?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
			
			
			pstmt.setInt(1, Integer.parseInt(caracteristicasAnuncio.get(13)));
			pstmt.setString(2, caracteristicasAnuncio.get(1));
			pstmt.setString(3, caracteristicasAnuncio.get(2));
			
			if(caracteristicasAnuncio.get(3).equals("")){
				
				pstmt.setInt(4, 0);
			}else{
				
				pstmt.setInt(4, Integer.parseInt(caracteristicasAnuncio.get(3)));
			}
			
			pstmt.setString(5, caracteristicasAnuncio.get(4));
			pstmt.setString(6, caracteristicasAnuncio.get(5));
			pstmt.setString(7, poblacion);
			pstmt.setInt(8, Integer.parseInt(caracteristicasAnuncio.get(8)));
			pstmt.setInt(9, Integer.parseInt(caracteristicasAnuncio.get(9)));
			pstmt.setInt(10, Integer.parseInt(caracteristicasAnuncio.get(14)) );
			pstmt.executeUpdate();
				

			ResultSet idDetallesInmueble = pstmt.getGeneratedKeys();
			while(idDetallesInmueble.next()){
				
				// CARACTERISTICAS ANUNCIO - POS-15 - ID Detalles Inmueble
				caracteristicasAnuncio.add(Integer.toString(idDetallesInmueble.getInt(1)));
			}
			
			idDetallesInmueble.close();
				
			conexion.commit();
			pstmt.close();
			
			
			//Modificar el inmueble para añadirles su categoria
			detectarCategoria();
			
			//Si hay imagenes, descargarlas.
			if(descargarImagenes){
				
				obtenerUrlDescargarImagenes();
			}
			
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
	
	
	/*
	 * Metodo que detecta que categoria es el anuncio y la añade.
	 * Tambien añade el numero de imagenes descargadas.
	 * */
	
	public static void detectarCategoria(){
		
		String categoria=caracteristicasAnuncio.get(7);
		
		int numCategoria=0;
		
		
		if(categoria.contains("casa"))
			numCategoria = 1;
		else if(categoria.contains("chal"))
			numCategoria = 2;
		else if(categoria.contains("piso"))
			numCategoria = 3;
		else if(categoria.contains("vacacion"))
			numCategoria = 4;
		else if(categoria.contains("apartamento"))
			numCategoria = 5;
		else if(categoria.contains("estudio"))
			numCategoria = 6;
		else if(categoria.contains("ático"))
			numCategoria = 7;
		else if(categoria.contains("dúplex"))
			numCategoria = 8;
		else if(categoria.contains("loft"))
			numCategoria = 9;
		else if(categoria.contains("planta"))
			numCategoria = 10;
		else if(categoria.contains("local"))
			numCategoria = 11;
		else
			numCategoria = 12;
		
		try{
			
			PreparedStatement pstmt= conexion.prepareStatement("UPDATE inmuebles SET in_categoria="+numCategoria+" WHERE in_codigo="
									+Integer.parseInt(caracteristicasAnuncio.get(13)));
			pstmt.executeUpdate();
		}catch(SQLException ex){
			
			System.out.println("Error al insertar la categoria del inmueble en la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
		}		
		
		
	}
	
	
	
	/*
	 * METODO QUE OBTIENE LA URL DE LAS IMAGENES.
	 * */
	public static void obtenerUrlDescargarImagenes(){
		
		ArrayList<String> listaImagenes = new ArrayList<String>();
		ArrayList<String> partes = new ArrayList<String>();
		boolean imagenDescargada=false;
		int numImagenesDescargadas=0;
		String rutaCarpeta;
		
		
		//De cada URL, nos quedamos solo con la direccion Web.
		for(int i=0;i<urlImagenes.size();i++){
			
			StringTokenizer st = new StringTokenizer(urlImagenes.get(i), "'");

			while(st.hasMoreTokens()) {

				partes.add(st.nextToken());

			}
			
			listaImagenes.add(partes.get(1));
			
			//Borramos el contenido
			partes.clear();
		}
		
		
		//Descargamos cada imagene.
		for(int i=0;i<listaImagenes.size()&&numImagenesDescargadas<5;i++){
        	
			//Creamos la carpeta donde guardaremos las imagenes.
			rutaCarpeta=crearCarpetaImagenes();
		
			imagenDescargada=descargaImagenesInmueble(listaImagenes.get(i), rutaCarpeta, Integer.toString(numImagenesDescargadas));
        	
        	
        	if(imagenDescargada){
        		
        		numImagenesDescargadas++;
        	}
        }
		
        //Actualizar campo numImagenes de la tabla detallesInmueble.
		try{
			
			PreparedStatement pstmt= conexion.prepareStatement("UPDATE detallesInmueble SET det_numImagenes="+numImagenesDescargadas+" where det_codigo="
									+Integer.parseInt(caracteristicasAnuncio.get(15)));
			pstmt.executeUpdate();
		}catch(SQLException ex){
			
			System.out.println("Error al insertar el numero de Imagenes descargadas, el la tabla detallesInmueble de la Base de Datos");
			System.out.println(ex.getMessage()+"\n"+ex.getErrorCode());
		}	
		
	}
	
	/*Metodo que obtiene el ID del inmueble a partir de la URL del anuncio.*/
	public static String obtenerIdInmueble(String enlace){
		
		ArrayList<String> partes = new ArrayList<String>();
				
		StringTokenizer st = new StringTokenizer(enlace, "/");

        while(st.hasMoreTokens()) {

        partes.add(st.nextToken());

        }
        
        return partes.get(4);
	}
	
	/*Metodo que crea la ruta de la carpeta de cada anuncio*/
	public static String crearCarpetaImagenes(){
		
		String ruta = "/Applications/XAMPP/xamppfiles/htdocs/openshift/imagenesAnuncios/Segundamano/".concat(caracteristicasAnuncio.get(13)+"-"+caracteristicasAnuncio.get(10));
		rutaCarpetaImagenes=ruta;
		
		File directorio = new File(ruta);
		directorio.mkdir();
		
		return ruta;
	}
	
	
	public static boolean descargaImagenesInmueble(String src, String rutaCarpeta, String numImagen){
	
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
			
			System.out.println("Ha habido un error al intentar descargar las Imagenes desde Segundamano, probaremos con la siguiente. ID-Inmueble"+caracteristicasAnuncio.get(10));
			System.out.println("Mensaje: "+e.getMessage() + "\nTraza: "+e.getStackTrace());
			
			return false;
		}
        
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

}


