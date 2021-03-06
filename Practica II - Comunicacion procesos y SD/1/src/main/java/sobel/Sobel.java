package sobel;

import java.awt.image.BufferedImage;

public class Sobel {

  static int [] sobel_y = { 1,  0, -1, 2, 0, -2,  1,  0, -1 };    
  static int [] sobel_x = { 1,  2,  1, 0, 0,  0, -1, -2, -1 };
  
    
    public static BufferedImage sobel (BufferedImage edgesX, BufferedImage edgesY) {
      BufferedImage result = new BufferedImage(edgesX.getWidth(), edgesX.getHeight(), BufferedImage.TYPE_INT_RGB);
      int height = result.getHeight();
      int width = result.getWidth();
       for(int x = 0; x < width ; x++){
         for(int y = 0; y < height; y++){
           int tmp = Math.abs(edgesX.getRGB(x, y)&0xff) + Math.abs(edgesY.getRGB(x, y)&0xff);
               result.setRGB(x,y, 0xff000000|(tmp<<16)|(tmp<<8)|tmp);
       } 
        }
      return result; 
    }
      
    private static BufferedImage edgeDetection(BufferedImage img, int[] kernel) {
        int height = img.getHeight();
        int width = img.getWidth();

        BufferedImage result = new BufferedImage(width -1, height -1, BufferedImage.TYPE_INT_RGB);
        for(int x = 1; x < width -1 ; x++){
            for(int y = 1; y < height - 1; y++){
                int [] tmp = {img.getRGB(x-1, y-1)&0xff,img.getRGB(x, y-1)&0xff,img.getRGB(x+1, y-1)&0xff,
                img.getRGB(x-1, y)&0xff,img.getRGB(x, y)&0xff,img.getRGB(x+1, y)&0xff,img.getRGB(x-1, y+1)&0xff,
                img.getRGB(x, y+1)&0xff,img.getRGB(x+1, y+1)&0xff};
                int value = convolution (kernel, tmp);
                result.setRGB(x,y, 0xff000000|(value<<16)|(value<<8)|value);
            }
        }
        return result;
    }
    
    public static BufferedImage edgeDetectionX(BufferedImage img) {
      return edgeDetection(img, sobel_x);
    }   
    public static BufferedImage edgeDetectionY(BufferedImage img) {
      return edgeDetection(img, sobel_y);
    }  
   
    private static int convolution (int [] kernel, int [] pixel){
        int result = 0; 

        for (int i = 0; i < pixel.length; i++){
            result += kernel[i] * pixel[i];
        }
        return (int)(Math.abs(result) / 9);
    }
   
  
   public static BufferedImage greyscale(BufferedImage img){
  
   //get image width and height
      int width = img.getWidth();
      int height = img.getHeight();

      //convert to grayscale
      for(int y = 0; y < height; y++){
        for(int x = 0; x < width; x++){
          int p = img.getRGB(x,y);

          int a = (p>>24)&0xff;
          int r = (p>>16)&0xff;
          int g = (p>>8)&0xff;
          int b = p&0xff;

          //calculate average
          int avg = (r+g+b)/3;

          //replace RGB value with avg
          p = (a<<24) | (avg<<16) | (avg<<8) | avg;

          img.setRGB(x, y, p);
        }
      }
     return img;
   }
}
