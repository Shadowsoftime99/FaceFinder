// This sample uses the Apache HTTP client library(org.apache.httpcomponents:httpclient:4.2.4)
// and the org.json library (org.json:json:20170516).

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
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
    public static final String subscriptionKey = "2da159111934452dbc95a0aa44b4cbcf";

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
        try
        {
        	HttpClient httpclient = new DefaultHttpClient();
            URIBuilder builder = new URIBuilder(uriBase);
            String outputTxt = "";
            int totalFaces = 0;

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
            
            //Block below manages files and pulls all image files from the folder.
            File dir = new File("./input");
            String[] EXTENSIONS = new String[]{"jpg", "png", "gif"};
            FilenameFilter IMAGE_FILTER = new FilenameFilter() 
            {
                @Override
                public boolean accept(final File dir, final String filename) 
                {
                    for (String extension : EXTENSIONS) 
                    	if (filename.endsWith("." + extension)) return true;
                    return false;
                }
            };
            
            ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
            if (dir.isDirectory()) 
            { // make sure it's a directory
                for (final File f : dir.listFiles(IMAGE_FILTER)) 
                {
                    try 
                    {
                    	images.add(ImageIO.read(f));
                    } 
                    catch (IOException e){}
                }
            }
            
            HttpClient httpclient2 = new DefaultHttpClient();


            URIBuilder builder2 = new URIBuilder("https://eastus2.api.cognitive.microsoft.com/face/v1.0/facelists/face_list");


            URI uri2 = builder2.build();
            HttpPut request2 = new HttpPut(uri2);
            request2.setHeader("Content-Type", "application/json");
            request2.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);


            // Request body
            StringEntity reqEntity = new StringEntity("{\"name\": \"face_list\"}");
            request2.setEntity(reqEntity);

            HttpResponse response2 = httpclient2.execute(request2);
            HttpEntity entity2 = response2.getEntity();
/*
            if (entity2 != null) 
            {
                System.out.println("2: " + EntityUtils.toString(entity2));
            }*/
            
            ArrayList<String> face_ids = new ArrayList<String>(0);
            ArrayList<Integer> ids_to_img = new ArrayList<Integer>(0);
            ArrayList<String> co_ords = new ArrayList<String>(0);
            
            int in = 1;
            for (BufferedImage bi : images) 
            {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bi,"jpg", os); 
                InputStream fis = new ByteArrayInputStream(os.toByteArray());
                
                InputStreamEntity e2 = new InputStreamEntity(fis, -1);
                
                request.setEntity(e2);

                // Execute the REST API call and get the response entity.
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
  
                if (entity != null) 
                {
                    // Format and display the JSON response.
                    //System.out.println("REST Response:\n");

                    String jsonString = EntityUtils.toString(entity).trim();
                    Graphics2D BBoxDrawer = bi.createGraphics();
                    BBoxDrawer.setColor(Color.RED);
                    
                    if (jsonString.charAt(0) == '[') {
                        JSONArray jsonArray = new JSONArray(jsonString);
                        
                        for(int i = 0; i < jsonArray.length(); i++) {
                        	JSONObject jo = jsonArray.getJSONObject(i).getJSONObject("faceRectangle");
                        	face_ids.add(jsonArray.getJSONObject(i).getString("faceId"));
                        	ids_to_img.add(in);
                        	
                        	int top = jo.getInt("top");
                        	int left = jo.getInt("left");
                        	int width = jo.getInt("width");
                        	int height = jo.getInt("height");
                        	co_ords.add("(" + left +", "+ top + ") - ("+ (left+width) +", "+ (top+height) + ")");
                       
                        	BBoxDrawer.drawRect(left, top, width, height);
                        	totalFaces += 1;
                        	BBoxDrawer.setFont(new Font(null, Font.BOLD, 14));
                        	BBoxDrawer.drawString("Face " + totalFaces, left, top);
                        }            
                    }
                    else if (jsonString.charAt(0) == '{') {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        System.out.println(jsonObject.toString(2));      
                    } else {
                        System.out.println(jsonString);
                    }
                }
                in++;
            }       

            JSONArray ids = new JSONArray();
            for(int i = 0; i < face_ids.size(); i++)
            	ids.put(face_ids.get(i));
            
            HttpClient httpclient4 = new DefaultHttpClient();
            URIBuilder builder4 = new URIBuilder("https://eastus2.api.cognitive.microsoft.com/face/v1.0/findsimilars");

            URI uri4 = builder4.build();
            HttpPost request4 = new HttpPost(uri4);
            request4.setHeader("Content-Type", "application/json");
            request4.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            // Request body
            for (int i = 0; i < face_ids.size(); i++) 
            {
            	httpclient4 = new DefaultHttpClient();
            	String current = (String)ids.remove(0);
                StringEntity reqEntity4 = new StringEntity("{\"faceId\": \""+ face_ids.get(i) + "\",\n\"faceids\": " + ids.toString() + "}");
                ids.put(current);
                request4.setEntity(reqEntity4);

                HttpResponse response4 = httpclient4.execute(request4);
                HttpEntity entity4 = response4.getEntity();

                if (entity4 != null) 
                {
                	String jsonString = EntityUtils.toString(entity4).trim();
                   // System.out.println(jsonString);
                    JSONArray jsonArray = new JSONArray(jsonString);
                    if(jsonArray.length() != 0)
                    {
                    	outputTxt += "The face [" +(i+1) + "] at " + co_ords.get(i) + " in image " + ids_to_img.get(i) + " matches with:\n";
                    	
                    	for(int j = 0; j < jsonArray.length(); j++)
                    	{
                    		String key = jsonArray.getJSONObject(j).getString("faceId");
                    		int matchInd = face_ids.indexOf(key);
                    		
                    		outputTxt += "    the face [" +(matchInd+1) + "] at " + co_ords.get(matchInd) + " in image " + ids_to_img.get(matchInd) + "\n";
                    	}
                    	outputTxt += "\n\n";
                    }
                }
            }  
            HttpClient httpclient5 = new DefaultHttpClient();
            URIBuilder builder5 = new URIBuilder("https://eastus2.api.cognitive.microsoft.com/face/v1.0/facelists/face_list");

            URI uri5 = builder5.build();
            HttpDelete request5 = new HttpDelete(uri5);
            request5.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            // Request body
            HttpResponse response5 = httpclient5.execute(request5);
            HttpEntity entity5 = response5.getEntity();
            
           /* if(entity != null)
            {
            	System.out.println("5: " + EntityUtils.toString(entity));
            }*/
            
            int ou = 1;
            for(BufferedImage bi : images)
            {
            	File f = new File("./output/out"+ou+".png");
            	ImageIO.write(bi, "PNG", f);
            	ou++;
            }
            
           // System.out.println(outputTxt);
            PrintWriter writer = new PrintWriter("./output/match_info.txt", "UTF-8");
            writer.write(outputTxt);
            writer.close();
            JOptionPane.showMessageDialog(null, "Analysis finished! Open the output folder to see the results.", "FaceFinder", -1);
        }
        catch (Exception e)
        {
            // Display error message.
            System.out.println(e.getMessage());
        }
    }
}