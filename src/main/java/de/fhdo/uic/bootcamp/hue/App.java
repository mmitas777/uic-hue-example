package de.fhdo.uic.bootcamp.hue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Jonas Sorgalla
 * Example appication to demonstrate the Philips Hue System as an IoT device.
 */
public class App {

	// Im Folgenden finden sich Variablen, die für die Verbindung zur Hue Bridge benötigt werden
	//
	// Die IP Adresse der Bridge, hier eine fiktive IP eingetragen. Muss geändert werden.
	final static String HUE_ADDRESS = "http://10.0.102.26/api/";
	// Die User ID muss initial angelegt werden. Ist der Nutzer einmal vorhanden, kann er von mehreren Personen genutzt werden.
	// Neue Nutzer können durch das betätigen des "Buttons" auf der Bridge + ein POST Call auf die Bridge angelegt werden
	// Eine Anleitung dazu findet sich auf https://developers.meethue.com/develop/get-started-2/
	final static String USER = "7UvAqtykWojX4fjAnmFPJttdmi8N1erMFBMX-wCT";
	// Jede an die Bridge angeschlossene Leuchte verfügt über eine eigene lokale ID, diese kann auch über Menüpunkt 5 ausgelesen werden
	final static String LIGHT_NUMBER = "8";
	
	//Variablen für die Wetter API von openweathermap.org
	final static String WEATHER_API = "http://api.openweathermap.org/data/2.5/weather";
	final static String CITY = "Dortmund";
	final static String APIKEY = "92647b8f3a3d7920948e20505514b882";
	

	final static OkHttpClient CLIENT = new OkHttpClient();
	static boolean terminate = false;

	public static void main(String[] args) throws Exception {
		var app = new App();


		// Erzeugen von später benötigten Objekten zur Interaktion mit der Konsole
		var scan = new Scanner(System.in);
		//
		System.out.println("Willkommen zur Hue TestApp im Schüler Bootcamp des User Innovation Center.");
		System.out.println("Bitte geben Sie die entsprechende Zahl für die Aktion ein und bestätigen Sie mit Enter.");
		System.out.println("(1) Leuchte ausschalten");
		System.out.println("(2) Leuchte einschalten");
		System.out.println("(3) Farbe verändern");
		System.out.println("(4) Aus-Ein-Aus-Ein-Aus blinken");
		System.out.println("(5) Liste aller anschlossenen Leuchten ausgeben");	
		System.out.println("(6) Alarm anschalten");
		System.out.println("(7) Wettermodus");
		System.out.println("(8) Programm beenden");
		while(!terminate) {
			System.out.print("Neue Eingabe 1 bis 8:");
			int input;
			try {
				input = scan.nextInt();		
				switch(input) {
				// Hier könnt ihr durch Hinzufügen von neuen "cases" einfach neue Szenarien hinzufügen.
				// bspw. könntet ihr versuchen einen neuen case 7 hinzuzufügen, indem (1) die Leuchte angemacht,
				// (2) die Farbe geändert wird, und anschließend (3) die Leuchte wieder ausgeschaltet wird.
				case 1: {
					//Licht ausschalten - GET Request mit FALSE
					var lightState = HUE_ADDRESS+USER+"/lights/"+LIGHT_NUMBER+"/state";
					System.out.println("Ausschalten von Leuchte "+LIGHT_NUMBER+" wird ausgeführt:\n"+lightState);
					System.out.println("Erhaltene Antwort:\n"+app.changeState(lightState, false));
					break;
				}
				case 2: {
					//Licht einschalten - GET Request mit TRUE
					var lightState = HUE_ADDRESS+USER+"/lights/"+LIGHT_NUMBER+"/state";
					System.out.println("Ausschalten von Leuchte "+LIGHT_NUMBER+" wird ausgeführt:\n"+lightState);
					System.out.println("Erhaltene Antwort:\n"+app.changeState(lightState, true));
					break;
				}
				case 3: {
					//Philips Hue erlaubt einen Farbwert von 0 bis 65353 - einfach mal ausprobieren welche Zahl welche Farbe ergibt (HSB Farbmodell)
					var changeColor = HUE_ADDRESS+USER+"/lights/"+LIGHT_NUMBER+"/state";
					System.out.println("Farbänderung für Leuchte "+LIGHT_NUMBER+" wird ausgeführt:\n"+changeColor);
					System.out.println("Auf welchen Wert soll die Farbe verändert werden? Eingaben von 0 bis 65353 möglich.");
					System.out.print("Farbwert: ");
					var farbwert = scan.nextInt();	
					System.out.println(app.changeColor(changeColor, farbwert));
					break;
				}
				case 4: {
					//Licht ausschalten - GET Request mit FALSE
					var lightState = HUE_ADDRESS+USER+"/lights/"+LIGHT_NUMBER+"/state";
					System.out.println("Zwiemaliges Blinken für Leuchte "+LIGHT_NUMBER+" wird ausgeführt:\n"+lightState);
					System.out.println("Erhaltene Antwort:\n"+app.changeState(lightState, false));
					// sleep() nimmt Millisekunden entgeben; 1000 Millisekunden = 1 Sekunde
					Thread.sleep(1000);
					System.out.println("Erhaltene Antwort:\n"+app.changeState(lightState, true));
					Thread.sleep(1000);
					System.out.println("Erhaltene Antwort:\n"+app.changeState(lightState, false));
					Thread.sleep(1000);
					System.out.println("Erhaltene Antwort:\n"+app.changeState(lightState, true));
					Thread.sleep(1000);
					System.out.println("Erhaltene Antwort:\n"+app.changeState(lightState, false));
					break;
				}
				case 5: {
					var allLights = HUE_ADDRESS+USER+"/lights";
					System.out.println("Alle Lichter werden abgerufen:\n"+allLights);
					System.out.println("\nAntwort:\n"+app.getRequest(allLights));
					break;
				}	
				case 6: {
					var alert = HUE_ADDRESS+USER+"/lights/"+LIGHT_NUMBER+"/state";
					System.out.println("Alarm für "+LIGHT_NUMBER+" wird ausgeführt:\n"+alert);
					System.out.println("Erhaltene Antwort:\n"+app.alertHue(alert));
					break;
				}
				case 7: {
					System.out.println("Erhaltene Antwort:\n"+app.getTemperature());
					break;
				}
				case 8: {
					terminate = true;
					break;
				}
				default:
					System.out.println("Leider ist etwas mit der Eingabe schief gelaufen. Zulässig sind nur Zahlen von 1 bis 5."
							+ "\nIhre Eingabe war:"+input);
				}
			} catch (InputMismatchException ime) {
				System.out.println("Fehlerhafte Eingabe. Programm wird beendet.");
			}
		}
	}

