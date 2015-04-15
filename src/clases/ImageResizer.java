package clases;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
 
public class ImageResizer {
    //Ancho máximo
    private int maxWidth;

    private int maxHeight;

    public ImageResizer(){
    	
    }
    
    public ImageResizer (int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }
 
    /*
    public void copiarImagen(String filePath, String copyPath) {
        BufferedImage nuevaImagen = cargarImagen(filePath);
        if(nuevaImagen.getHeight()>nuevaImagen.getWidth()){
            int heigt = (nuevaImagen.getHeight() * maxWidth) / nuevaImagen.getWidth();
            nuevaImagen = resize(nuevaImagen, maxWidth, heigt);
            int width = (nuevaImagen.getWidth() * maxHeight) / nuevaImagen.getHeight();
            nuevaImagen = resize(nuevaImagen, width, maxHeight);
        }else{
            int width = (nuevaImagen.getWidth() * maxHeight) / nuevaImagen.getHeight();
            nuevaImagen = resize(nuevaImagen, width, maxHeight);
            int heigt = (nuevaImagen.getHeight() * maxWidth) / nuevaImagen.getWidth();
            nuevaImagen = resize(nuevaImagen, maxWidth, heigt);
        }
        GuardarImagen(nuevaImagen, copyPath);
    }
    */
    
    /*
    Con este método, cargamos la imagen inicial, indicándole el path
    */
    public static void cargarImagen(String pathName, int newW, int newH) {
        BufferedImage nuevaImagen = null;
        try {
            nuevaImagen = ImageIO.read(new File(pathName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        resize(nuevaImagen, newW, newH, pathName);
    }

    /*
    Este método se utiliza para redimensionar la imagen
    */
    public static void resize(BufferedImage bufferedImage, int newW, int newH, String pathName) {
        int w = bufferedImage.getWidth();
        int h = bufferedImage.getHeight();
        BufferedImage imagenRedimensionada = new BufferedImage(newW, newH, bufferedImage.getType());
        Graphics2D g = imagenRedimensionada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(bufferedImage, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        
        GuardarImagen(imagenRedimensionada, pathName);;
    }
 
    /*
    Con este método almacenamos la imagen en el disco
    */
    public static void GuardarImagen(BufferedImage bufferedImage, String pathName) {
        try {
            String format = (pathName.endsWith(".png")) ? "png" : "jpg";
            File file =new File(pathName);
            file.getParentFile().mkdirs();
            ImageIO.write(bufferedImage, format, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
