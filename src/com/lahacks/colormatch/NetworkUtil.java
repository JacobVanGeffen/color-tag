package com.lahacks.colormatch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.JsonReader;
import android.util.Log;

public class NetworkUtil {
	
	private static String postSite = "http://colortag.kywu.org/game/start",
			postHighScoreSite = "http://colortag.kywu.org/player/submit/",
			listSite = "http://colortag.kywu.org/game/list", 
			gameSite = "http://colortag.kywu.org/game/view/", //replace gameID w/ actual ID
			disconnectSite = "http://colortag.kywu.org/disconnect";
	
	public static interface FinishLoadingService{
		public void onEnd();
	}
	
	public static void disconnect(){
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(disconnectSite);
		try {
			httpclient.execute(httppost);
		} catch (ClientProtocolException e) {} catch (IOException e) {}
	}
	
	public static void getGame(final String uid, final FinishLoadingService service) throws IOException, InterruptedException {
		try{
			new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						HttpClient httpclient = new DefaultHttpClient();
						HttpGet httppost = new HttpGet(gameSite+uid);
						HttpResponse response = null;
						response = httpclient.execute(httppost);
						Log.wtf("Response", ""+response.getStatusLine().getStatusCode());
						InputStream is = null;
						if (response.getStatusLine().getStatusCode() < 400)
							is = response.getEntity().getContent();
						else return;
						JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
						reader.beginObject();
						while(reader.hasNext()){
							String name = reader.nextName(), item = "";
							try{
								item = reader.nextString();
							}catch(Exception e){reader.nextBoolean();}
							if(name.equals("color"))
								CameraActivity.color = (int)Long.parseLong("ff"+item, 16);
							else if(name.equals("length"))
								CameraActivity.length = Integer.parseInt(item);
							else if(name.equals("hiscore")){
								CameraActivity.globalHighScore = Double.parseDouble(item);
								Log.wtf("hgih", CameraActivity.globalHighScore+"");
							}
						}
						Log.wtf("Color", Long.toString(CameraActivity.color, 16));
						reader.close();
					}catch(Exception e){
						Log.wtf("ERROR", e.toString());
					}
					finally{service.onEnd();}
				}
			}).start();
		}catch(Exception e){}
	}
	
	public static void postData(final Game game){
		try {
			Thread t = new Thread(new Runnable() {
				public void run() {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(postSite);
					try {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						for (Map.Entry<String, String> e : game.post().entrySet())
							nameValuePairs.add(new BasicNameValuePair(e.getKey(), e.getValue()));
						Log.wtf("Pairs", game.post()+" "+game.color);
						httppost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs));
						HttpResponse response = httpclient.execute(httppost);
						InputStream is = null;
						if (response.getStatusLine().getStatusCode() < 400)
							is = response.getEntity().getContent();
						else return;
						JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
						reader.beginObject();
						reader.nextName(); CameraActivity.expTime = Long.parseLong(reader.nextString());
						reader.nextName(); CameraActivity.gameID = reader.nextString();
						reader.close();
					} catch (ClientProtocolException e) {
					} catch (IOException e) {}
				}
			});
			t.start();
			t.join();
		} catch (InterruptedException e) {
			Log.wtf("Post Error", e + "");
		}
	
	}
	
	public static void postData(final String uid, final double score){
		try {
			Thread t = new Thread(new Runnable() {
				public void run() {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(postHighScoreSite+uid);
					try {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("score", score+""));
						httppost.setEntity(new UrlEncodedFormEntity(
								nameValuePairs));
						HttpResponse response = httpclient.execute(httppost);
						Log.wtf("Response", ""+response.getStatusLine().getStatusCode());
					} catch (ClientProtocolException e) {
					} catch (IOException e) {}
				}
			});
			t.start();
			t.join();
		} catch (InterruptedException e) {
			Log.wtf("Post Error", e + "");
		}
	
	}
	
	@SuppressWarnings("finally")
	public static Map<String, String> getListData(final FinishLoadingService service){
		final Map<String, String> ret = new HashMap<String, String>();
		try {
			Thread t = new Thread(new Runnable() {
				public void run() {
					HttpClient httpclient = new DefaultHttpClient();
					HttpGet httppost = new HttpGet(listSite);
					try {
						HttpResponse response = httpclient.execute(httppost);
						InputStream is = null;
						if (response.getStatusLine().getStatusCode() < 400)
							is = response.getEntity().getContent();
						else
							return;
						JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
						reader.setLenient(true);
						reader.beginObject();
						reader.nextName();
						reader.beginObject();
						String id="", gName="";
						while(reader.hasNext()){
							String name = reader.nextName(), item = reader.nextString();
							if(name.equals("id"))
								id = item;
							else if(name.equals("name")){
								gName = item;
								ret.put(id, gName);
							}else if(name.equals("expiration")){
								reader.endObject();
								reader.nextName();
								try{
									reader.beginObject();
								}catch(Exception e){
									break;
								}
							}
						}
						//end stuff?
						reader.close();
						service.onEnd();
					} catch (ClientProtocolException e) {
						Log.wtf("Client Error", e.toString());
					} catch (IOException e) {
						Log.wtf("IO Error", e.toString());
					}
				}
			});
			t.start();
			t.join();
		} catch (InterruptedException e) {
			Log.wtf("Post Error", e + "");
		}finally{
			return ret;
		}
	}
	
	public static class Game{
		@SuppressWarnings("unused")
		private String name, uid;
		private int color, length;
		public Game(){};
		public Game(String name, String uid, int color, int length){
			this.name = name;
			this.uid = uid;
			this.color = color;
			this.length = length;
		}
		public Map<String, String> post(){
			Map<String, String> ret = new HashMap<String, String>();
			ret.put("name", name);
			ret.put("color", Integer.toString(color&0xffffff, 16));
			ret.put("length", length+"");
			return ret;
		}
	}
	
}
