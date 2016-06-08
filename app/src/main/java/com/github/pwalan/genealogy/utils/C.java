package com.github.pwalan.genealogy.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


public class C {
	
	static private final int Method=3;
	static public final int TRANSPORT_METHOD_JSON=1;
	static public final int TRANSPORT_METHOD_XML=2;
	static public final int TRANSPORT_METHOD_MULITY=3;
	
	static public JSONObject asyncPost(String toUrl, HashMap content) {
		PostThread t = new PostThread(toUrl, content);
		t.start();
		try 
		{
			t.join();
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return t.res;
	}
	
	static class PostThread extends Thread {

		private String toUrl;
		private HashMap content;

		public JSONObject res;

		PostThread(String toUrl, HashMap content)
		{
			this.toUrl = toUrl;
			this.content = content;
		}

		@Override
		public void run() 
		{
			res = C.post(toUrl, content);
		}
	}
	
	public static JSONObject post(String toUrl, HashMap content) {

		switch(Method)
		{
		case 1:
			try 
			{
				JSONObject req=new JSONObject();
				for(Object okey:content.keySet())
				{
					String key=okey.toString();
					req.put(key, content.get(key));
				}
				
				URL url = new URL(toUrl);
				HttpURLConnection connection=(HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				
				connection.connect();
				DataOutputStream dos=new DataOutputStream(connection.getOutputStream());
				
				dos.writeBytes(URLEncoder.encode(req.toString(), "UTF-8"));
				dos.flush();
				dos.close();
				
				BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
				StringBuffer sb=new StringBuffer();
				String line;
				while((line=br.readLine())!=null)
					sb.append(line);
				br.close();
				connection.disconnect();
				return new JSONObject(sb.toString());
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case 2:
			try
			{
				String req=Map2XML(content);
				
				URL url = new URL(toUrl);
				HttpURLConnection connection=(HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Charset", "UTF-8");

				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				
				connection.connect();
				DataOutputStream dos=new DataOutputStream(connection.getOutputStream());
				
				dos.writeBytes(req);
				dos.flush();
				dos.close();
				
				BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
				StringBuffer sb=new StringBuffer();
				String line;
				while((line=br.readLine())!=null)
					sb.append(line);
				br.close();
				connection.disconnect();
				return new JSONObject(sb.toString());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			break;
		case 3:
			try 
			{
				String BOUNDARY ="ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC";

				URL url=new URL(toUrl);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
				
				OutputStream out = connection.getOutputStream();
				
				Map2Multi(content, BOUNDARY, out);
				
				out.write(("--" + BOUNDARY + "--\r\n").getBytes("UTF-8"));
				Log.e("Debug_sendMessage", BOUNDARY);
				out.flush();
				out.close();
				
				
				// read response
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				StringBuffer response = new StringBuffer();
				String line;
				while((line= reader.readLine())!=null)
					response.append(line);
				reader.close();
				connection.disconnect();
				Log.e("Debug_resp", response.toString());
				return new JSONObject(response.toString());

			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		JSONObject ret = new JSONObject();
		try 
		{
			ret.put("status", "false");
		} 
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public static String Map2XML(HashMap map)
	{
		StringBuffer sb=new StringBuffer();
		for(Object key:map.keySet())
		{
			try 
			{
				sb.append(key);
				sb.append('=');
				sb.append(URLEncoder.encode((String) map.get(key), "utf-8"));
				sb.append('&');
			} 
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public static void Map2Multi(HashMap map,String boundary,OutputStream out)
	{
		try
		{
			for(Object key:map.keySet())
			{
				if(map.get(key) instanceof File)
				{
					File file=(File)map.get(key);
					out.write(("--"+boundary+"\r\n").getBytes("UTF-8"));
					out.write(("Content-Disposition: form-data; name=\""+key+"\"; filename=\""+file.getName()+"\"\r\n").getBytes("UTF-8"));
					String type=file.getName().substring(file.getName().lastIndexOf("."),file.getName().length());
					if(type.equals(".jpg"))
						out.write(("Content-Type: image/jpeg\r\n\r\n").getBytes("UTF-8"));
					else if(type.equals(".png"))
						out.write(("Content-Type: image/png\r\n\r\n").getBytes("UTF-8"));
					
					InputStream fileIn = new FileInputStream(file);
					byte[] imageData = new byte[1024];
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					int length = 0;
					while ((length = fileIn.read(imageData)) != -1) {
						stream.write(imageData, 0, length);
					}
					fileIn.close();
					out.write(stream.toByteArray());
					out.write(("\r\n").getBytes("UTF-8"));
				}
				else if(map.get(key) instanceof File[])
				{
					File[] f=(File[])map.get(key);
					for(int j=0;j<f.length;j++)
					{
						File file=(File)f[j];
						out.write(("--"+boundary+"\r\n").getBytes("UTF-8"));
						out.write(("Content-Disposition: form-data; name=\""+key+"\"; filename=\""+file.getName()+"\"\r\n").getBytes("UTF-8"));
						String type=file.getName().substring(file.getName().lastIndexOf("."),file.getName().length());
						if(type.equals(".jpg"))
							out.write(("Content-Type: image/jpeg\r\n\r\n").getBytes("UTF-8"));
						else if(type.equals(".png"))
							out.write(("Content-Type: image/png\r\n\r\n").getBytes("UTF-8"));
						
						InputStream fileIn = new FileInputStream(file);
						byte[] imageData = new byte[1024];
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						int length = 0;
						while ((length = fileIn.read(imageData)) != -1) {
							stream.write(imageData, 0, length);
						}
						fileIn.close();
						out.write(stream.toByteArray());
						out.write(("\r\n").getBytes("UTF-8"));
					}
				}
				else if(map.get(key) instanceof String[])
				{
					String[] s=(String[])map.get(key);
					for(int i=0;i<s.length;i++)
					{
						String str=s[i];
						out.write(("--"+boundary+"\r\n").getBytes("UTF-8"));
						out.write(("Content-Disposition: form-data; name=\""+key.toString()+"\"\r\n\r\n").getBytes("UTF-8"));
						out.write((str+"\r\n").getBytes("UTF-8"));
					}
				}
				else
				{
					out.write(("--"+boundary+"\r\n").getBytes("UTF-8"));
					out.write(("Content-Disposition: form-data; name=\""+key.toString()+"\"\r\n\r\n").getBytes("UTF-8"));
					out.write((map.get(key).toString()+"\r\n").getBytes("UTF-8"));
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
