// This sample uses the Apache HTTP client library(org.apache.httpcomponents:httpclient:4.2.4)
// and the org.json library (org.json:json:20170516).

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class FaceAPI
{
    // **********************************************
    // *** Update or verify the following values. ***
    // **********************************************

    // Replace the subscriptionKey string value with your valid subscription key.
    public static final String subscriptionKey = "b9e6a92cea184214bac927037c576461";

    // Replace or verify the region.
    //
    // You must use the same region in your REST API call as you used to obtain your subscription keys.
    // For example, if you obtained your subscription keys from the westus region, replace
    // "westcentralus" in the URI below with "westus".
    //
    // NOTE: Free trial subscription keys are generated in the westcentralus region, so if you are using
    // a free trial subscription key, you should not need to change this region.
    public static final String uriBase = "https://eastus2.api.cognitive.microsoft.com/face/v1.0/detect";


    public static void main(String[] args)
    {
    	//FaceServiceClient client = new Face
        HttpClient httpclient = new DefaultHttpClient();

        try
        {
            URIBuilder builder = new URIBuilder(uriBase);

            // Request parameters. All of them are optional.
            builder.setParameter("returnFaceId", "true");
            builder.setParameter("returnFaceLandmarks", "false");
            builder.setParameter("returnFaceAttributes", "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise");

            // Prepare the URI for the REST API call.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/octet-stream"); //octet-stream for local, json for URL
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            // Request body.
            // StringEntity e1 = new StringEntity("{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/c/c3/RH_Louise_Lillian_Gish.jpg\"}");
            
            //NEW CODE: Trying to change the request Entity to be an InputStream
            File dir = new File(".");
            String[] EXTENSIONS = new String[]{"jpg", "png", "gif"};
            FilenameFilter IMAGE_FILTER = new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String filename) {
                    for (String extension : EXTENSIONS) if (filename.endsWith("." + extension)) return true;
                    return false;
                }
            };
            ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
            if (dir.isDirectory()) { // make sure it's a directory
                for (final File f : dir.listFiles(IMAGE_FILTER)) {
                    try {
                    	images.add(ImageIO.read(f));
                    } catch (final IOException e) {
                    }
                }
            }
            
//            BufferedImage bi = ImageIO.read(new File("./pic.jpg"));
            for (BufferedImage bi : images) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bi,"jpg", os); 
                InputStream fis = new ByteArrayInputStream(os.toByteArray());
                
                InputStreamEntity e2 = new InputStreamEntity(fis, -1);
                
                //END NEW CODE
                
                request.setEntity(e2);

                // Execute the REST API call and get the response entity.
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                
                
                
                
                
                HttpClient httpclient2 = new DefaultHttpClient();


                URIBuilder builder2 = new URIBuilder("https://eastus2.api.cognitive.microsoft.com/face/v1.0/facelists/face_list_3");


                URI uri2 = builder2.build();
                HttpPut request2 = new HttpPut(uri2);
                request2.setHeader("Content-Type", "application/json");
                request2.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


                // Request body
                StringEntity reqEntity = new StringEntity("{\"name\": \"face_list_3\"}");
                request2.setEntity(reqEntity);

                HttpResponse response2 = httpclient2.execute(request2);
                HttpEntity entity2 = response2.getEntity();

                if (entity2 != null) {
                    System.out.println(EntityUtils.toString(entity2));
                }
                
                
                
                
                if (entity != null) {
                    // Format and display the JSON response.
                    //System.out.println("REST Response:\n");

                    String jsonString = EntityUtils.toString(entity).trim();
                    Graphics2D BBoxDrawer = bi.createGraphics();
                    BBoxDrawer.setColor(Color.RED);
                    
                    if (jsonString.charAt(0) == '[') {
                        JSONArray jsonArray = new JSONArray(jsonString);
                        ArrayList<String> face_ids = new ArrayList<String>(0);
                        for(int i = 0; i < jsonArray.length(); i++) {
                        	JSONObject jo = jsonArray.getJSONObject(i).getJSONObject("faceRectangle");
                        	face_ids.add(jsonArray.getJSONObject(i).getString("faceId"));
                        	int top = jo.getInt("top");
                        	int left = jo.getInt("left");
                        	int width = jo.getInt("width");
                        	int height = jo.getInt("height");
                        
                        	System.out.println(jsonArray.toString(2));           
                        	BBoxDrawer.drawRect(left, top, width, height);
                        	
                        	
                        	
                        	
                        	URIBuilder builder3 = new URIBuilder("https://eastus2.api.cognitive.microsoft.com/face/v1.0/facelists/face_list_3");

                        	builder3.setParameter("faceListId", "face_list_3");
//                            builder3.setParameter("userData", "{string}");
                            builder3.setParameter("targetFace", left +","+ top +","+ width +","+ height);

                            URI uri3 = builder3.build();
                            HttpPost request3 = new HttpPost(uri3);
                            request.setHeader("Content-Type", "application/octet-stream");
                            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


                            // Request body
                            request.setEntity(e2);

                            HttpResponse response3 = httpclient.execute(request3);
                            HttpEntity entity3 = response3.getEntity();

                            if (entity3 != null) 
                            {
//                                System.out.println(EntityUtils.toString(entity3));
                            }
                        	
                        	
                        	
                        	
                        }  
                        
                        httpclient = new DefaultHttpClient();
                        URIBuilder builder4 = new URIBuilder("https://eastus2.api.cognitive.microsoft.com/face/v1.0/findsimilars");


                        URI uri4 = builder4.build();
                        HttpPost request4 = new HttpPost(uri4);
                        request4.setHeader("Content-Type", "application/json");
                        request4.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


                        // Request body
                        for (int i = 0; i < face_ids.size(); i++) {
                            StringEntity reqEntity4 = new StringEntity("\"faceId\": \""+ face_ids.get(i) + "\",\n\"faceListId\": \"face_list_3\"");
                            request4.setEntity(reqEntity4);

                            HttpResponse response4 = httpclient.execute(request4);
                            HttpEntity entity4 = response4.getEntity();

                            if (entity4 != null) 
                            {
                                System.out.println(EntityUtils.toString(entity4));
                            }
                        }
                        
                        
                        
                    }
                    else if (jsonString.charAt(0) == '{') {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        System.out.println(jsonObject.toString(2));      
                    } else {
                        System.out.println(jsonString);
                    }
                }
                JFrame jf = new JFrame("FaceFinder");
                jf.setSize(bi.getWidth(), bi.getHeight()+45);
                jf.getContentPane().add(new JLabel(new ImageIcon(bi)));	
                jf.setDefaultCloseOperation(3);
                jf.setVisible(true);
            }

        }
        catch (Exception e)
        {
            // Display error message.
            System.out.println(e.getMessage());
        }
    }
}