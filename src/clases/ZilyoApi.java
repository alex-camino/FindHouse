package clases;


import java.io.IOException;

import org.json.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class ZilyoApi {
	
	
	public static void iniciarApi(){
		
				
		CloseableHttpClient httpclient = HttpClients.createDefault();
		//&page=1&resultsperpage=50
		HttpGet httpget = new HttpGet("https://zilyo.p.mashape.com/search?isinstantbook=true&nelatitude=38.806908&nelongitude=0.100792&swlatitude=38.725262&swlongitude=0.241554");
		httpget.setHeader("X-Mashape-Key", "uic36LiXULmshup21bgSTRz0PqRLp1FwL8ijsnPZTpwszssFrR");
		httpget.setHeader("Accept", "application/json");
		
		try {
			CloseableHttpResponse response = httpclient.execute(httpget);
			
			
			JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity()));
			
			//String pageName = obj.getJSONObject("pageInfo").getString("pageName");
		
			JSONArray arr = obj.getJSONArray("result");
			
			if(arr.length()==0){
				
				System.out.println("No existe esa pagina.");
			}else{
				
				for (int i = 0; i < arr.length(); i++)
				{
				    System.out.println("ID: "+arr.getJSONObject(i).getString("id"));
				    System.out.println("TIPO: "+arr.getJSONObject(i).getJSONObject("attr").getJSONObject("propType").getString("text"));
				    System.out.println("HABITACIONES: "+arr.getJSONObject(i).getJSONObject("attr").getInt("bedrooms"));
				    System.out.println("PRECIOS: "
				    				  +"\nPor mes: "+arr.getJSONObject(i).getJSONObject("price").getInt("monthly")
				    				  +"\nPor noche: "+arr.getJSONObject(i).getJSONObject("price").getInt("nightly")
				    				  +"\nFin de Semana: "+arr.getJSONObject(i).getJSONObject("price").getInt("weekend")
				    				  +"\nPor semana: "+arr.getJSONObject(i).getJSONObject("price").getInt("weekly"));
				    
				    JSONArray fotos = arr.getJSONObject(i).getJSONArray("photos");
				    System.out.println("ENLACES FOTOS");
				    System.out.println("=============");
				    for(int x=0; x<fotos.length();x++){
				    	
				    	String foto=fotos.getJSONObject(x).getString("large");
				    	System.out.println(foto);
				    	
				    }
				    System.out.println("FIN FOTOS");
				    System.out.println("ENLACE ANUNCIO: "+arr.getJSONObject(i).getJSONObject("provider").getString("url"));
				    System.out.println("LOCALIDAD: "+arr.getJSONObject(i).getJSONObject("location").getString("city"));
				    
				    JSONArray caracteristicas = arr.getJSONObject(i).getJSONArray("amenities");
				    System.out.println("CARACTERISTICAS");
				    System.out.println("=============");
				    for(int x=0; x<caracteristicas.length();x++){
				    	
				    	System.out.println(caracteristicas.getJSONObject(x).getString("text"));
				    	
				    }
				    System.out.println("FIN CARACTERISTICAS");
				    
				    //System.out.println("DESCRIPCION: \n"+arr.getJSONObject(i).getJSONObject("attr").getString("description"));
				    System.out.println("=======================================");
				}
				
			}
			
			
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
