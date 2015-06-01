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

import org.json.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class ZilyoApi extends Thread{
	
   // public static Connection conexion;
	
	//    VARIABLES GLOBALES NECESARIAS POR ANUNCIO
    public static ArrayList<String> urlImagenes = new ArrayList<String>();
    public static ArrayList<String> caracteristicasAnuncio = new ArrayList<String>();
	public static ArrayList<Integer> precios = new ArrayList<Integer>();
	public static int operacion=2;
	public static String poblacion;
	
	
	public void run()
 	{
 		
		Main.mensajesError.add("\nConectándose a la API de ZILYO...\n");
		iniciarApi();
				
 	}
	
	
	public static void iniciarApi(){
		
		String categoria, caract="";
		
		for(int z=0;z<Main.listaCoordenadas.size();z++){
			
			for(int k=1;k<=Main.numPaginas;k++){
				
				poblacion=Main.listaLocalidades.get(z);
				
				CloseableHttpClient httpclient = HttpClients.createDefault();
				//&page=1&resultsperpage=50
				HttpGet httpget = new HttpGet("https://zilyo.p.mashape.com/search?isinstantbook=true&nelatitude="+Main.listaCoordenadas.get(z)[0]
											  +"&nelongitude="+Main.listaCoordenadas.get(z)[1]
											  +"&swlatitude="+Main.listaCoordenadas.get(z)[2]
											  +"&swlongitude="+Main.listaCoordenadas.get(z)[3]
											  +"&resultsperpage="+Main.cantAnuncios+"&page="+k);
				httpget.setHeader("X-Mashape-Key", "uic36LiXULmshup21bgSTRz0PqRLp1FwL8ijsnPZTpwszssFrR");
				httpget.setHeader("Accept", "application/json");
				
				try {
					CloseableHttpResponse response = httpclient.execute(httpget);
					
					
					JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity()));
							
					JSONArray arr = obj.getJSONArray("result");
					
					if(arr.length()==0){
						
						Main.mensajesError.add("API ZILYO -- No existe mas paginas.");
					}else{
						
						for (int i = 0; i < arr.length(); i++)
						{
							//Reseteamos los ArrayList
							urlImagenes.clear();
							caracteristicasAnuncio.clear();					
							precios.clear();
							
							
							categoria=arr.getJSONObject(i).getJSONObject("attr").getJSONObject("propType").getString("text");
							
							if(categoria.contains("House")||categoria.contains("Apartment")||categoria.contains("Villa")){
								
								
				/*Pos 0: Titulo*/			caracteristicasAnuncio.add(arr.getJSONObject(i).getJSONObject("attr").getString("heading"));
				/*Pos 1: Descripcion*/		caracteristicasAnuncio.add(arr.getJSONObject(i).getJSONObject("attr").getString("description"));
				/*Pos 2: Vendedor*/			caracteristicasAnuncio.add("N.N.");
				/*Pos 3: Habitaciones*/		caracteristicasAnuncio.add(Integer.toString(arr.getJSONObject(i).getJSONObject("attr").getInt("bedrooms")));
				/*Pos 4: Superficie*/		caracteristicasAnuncio.add("--");
				/*Pos 5: Terreno*/			caracteristicasAnuncio.add("--");
				/*Pos 6: Categoria*/		caracteristicasAnuncio.add(Integer.toString(detectarCategoria(categoria)));
				/*Pos 7: Nº Veces*/			caracteristicasAnuncio.add("0");
				/*Pos 8: ID-Inmueble*/		caracteristicasAnuncio.add(arr.getJSONObject(i).getString("id"));	
				/*Pos 9: Fecha*/			caracteristicasAnuncio.add(obtenerFecha());	
				/*Pos 10: URL*/				caracteristicasAnuncio.add(arr.getJSONObject(i).getJSONObject("provider").getString("url"));
				

										    JSONArray caracteristicas = arr.getJSONObject(i).getJSONArray("amenities");
										   
										    for(int x=0; x<caracteristicas.length();x++){
										    	
										    	if(x==caracteristicas.length()-1){
													caract += caracteristicas.getJSONObject(x).getString("text")+".";
												}else{
													caract += caracteristicas.getJSONObject(x).getString("text")+", ";
												}
										   								    	
										    }
										    
				/*Pos 11: Caracteris.*/		caracteristicasAnuncio.add(caract);
				/*Pos 12: Web Anuncio.*/	caracteristicasAnuncio.add(arr.getJSONObject(i).getJSONObject("provider").getString("full"));
				
											JSONArray fotos = arr.getJSONObject(i).getJSONArray("photos");
										    
										    for(int x=0; x<fotos.length();x++){
										    	
										    	urlImagenes.add(fotos.getJSONObject(x).getString("large"));
										    									    	
										    }
										    
										    //PRECIOS
										    precios.add(arr.getJSONObject(i).getJSONObject("price").getInt("monthly")); // MES
										    precios.add(arr.getJSONObject(i).getJSONObject("price").getInt("weekend")); // SEMANA
										    precios.add(arr.getJSONObject(i).getJSONObject("price").getInt("nightly")); // NOCHE
										    precios.add(0);																// NORMAL
						    				  
						    
										    crearInmueble();
				  
							}
						}
					
					}	
				} catch (ClientProtocolException e) {
					
					Main.mensajesError.add("Error al crear el Cliente HTTP en la API de ZILYO.\nERROR: "+e.getMessage()+"\nTRAZA: "+e.getStackTrace());
					
				} catch (IOException e) {
					
					Main.mensajesError.add("Error al intentar ejecutar la API de ZILYO.\nERROR: "+e.getMessage()+"\nTRAZA: "+e.getStackTrace());
					
				} catch (JSONException e) {
					
					Main.mensajesError.add("Error en el JSON de la API de ZILYO.\nERROR: "+e.getMessage()+"\nTRAZA: "+e.getStackTrace());
				}
				
			}
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
		nuevoInmueble.setCategoria(Integer.parseInt(caracteristicasAnuncio.get(6)));
		nuevoInmueble.setNumVecesListado(Integer.parseInt(caracteristicasAnuncio.get(7)));
		nuevoInmueble.setIdInmueble(caracteristicasAnuncio.get(8));
		nuevoInmueble.setFecha(caracteristicasAnuncio.get(9));
		nuevoInmueble.setUrl(caracteristicasAnuncio.get(10));
		nuevoInmueble.setCaracteristicas(caracteristicasAnuncio.get(11));
		nuevoInmueble.setOperacion(operacion);
		nuevoInmueble.setPoblacion(poblacion);
		nuevoInmueble.setWebAnuncio(caracteristicasAnuncio.get(12));
		
		//Insertamos el inmueble.
		nuevoInmueble.insertarInmueble();
		
		if(nuevoInmueble.getCodInmueble()!=0){
			
			//Añadimos los precios
			nuevoInmueble.insertarPrecios(precios.get(0),precios.get(1),precios.get(2),precios.get(3));
			
			//Descargamos las imagenes
			if(urlImagenes.size()!=0){
				
				descargaImagenes(nuevoInmueble.getIdInmueble(), nuevoInmueble.getCodInmueble());
			}
		}	
	}
	
	
	public static String obtenerFecha(){
		
		String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto"," ;Septiembre"
	            ,"Octubre","Noviembre","Diciemrbre"};	
		Date calendario = new Date();
		String fecha = calendario.toString();
		String formato="dd", fechaBuena;
		SimpleDateFormat formatoDia = new SimpleDateFormat(formato);
					
		formato="MM";
		SimpleDateFormat formatoMes = new SimpleDateFormat(formato);
		
		fechaBuena=formatoDia.format(calendario)+"-"+meses[(Integer.parseInt(formatoMes.format(calendario))-1)];
		fechaBuena += "-"+fecha.substring((fecha.length()-4), fecha.length());	
		
		return fechaBuena;
	}
	
	
	public static int detectarCategoria(String categoria){
		
		
		if(categoria.contains("House"))
			return 1;
		else if(categoria.contains("Villa"))
			return 2;
		else if(categoria.contains("Apartment"))
				return 5;
		else if(categoria.contains("Divers"))
				return 12;
		else
				return 12;
	}
	
	
	/*
	 * Metodo que va descargando una a una las imagenes.
	 * */
	public static synchronized void descargaImagenes(String idInmueble, int codigoInmueble){
		
		String rutaCarpeta;
		boolean imagenDescargada;
		int numImagenesDescargadas=0;
		
		//Creamos la carpeta donde guardaremos las imagenes.
		rutaCarpeta=crearCarpetaImagenes(idInmueble, codigoInmueble);
	
		for(int i=0;i<urlImagenes.size()&&numImagenesDescargadas<5;i++){
			
			imagenDescargada=descargaImagenesInmueble(urlImagenes.get(i), rutaCarpeta, Integer.toString(numImagenesDescargadas), idInmueble);
			
			if(imagenDescargada)
				numImagenesDescargadas++;
			
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
			
			Main.mensajesError.add("\nError al insertar el numero de Imagenes descargadas, el la tabla Inmuebles de la Base de Datos, desde la clase ZILYOAPI."
			                  +"\n"+ex.getMessage()+"\n"+ex.getErrorCode());
			
		}	
		
		
	}
	
	public static String crearCarpetaImagenes(String idInmueble, int codInmueble){
		
		File nuevo = new File(".");
		
		String ruta = "/Applications/XAMPP/xamppfiles/htdocs/openshift/imagenesAnuncios/Zilyo-Api/".concat(Integer.toString(codInmueble)+"-"+idInmueble);
		
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
			
			Main.mensajesError.add("\nHa habido un error al intentar descargar las Imagenes desde la API de Zilyo, probaremos con la siguiente. ID-Inmueble: "+idInmueble
	                  +"\n"+ex.getMessage()+"\n"+ex.getStackTrace());
			
			return false;
		}
       
    }
	
	
}