	public String getRequest(String url) {
		Request request = new Request.Builder().url(url).get().build();
		try {
			Response response = CLIENT.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			System.out.println("Leider ist bei der Verbindung ein Fehler aufgetreten... :-/");
			e.printStackTrace();
			return null;
		}		
	}
	

	public String changeState(String url, boolean on) {
		MediaType mediaType = MediaType.parse("application/json");

		RequestBody body;	
		if(on) {
			// Schrägstriche werden für das sogenannte Escapen der Anführungszeichen benötigt!
			body = RequestBody.create(mediaType, "{\"on\":true}\n");
		} else {
			body = RequestBody.create(mediaType, "{\"on\":false}\n");
		}

		Request request = new Request.Builder()
				.url(url)
				.put(body)
				.addHeader("Content-Type", "application/json")
				.build();

		try {
			Response response = CLIENT.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			System.out.println("Leider ist bei der Verbindung ein Fehler aufgetreten... :/");
			e.printStackTrace();
			return null;
		}		
	}
	
	public String alertHue(String url) {
		MediaType mediaType = MediaType.parse("application/json");
		
		// Hier wird der Body des REST Calls definiert
		// Parameter siehe: http://www.burgestrand.se/hue-api/api/lights/
		JSONObject jsonObject = new JSONObject();
		try {
		    jsonObject.put("alert", "lselect");
		    jsonObject.put("hue", 54722);
		} catch (JSONException e) {
		    e.printStackTrace();
		}

		RequestBody body = RequestBody.create(mediaType, jsonObject.toString());
		

		Request request = new Request.Builder()
				.url(url)
				.put(body)
				.addHeader("Content-Type", "application/json")
				.build();

		try {
			Response response = CLIENT.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			System.out.println("Leider ist bei der Verbindung ein Fehler aufgetreten... :/");
			e.printStackTrace();
			return null;
		}		
	}
	
	public String getTemperature() {
		String url = WEATHER_API+"?q=" + CITY + "&appid=" + APIKEY + "&units=metric";

		Request request = new Request.Builder()
				.url(url)
				.addHeader("Content-Type", "application/json")
				.build();

		try {
			Response response = CLIENT.newCall(request).execute();
			String jsonData = response.body().string();
			JSONObject allWeatherInformation = new JSONObject(jsonData);
			JSONObject mainWeatherParameters = (JSONObject) allWeatherInformation.get("main");
			BigDecimal temp = (BigDecimal) mainWeatherParameters.get("temp");
			return "Die aktuelle Temperatur für " + CITY + " beträgt " + temp.toString() + " Celcius";
		} catch (IOException e) {
			System.out.println("Leider ist bei der Verbindung ein Fehler aufgetreten... :/");
			e.printStackTrace();
			return null;
		}		
	}

	public String changeColor(String url, int farbwert) {
		MediaType mediaType = MediaType.parse("application/json");

		RequestBody body = RequestBody.create(mediaType, "{\"on\":true, \"sat\":254, \"bri\":254,\"hue\":"+farbwert+"}\n");

		Request request = new Request.Builder()
				.url(url)
				.put(body)
				.addHeader("Content-Type", "application/json")
				.build();

		try {
			Response response = CLIENT.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			System.out.println("Leider ist bei der Verbindung ein Fehler aufgetreten... :/");
			e.printStackTrace();
			return null;
		}	
	}
}
