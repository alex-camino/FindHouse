package clases;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Segundamano extends Thread{

    //public static Connection conexion;
    
    //    VARIABLES GLOBALES NECESARIAS POR ANUNCIO
    public static ArrayList<String> urlImagenes = new ArrayList<String>();
    public static ArrayList<String> caracteristicasAnuncio = new ArrayList<String>();
    public static boolean descargarImagenes=false;
 	public static int operacion, categoria;
 	public static String poblacion="";
 	
 	public void run()
 	{
 		
 		Main.mensajesError.add("\nSCRAPING a la web www.segundamano.es ha comenzado.\n");
 		iniciarScraping();
 		
 	}	
 		
 	
	public static void iniciarScraping() {
		
		for(int i=0;i<Main.listaLocalidades.size();i++){
			
			//Venta
			operacion=1;
			poblacion=Main.listaLocalidades.get(i);
			obtenerPaginas("http://www.segundamano.es/venta-de-pisos-y-casas-alicante/"+Main.listaLocalidades.get(i)+".htm?ca=3_s&fPos=601&fOn=sb_st");
			
			//Alquiler
			operacion=2;
			poblacion=Main.listaLocalidades.get(i);
			obtenerPaginas("http://www.segundamano.es/alquiler-de-pisos-y-casas-alicante/"+Main.listaLocalidades.get(i)+".htm?ca=3_s&fPos=576&fOn=sb_st");
			
		}
		
	}
	
	/*
	 * METODO: ObtenerInfo
	 * ACCIONES: Se encarga de conectar con la página web de segundamano y hacer el Scraping
	 * para obtener las caracteristicas que queramos.
	 * */
	public static void obtenerPaginas(String url){
		
		String fecha, urlDetallesAnuncio;
		int contadorAnuncios;
		ArrayList<String> listaPaginas = new ArrayList<String>();
		
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
			
			for(int i=0;i<Main.numPaginas;i++){
				
				contadorAnuncios=0;
				
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
					
					//Si ha llegado al maximo de anuncios paramos.
					if(contadorAnuncios==Main.cantAnuncios)
						break;
					
					
					//Reiniciar ArrayList.
					caracteristicasAnuncio.clear();
					urlImagenes.clear();
					
					fecha=anuncio.getElementsByClass("date").text();	
					urlDetallesAnuncio=anuncio.getElementsByClass("subjectTitle").attr("href");
					
					obtenerInfoDetallesInmueble(urlDetallesAnuncio, fecha);
					
					contadorAnuncios++;
				}
			
			}
			
		} catch (IOException ex) {
			
			Main.mensajesError.add("\nHa habido un error al hacer el Scraping a la Web principal de Segundamano.es"
					          +"\n"+ex.getMessage()+"\n"+ex.getStackTrace());
		}		
	}
	
	
	/*
	 * METODO: ObtenerInfoDetallesInmueble
	 * Acciones: Se encarga de conectar con la url de los detalles del anuncio y de obtener la información que queramos y
	 * de llamar al metodo de las imagenes para pasarle el arraylist con las URL de las imagenes de cada inmueble
	 * 
	 * */
	public static void obtenerInfoDetallesInmueble(String urlDetalles, String fechaAnuncio){
		
		
		String  cadena="", precio="", precioFinal="", cuadroImagenes="", titulo="", caracteristicas=""
				,descripcion="", vendedor="", habitaciones="", superficie="", terreno="", tipo="", fecha="";
		int i=0;
		boolean existenImagenes=true, anuncioCorrecto=false, existenCaracteristicas=false;
			
		
		try {
			
			Document doc = Jsoup.connect(urlDetalles)
					.userAgent("Mozilla/5.0")
					.post();
			
					
			
			Elements listaAnuncios = doc.getElementsByClass("view");
						
			for(Element anuncio : listaAnuncios){
								
				
				//Controlaar si hay imagenes en el anuncio, si no lo descartamos.
                cuadroImagenes=doc.getElementsByClass("centerMachine").toString();
                
				if(!cuadroImagenes.contains("este anuncio no tiene foto")){
					
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
					
					
					//AÑADIR AL ARRAY DE IMAGENES TODAS LAS QUE HAYAN.
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
				if(tipo.equals(""))
					caracteristicasAnuncio.add("No hay categoria");
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
					
					// CARACTERISTICAS ANUNCIO - POS-7 - Numero de veces Visitado 
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
				

				//-----------CARACTERISTICAS ANUNCIO-------------------------------------------
				Elements elemento = doc.getElementsByClass("extra_features_detail_sel");
				
				for(int x=0;x<elemento.size();x++){
					
					existenCaracteristicas=true;
					if(x==elemento.size()-1){
						caracteristicas += elemento.get(x).text()+".";
					}else{
						caracteristicas += elemento.get(x).text()+", ";
					}
					
				}
				
				if(!existenCaracteristicas){
					
					caracteristicas="No hay caracteristicas";
				}
				//------------------------------------------------------------------------------
							
				//REEMPLAZAR HOY POR LA FECHA ACTUAL
				if(fechaAnuncio.contains("hoy")||fechaAnuncio.contains("HOY")||fechaAnuncio.contains("ayer")||fechaAnuncio.contains("AYER")){
											
					fecha=obtenerFecha(fechaAnuncio);
					
				}else{
					
					fecha=fechaAnuncio;
				}
				
				caracteristicasAnuncio.add(precioFinal);  			         //  POS-8 - Precio Inmueble
				caracteristicasAnuncio.add(obtenerIdInmueble(urlDetalles));  //  POS-9 - ID Inmueble
				caracteristicasAnuncio.add(fecha);                           //  POS-10 - Fecha Anuncio
				caracteristicasAnuncio.add(urlDetalles);                     //  POS-11 - URL Inmueble
				caracteristicasAnuncio.add(caracteristicas);                 //  POS-12-Caracteristicas anuncio
				
				
				//CREAMOS EL INMUEBLE
				crearInmueble();
				
			}
			
		} catch (IOException ex) {
			
			Main.mensajesError.add("\nHa habido un error al hacer el Scraping a la Web de Detalles del Inmueble en Segundamano.es"
			                  +"\n"+ex.getMessage()+"\n"+ex.getStackTrace());
		
		}	
			
	}
	
	public static void crearInmueble(){
		
		Inmueble nuevoInmueble = new Inmueble();
		
		nuevoInmueble.setTitulo(caracteristicasAnuncio.get(0));
		nuevoInmueble.setDescripcion(caracteristicasAnuncio.get(1));
		nuevoInmueble.setVendedor(caracteristicasAnuncio.get(2));
		nuevoInmueble.setHabitaciones(Integer.parseInt(caracteristicasAnuncio.get(3)));
		nuevoInmueble.setSuperficie(caracteristicasAnuncio.get(4));
		nuevoInmueble.setTerreno(caracteristicasAnuncio.get(5));
		nuevoInmueble.setCategoria(detectarCategoria());
		nuevoInmueble.setNumVecesListado(Integer.parseInt(caracteristicasAnuncio.get(7)));
		nuevoInmueble.setIdInmueble(caracteristicasAnuncio.get(9));
		nuevoInmueble.setFecha(caracteristicasAnuncio.get(10));
		nuevoInmueble.setUrl(caracteristicasAnuncio.get(11));
		nuevoInmueble.setCaracteristicas(caracteristicasAnuncio.get(12));
		nuevoInmueble.setOperacion(operacion);
		nuevoInmueble.setPoblacion(poblacion);
		nuevoInmueble.setWebAnuncio("Segundamano");
		
		//Insertamos el inmueble.
		nuevoInmueble.insertarInmueble();
		
		if(nuevoInmueble.getCodInmueble()!=0){
			
			//Añadimos los precios
			if(operacion==1){
				nuevoInmueble.insertarPrecios(0, 0, 0, Integer.parseInt(caracteristicasAnuncio.get(8)));
			}else{
				nuevoInmueble.insertarPrecios(Integer.parseInt(caracteristicasAnuncio.get(8)), 0, 0, 0);
			}
			
			
			//Descargamos las imagenes
			if(descargarImagenes){
				
				obtenerUrlDescargarImagenes(nuevoInmueble.getCodInmueble());
			}
		}	
	}
	
	public static int detectarCategoria(){
		
		String categoria=caracteristicasAnuncio.get(6);
		
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
		
		return numCategoria;
		
	}	
	
	public static String obtenerFecha(String fechaAnuncio){
		
		String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto"," ;Septiembre"
	            ,"Octubre","Noviembre","Diciemrbre"};	
		Date calendario = new Date();
		String formato="dd", fechaBuena;
		SimpleDateFormat formatoDia = new SimpleDateFormat(formato);
					
		formato="MM";
		SimpleDateFormat formatoMes = new SimpleDateFormat(formato);
		
		fechaBuena=formatoDia.format(calendario)+"-"+meses[(Integer.parseInt(formatoMes.format(calendario))-1)];
		
		fechaBuena +=" "+fechaAnuncio.substring((fechaAnuncio.length()-5), fechaAnuncio.length());
		
		return fechaBuena;
	}
	
	/*
	 * METODO QUE OBTIENE LA URL DE LAS IMAGENES.
	 * */
	public static synchronized void obtenerUrlDescargarImagenes(int codigoInmueble){
		
		ArrayList<String> listaImagenes = new ArrayList<String>();
		ArrayList<String> partes = new ArrayList<String>();
		boolean imagenDescargada=false;
		int numImagenesDescargadas=0;
		String rutaCarpeta="";
		
		
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
			rutaCarpeta=crearCarpetaImagenes(codigoInmueble);
		
			imagenDescargada=descargaImagenesInmueble(listaImagenes.get(i), rutaCarpeta, Integer.toString(numImagenesDescargadas));
        	
        	
        	if(imagenDescargada){
        		
        		numImagenesDescargadas++;
        	}
        }
		
		//Actualizar campo numImagenes de la tabla detallesInmueble.
		try{
			
			PreparedStatement pstmt;
			
			if(numImagenesDescargadas!=0){
				
				 pstmt= Main.conexion.prepareStatement("UPDATE inmuebles SET in_numImagenes="+numImagenesDescargadas+" where in_codigo="
							+codigoInmueble);
				 pstmt.executeUpdate();
				 
				 Main.conexion.commit();
				 pstmt.close();
				
			}else{
				
				//Eliminamos la carpeta
				File carpetaEliminar = new File(rutaCarpeta);
				carpetaEliminar.delete();
				
				
				pstmt= Main.conexion.prepareStatement("DELETE FROM precios where precio_codigo in (SELECT in_precioCodigo from inmuebles where in_codigo="+codigoInmueble+")");
				pstmt.executeUpdate();
				pstmt.close();
				
				
			}
			
			
		}catch(SQLException ex){
			
			Main.mensajesError.add("\nError al insertar el numero de Imagenes descargadas, el la tabla Inmuebles de la Base de Datos, en la clase SEGUNDAMANO."
			                  +"\n"+ex.getMessage()+"\n"+ex.getErrorCode());
			
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
	public static String crearCarpetaImagenes(int codigoInmueble){
		
		File nuevo = new File(".");
		
		String ruta = "/Applications/XAMPP/xamppfiles/htdocs/openshift/imagenesAnuncios/Segundamano/".concat(Integer.toString(codigoInmueble)+"-"+caracteristicasAnuncio.get(9));
				
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
			
		}catch (IOException ex) {
			
			Main.mensajesError.add("\nHa habido un error al intentar descargar las Imagenes desde Segundamano, probaremos con la siguiente. ID-Inmueble: "+caracteristicasAnuncio.get(9)
			                  +"\n"+ex.getMessage()+"\n"+ex.getStackTrace());
			
			return false;
		}
        
    }

}


