package clases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class prueba {

	public static void main(String[] args) {
		
		//http://www.milanuncios.com/alquiler-de-viviendas-en-javea|xabia-alicante/
		//http://www.milanuncios.com/venta-de-viviendas-en-javea|xabia-alicante/
		prueba3();
	}
	
	public static void prueba1(){
		
		String caracteristicas="";
		boolean existen=false;
		
		try{
			
		
		Document doc = Jsoup.connect("http://www.segundamano.es/alicante/casa-chale-en-camino-viejo-de-gata/a67366758/?ca=3_s&st=a&c=59")
				.userAgent("Mozilla/5.0")
				.post();
		
		Elements listaDetalles = doc.getElementsByClass("descriptionFeatures");
		
		for(Element detalle : listaDetalles){
			
			Elements listaTitulos = detalle.getElementsByTag("dt");
			Elements listasDetalles = detalle.getElementsByTag("dd");
			
			
			for(int i=0;i<listaTitulos.size();i++){
				
				System.out.println(listaTitulos.get(i).text());
				System.out.println("==============================");
				System.out.println(listasDetalles.get(i*2).text());
			}
			
		}
		
		
		
		System.out.println("CARACTERISTICAS");
		
			Elements elemento = doc.getElementsByClass("extra_features_detail_sel");
			for(int i=0;i<elemento.size();i++){
				
				existen=true;
				if(i==elemento.size()-1){
					caracteristicas += elemento.get(i).text()+".";
				}else{
					caracteristicas += elemento.get(i).text()+", ";
				}
				
			}
			
			if(existen){
				
				System.out.println(caracteristicas);
			}else{
				
				caracteristicas="No hay caracteristicas";
				System.out.println(caracteristicas);
			}
			String visitado= doc.getElementsByClass("TimesSeen").text();
			String numVeces="";
			
			for(int i=0;i<visitado.length();i++){
				
				if(Character.isDigit(visitado.charAt(i))){
					
					numVeces += visitado.charAt(i);
					
				}
			}
		
			System.out.println("Se ha visitado el anuncio "+Integer.parseInt(numVeces)+" veces.");
		}catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de Detalles");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}
	}

	public static void prueba2(String urlWeb){
		
		String url="http://www.milanuncios.com";
		String urlDetalles="", referencia="", fecha="", titulo="", categoria="", descripcion="", txtPrecio="", txtHabitaciones="", superficie="";
		ArrayList<String> arrayPaginas = new ArrayList<String>();
		
		int i;
		
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
				
				System.out.println("PAGINA ---- "+x);
				
				i=1;
				
				doc = Jsoup.connect(urlWeb+arrayPaginas.get(x))
						.userAgent("Mozilla/5.0")
						.post();
				
				Elements listaAnuncios = doc.getElementsByClass("x1");
				
				for(Element anuncio : listaAnuncios){
					
					System.out.println("ANUNCIO - "+i);
					urlDetalles=url+anuncio.getElementsByClass("cti").attr("href");	
					System.out.println("URL  - "+urlDetalles);
					
					//Recorremos la url de los detalles de cada anuncio.
					doc = Jsoup.connect(urlDetalles)
							.userAgent("Mozilla/5.0")
							.post();
					
					//Obtener la referencia
					Elements numReferencia = doc.getElementsByClass("anuRefBox");
					
					for(Element detalle : numReferencia){
						
						referencia=detalle.getElementsByTag("b").text();
						
						System.out.println("Referencia: "+referencia);
										
					}
					
					fecha=doc.getElementsByClass("anuFecha").text();
					System.out.println("Fecha: "+fecha);
					
					titulo=doc.getElementsByClass("pagAnuTituloBox").text();
					System.out.println("Titulo: "+titulo);
					
					categoria=doc.getElementsByClass("anuTitulo").text();
					System.out.println("Categoria: "+ categoria);
					
					descripcion=doc.getElementsByClass("pagAnuCuerpoAnu").text();
					System.out.println("Descripcion: "+descripcion);
					
					txtPrecio = doc.getElementsByClass("pr").text();
					
					if(txtPrecio.equals("")){
						
						txtPrecio="0";
					}
					System.out.println("Precio: "+txtPrecio);
					
					
					//Obtener Metros 2
					superficie=doc.getElementsByClass("m2").text();
					
					if(superficie.equals("")){
						
						superficie="0 m2";
					}
					System.out.println("Superficie: "+superficie);
					
					txtHabitaciones=doc.getElementsByClass("dor").text();
					
					if(txtHabitaciones.equals("")){
						
						txtHabitaciones="0";
					}
					
					//Obtener dormitorios
					System.out.println("Dormitorios: "+txtHabitaciones);
					
					
					System.out.println("Enlaces fotos: ");
					
					Elements fotos = doc.getElementsByClass("pagAnuFotoBox");
					
					if(fotos.size()==0){
						
						System.out.println("No hay fotos");
					}else{
						
						for(Element detalle : fotos){
							
							Elements urlImagenes = detalle.getElementsByClass("pagAnuFoto");
							
							for(Element detalleUrl : urlImagenes){
								
								System.out.println(detalleUrl.getElementsByTag("img").attr("src"));
							}
											
						}
					}
					
					System.out.println("-----------------------------");
					i++;
					
					System.exit(0);
				}
				
			}
			
			
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de milanuncios.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}		
	}

	public static void prueba3(){
		
		String url;
		
		try {
			
			Document doc = Jsoup.connect("http://www.milanuncios.com/alquiler-de-viviendas-en-javea|xabia-alicante/"+"?demanda=n")
					.userAgent("Mozilla/5.0")
					.post();
			
			
			Elements listaAnuncios = doc.getElementsByClass("p2");
			
			for(Element anuncio : listaAnuncios){
				
				url=anuncio.getElementsByAttribute("href").attr("href");
				System.out.println(url);	
				
			}
			
			
			System.out.println(doc.getElementsByClass("x4").text());
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de milanuncios.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}		
	}
	
	public static void prueba4(){
		
		String url="http://www.milanuncios.com";
		String urlDetalles="", referencia="", fecha="", titulo="", categoria="", descripcion="", txtPrecio="", txtHabitaciones="", superficie="" ;
		ArrayList<String> arrayPaginas = new ArrayList<String>();
		
		
		
		try{
			
			//Recorremos la url de los detalles de cada anuncio.
			Document doc = Jsoup.connect("http://www.segundamano.es/venta-de-pisos-y-casas-alicante/javea.htm?ca=3_s&fPos=601&fOn=sb_st")
					.userAgent("Mozilla/5.0")
					.post();
			
			Elements listaPaginas = doc.getElementsByClass("paginationLink");
			
			for(Element detalle : listaPaginas){
				
				System.out.println(detalle.getElementsByTag("a").attr("href"));
			
							
			}
			
			
			//Obtener la referencia
			/*
			Elements numReferencia = doc.getElementsByClass("anuRefBox");
			
			for(Element detalle : numReferencia){
				
				referencia=detalle.getElementsByTag("b").text();
				
				System.out.println("Referencia: "+referencia);
								
			}
			
			fecha=doc.getElementsByClass("anuFecha").text();
			System.out.println("Fecha: "+fecha);
			
			titulo=doc.getElementsByClass("pagAnuTituloBox").text();
			System.out.println("Titulo: "+titulo);
			
			categoria=doc.getElementsByClass("anuTitulo").text();
			System.out.println("Categoria: "+ categoria);
			
			descripcion=doc.getElementsByClass("pagAnuCuerpoAnu").text();
			System.out.println("Descripcion: "+descripcion);
			
			txtPrecio = doc.getElementsByClass("pr").text();
			
			if(txtPrecio.equals("")){
				
				txtPrecio="0";
			}
			System.out.println("Precio: "+txtPrecio);
			
			
			//Obtener Metros 2
			superficie=doc.getElementsByClass("m2").text();
			
			if(superficie.equals("")){
				
				superficie="0 m2";
			}
			System.out.println("Superficie: "+superficie);
			
			txtHabitaciones=doc.getElementsByClass("dor").text();
			
			if(txtHabitaciones.equals("")){
				
				txtHabitaciones="0";
			}
			
			//Obtener dormitorios
			System.out.println("Dormitorios: "+txtHabitaciones);
			
			
			System.out.println("Enlaces fotos: ");
			
			Elements fotos = doc.getElementsByClass("pagAnuFotoBox");
			
			if(fotos.size()!=0){
				
			
				for(Element detalle : fotos){
					
					Elements urlImagenes = detalle.getElementsByClass("pagAnuFoto");
					
					for(Element detalleUrl : urlImagenes){
						
						System.out.println(detalleUrl.getElementsByTag("img").attr("src"));
					}
									
				}
			}
			
			//Obtener veces listado
			Elements vecesListado = doc.getElementsByClass("dato");
			System.out.println("EEOOO "+vecesListado.get(0).getElementsByTag("strong").text());
			
			System.out.println("-----------------------------");
			
			
			if(categoria.contains("chal")){
				
				System.out.println("La categoria es chalé");
			}else{
				
				System.out.println("No se reconoce la categoria");
			}
			
			*/
			//System.exit(0);
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de milanuncios.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}	
	}

	public static void prueba5(){
		
		String url;
		
		ArrayList<String> listaPaginas = new ArrayList<String>();
		ArrayList<String> listaURLAnuncios = new ArrayList<String>();

		
		try {
			
			Document doc = Jsoup.connect("http://www.segundamano.es/venta-de-pisos-y-casas-alicante/javea.htm?ca=3_s&fPos=601&fOn=sb_st")
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
				
				if(i==0){
					
					doc = Jsoup.connect("http://www.segundamano.es/venta-de-pisos-y-casas-alicante/javea.htm?ca=3_s&fPos=601&fOn=sb_st")
						.userAgent("Mozilla/5.0")
						.post();
				}else{
					
					doc = Jsoup.connect(listaPaginas.get(i))
						.userAgent("Mozilla/5.0")
						.post();
				}
				
				
				Elements listaAnuncios = doc.getElementsByClass("basicList");
				
				for(Element anuncio : listaAnuncios){
					
					// CARACTERISTICAS ANUNCIO - POS-2 - URL Detalles
					listaURLAnuncios.add(anuncio.getElementsByClass("subjectTitle").attr("href"));
		
				}	
				
				
				for(int x=0;x<listaURLAnuncios.size();x++){
					
					obtenerInfo(listaURLAnuncios.get(x));
				}
			}
			
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web.");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}		
	}
	
	
	public static void obtenerInfo(String url){
		
		String  cadena="", precio="", precioFinal="", cuadroImagenes="";
		int i;
		boolean existenImagenes=true;
		
		
		try {
			
			Document doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0")
					.post();
			
					
			Elements listaAnuncios = doc.getElementsByClass("view");
						
			for(Element anuncio : listaAnuncios){
				
				i=0;
				System.out.println(anuncio.getElementsByClass("productTitle").text());
				// CARACTERISTICAS ANUNCIO - POS-6 - Descripcion Anuncio 
				System.out.println(anuncio.getElementById("descriptionText").text());
				// CARACTERISTICAS ANUNCIO - POS-7 - Vendedor
				System.out.println(anuncio.getElementsByClass("Cname").text());	
				
                //Controlaar si hay imagenes en el anuncio.
                cuadroImagenes=doc.getElementsByClass("centerMachine").attr("alt");
    			
    			if(!cuadroImagenes.equalsIgnoreCase("este anuncio no tiene foto")){
    				
    				System.out.println("URL-IMAGENES");
					try{
						
						while(existenImagenes){
							
							
							
							System.out.println(anuncio.getElementById("thumb"+i).attr("onclick"));
							i++;
							
						}
						
					}catch(NullPointerException ex){
						
						existenImagenes=false;
					}
    			}
			}
			
			
			Elements listaDetalles = doc.getElementsByClass("descriptionFeatures");
			
			for(Element detalle : listaDetalles){
				
				Elements listaTitulos = detalle.getElementsByTag("dt");
				Elements listasDetalles = detalle.getElementsByTag("dd");
				
				
				for(int x=0;x<listaTitulos.size();x++){
					
					cadena=listaTitulos.get(x).text();
					
					switch(cadena){
					
						case "nº hab":
								    // CARACTERISTICAS ANUNCIO - POS-8 - Habitaciones 
							System.out.println(listasDetalles.get(x*2).text());
							break;
						
						case "superficie":
							        // CARACTERISTICAS ANUNCIO - POS-9 - Superficie
							System.out.println(listasDetalles.get(x*2).text());
							break;
							
						case "terreno":
								    // CARACTERISTICAS ANUNCIO - POS-10 - Terreno 
							System.out.println(listasDetalles.get(x*2).text());
							break;
						
						case "categoría":
								    // CARACTERISTICAS ANUNCIO - POS-11 - Categoria 
							System.out.println("categoria"+listasDetalles.get(x*2).text());
							break;
						
						case "tipo":
									// CARACTERISTICAS ANUNCIO - POS-12 - Tipo 
							System.out.println("tipo:"+listasDetalles.get(x*2).text());
							break;
							
						default:
					}
				}
			}
			
			//Cuantas veces se ha visitado el anuncio
			String visitado= doc.getElementsByClass("TimesSeen").text();
			String numVeces="";
			
			for(int x=0;x<visitado.length();x++){
				
				if(Character.isDigit(visitado.charAt(x))){
					
					numVeces += visitado.charAt(x);
					
				}
			}
			
			// CARACTERISTICAS ANUNCIO - POS-13 - Numero de veces Visitado 
			System.out.println(numVeces);
			
			
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
			
			// CARACTERISTICAS ANUNCIO - POS-14 - Precio Inmueble
			System.out.println(precioFinal);
			
			System.out.println("======================================");
		} catch (IOException e) {
			
			System.out.println("Ha habido un error al hacer el Scraping a la Web de Detalles del Inmueble");
			System.out.println(e.getMessage()+"\n"+e.getStackTrace());
		}	
			
	}

}
