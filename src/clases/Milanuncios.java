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
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Milanuncios extends Thread{

    public static ArrayList<String> listaFotos = new ArrayList<String>();
    public static ArrayList<String> contenedorAnuncio = new ArrayList<String>();
    public static String poblacion;
    public static int operacion;
    
 	public void run()
 	{
 		
 			Main.mensajesError.add("\nSCRAPING a la web www.milanuncios.com ha comenzado.\n");
 			iniciarScraping();
			
 	}
	public static void iniciarScraping() 
	{
						
		for(int i=0;i<Main.listaLocalidades.size();i++){
			
			if(i==0){
				
				obtenerInfo("http://www.milanuncios.com/venta-de-viviendas-en-javea|xabia-alicante/", 1, Main.listaLocalidades.get(i));
				obtenerInfo("http://www.milanuncios.com/alquiler-de-viviendas-en-javea|xabia-alicante/", 2, Main.listaLocalidades.get(i));
				
			}else{
				
				obtenerInfo("http://www.milanuncios.com/venta-de-viviendas-en-"+Main.listaLocalidades.get(i)+"-alicante/", 1, Main.listaLocalidades.get(i));
				obtenerInfo("http://www.milanuncios.com/alquiler-de-viviendas-en-"+Main.listaLocalidades.get(i)+"-alicante/", 2, Main.listaLocalidades.get(i));
			}
			
		}
				
	}
	
	/*
	 * METODO: ObtenerInfo
	 * Acciones: Se encarga de conectar con la página web de milanuncios y hacer el Scraping
	 * para obtener todas las URL de cada anuncio. Luego ire conectandose a la url de los detalles de cada anuncio
	 * para poder obtener la información que nosotros quereamos.
	 * 
	 * */
	public static void obtenerInfo(String urlWeb, int numOperacion, String nomPoblacion){
		
		String url="http://www.milanuncios.com";
		String urlDetalles="", idInmueble="", fecha="", titulo="", categoria="", descripcion="", txtPrecio="", precioReal="", txtHabitaciones="",
			   numHabitaciones="", superficie="", vecesListado="";
		ArrayList<String> arrayPaginas = new ArrayList<String>();
		int contadorAnuncios;

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
			for(int x=0;x<Main.numPaginas;x++){
				
				contadorAnuncios=0;
				
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
					
					//Si llegamos al tope de Anuncios por pagina, paramos el FOR
					if(contadorAnuncios==Main.cantAnuncios)
						break;
					
					
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
							
							idInmueble=detalle.getElementsByTag("b").text();
											
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
						contenedorAnuncio.add(titulo);
						contenedorAnuncio.add(descripcion);
						contenedorAnuncio.add("");
						contenedorAnuncio.add(numHabitaciones);
						contenedorAnuncio.add(superficie);
						contenedorAnuncio.add("");
						contenedorAnuncio.add(categoria);
						contenedorAnuncio.add(vecesListado);
						contenedorAnuncio.add(precioReal);
						contenedorAnuncio.add(idInmueble);
						contenedorAnuncio.add(fecha);
						contenedorAnuncio.add(urlDetalles);
						operacion=numOperacion;
						poblacion=nomPoblacion;
						
						//Creamos el inmueble.
						crearInmueble();
						
						contadorAnuncios++;

					}
					
				}	
			}
		} catch (IOException ex) {
			
			Main.mensajesError.add("\nHa habido un error al hacer el Scraping a la Web de Milanuncios."
			          +"\n"+ex.getMessage()+"\n"+ex.getStackTrace());
		}		
	}
	
	
	public static void crearInmueble(){
		
		Inmueble nuevoInmueble = new Inmueble();
		
		nuevoInmueble.setTitulo(contenedorAnuncio.get(0));
		nuevoInmueble.setDescripcion(contenedorAnuncio.get(1));
		nuevoInmueble.setVendedor("N.N.");
		nuevoInmueble.setHabitaciones(Integer.parseInt(contenedorAnuncio.get(3)));
		nuevoInmueble.setSuperficie(contenedorAnuncio.get(4));
		nuevoInmueble.setTerreno("0 m2");
		nuevoInmueble.setCategoria(detectarCategoria());
		nuevoInmueble.setNumVecesListado(Integer.parseInt(contenedorAnuncio.get(7)));
		nuevoInmueble.setIdInmueble(contenedorAnuncio.get(9));
		nuevoInmueble.setFecha(contenedorAnuncio.get(10));
		nuevoInmueble.setUrl(contenedorAnuncio.get(11));
		nuevoInmueble.setCaracteristicas("No existen caracteristicas.");
		nuevoInmueble.setOperacion(operacion);
		nuevoInmueble.setPoblacion(poblacion);
		nuevoInmueble.setWebAnuncio("Milanuncios");
		
		//Insertamos el inmueble.
		nuevoInmueble.insertarInmueble();
		
		
		if(nuevoInmueble.getCodInmueble()!=0){
			
			//Añadimos los precios
			if(operacion==1){
				nuevoInmueble.insertarPrecios(0, 0, 0, Integer.parseInt(contenedorAnuncio.get(8)));
			}else{
				nuevoInmueble.insertarPrecios(Integer.parseInt(contenedorAnuncio.get(8)), 0, 0, 0);
			}
			//Añadimos los precios.
			
			//Descargamos todas las imagenes.
			descargaImagenes(contenedorAnuncio.get(9), nuevoInmueble.getCodInmueble());

		}
		
	}
	
	
	public static int detectarCategoria(){
		
		String categoria=contenedorAnuncio.get(6);
		
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
		else
			return 12;
	}
	
	/*
	 * Metodo que va descargando una a una las imagenes.
	 * */
	public static synchronized void descargaImagenes(String idInmueble, int codigoInmueble){
		
		String rutaCarpeta="";
		boolean imagenDescargada;
		int numImagenesDescargadas=0;
		
		if(listaFotos.size()!=0){
			
			//Creamos la carpeta donde guardaremos las imagenes.
			rutaCarpeta=crearCarpetaImagenes(idInmueble, codigoInmueble);
		
			for(int i=0;i<listaFotos.size()&&numImagenesDescargadas<5;i++){
				
				imagenDescargada=descargaImagenesInmueble(listaFotos.get(i), rutaCarpeta, Integer.toString(numImagenesDescargadas), idInmueble);
				
				if(imagenDescargada)
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
			
			Main.mensajesError.add("\nError al insertar el numero de Imagenes descargadas, el la tabla Inmuebles de la Base de Datos, desde la clase MILANUNCIOS."
			                  +"\n"+ex.getMessage()+"\n"+ex.getErrorCode());
			
		}	
	}
	
	public static String crearCarpetaImagenes(String idInmueble, int codInmueble){
		
		File nuevo = new File(".");
				
		String ruta = "/Applications/XAMPP/xamppfiles/htdocs/openshift/imagenesAnuncios/Milanuncios/".concat(Integer.toString(codInmueble)+"-"+contenedorAnuncio.get(9));
		
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
		}catch (IOException ex) {
			
			Main.mensajesError.add("\nHa habido un error al intentar descargar las Imagenes desde Milanuncios, probaremos con la siguiente. ID-Inmueble: "+idInmueble
	                  +"\n"+ex.getMessage()+"\n"+ex.getStackTrace());
			
			return false;
		}
       
    }
}

