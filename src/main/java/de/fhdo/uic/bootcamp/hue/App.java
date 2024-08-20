package de.fhdo.uic.bootcamp.hue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.primitives.Floats;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Jonas Sorgalla (and Marcel Mitas ;))
 * Example application to demonstrate the Philips Hue System as an IoT device.
 */
public class App {

	// Fake IP address of the Hue Bridge. Please change it!
	static String HUE_ADDRESS = "http://192.168.4.84/api/";
	
	// Initial user ID required to access the Hue Bridge. Please change it!
	// Have a look here to change it: https://developers.meethue.com/develop/get-started-2/
	static String USER = "mL-H7OIf7wEncVofkUCsCng2jNJaJR404DSM4BUu";
	// The number of the light to control. Change it to the number of your light.
	static String LIGHT_NUMBER = "7";
	
	//Weather API for getting the current temperature
	final static String WEATHER_API = "http://api.openweathermap.org/data/2.5/weather";
	final static String CITY = "Dortmund";
	final static String APIKEY = "Take your own ;)";
	

	final static OkHttpClient CLIENT = new OkHttpClient();
	static boolean terminate = false;

	public static void main(String[] args) throws Exception {
		var app = new App();

		// Here, the java arguments are read in to configure the access to the Hue bridge and the lights.
		// Possible values:
		// 1. argument (args[0]): IP address of the Hue bridge
		// 2. argument (args[1]): Username for the Hue Bridge
		// 3. argument (args[2]): Light number (optional)
		if(args != null && args.length > 0) {
			System.out.println("Lese Argumente ein...");
			if(args.length >= 2) {
				//First argument is the IP address. Additionally, we check the IP address format.
				String ipaddress = args[0];
				if(!ipaddress.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")){
					System.out.println("Schade, das erste Argument ist keine IP-Adresse. Beende Programm.");
					System.exit(1);
				}
				else {
					HUE_ADDRESS = "http://"+ipaddress+"/api/";
				}
				USER = args[1];
				if(args.length >= 3) {
					System.out.println("Lese Nummer der Lampe ein...");
					LIGHT_NUMBER = args[2];
					if(!LIGHT_NUMBER.matches("[0-9]+")) {
						System.out.println("Schade, das dritte Argument ist keine Zahl. Starte also Lampe " + LIGHT_NUMBER + ".");
					}
					else {
						System.out.println("OK, starte Lampe" + LIGHT_NUMBER + ".");
					}
				}
				else {
					System.out.println("Alles startbereit. Die Lampe kenne ich nicht, also gehe ich von Lampe " + LIGHT_NUMBER + " aus.");
				}
			}
		}
		else {
			System.out.println("Keine Argumente gefunden. Starte mit Standardwerten.");
		}

		// Creating objects needed for the interaction with the user
		var scan = new Scanner(System.in);
		//Asking for a specific code to start the application
		System.out.println("Bevor ich dir zeige, was ich so kann, brauche ich den 4-stelligen Code von dir:");
		while(!terminate) {
			int input;
			try {
				input = scan.nextInt();		
				switch(input) {
				case 2874: {
					System.out.println("Das war korrekt! Jetzt geht's los...");
					app.sendColorCode();
					terminate = true;
					break;
				}
				default: {
					System.out.println("Leider ist der eingegebene Code falsch. Bitte versuche es erneut.");
					break;
				}
			}
			} catch (InputMismatchException ime) {
				System.out.println("Fehlerhafte Eingabe. Programm wird beendet.");
			}
		}
		scan.close();
	}

	/**
	 * Method to send a color code to the Philips Hue light
	 * @throws InterruptedException
	 */
	
	public void sendColorCode() throws InterruptedException {
		var lightState = HUE_ADDRESS+USER+"/lights/"+LIGHT_NUMBER+"/state"; // Die Adresse
		for(int i = 0; i <= 5; i++) {
			for (Integer color : getColorCode()) {
				List<Integer> rgb = colorCodeToRGB(color);
				int sat = rgb.size() > 3 ? rgb.get(3) : 254;
				int bri = rgb.size() > 4 ? rgb.get(4) : 254;

				changeColor(lightState, getRGBtoPhilipsHue(rgb.get(0), rgb.get(1), rgb.get(2)), sat, bri);
				Thread.sleep(1500);
			}
			changeState(lightState, false);
			Thread.sleep(5000);		
		}
	}

	/**
	 * Method to get the color code being sent to the Philips Hue light
	 * @return List of numbers representing the color code
	 */

	public List<Integer> getColorCode() {
		return Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9); //This shows all colors available in the color code. Feel free to change it.
	}

	public List<Integer> colorCodeToRGB(int colorCode) {
		List<Integer> result = new ArrayList<>();
		//Array pattern: [Red, Green, Blue, Saturation, Brightness] (two last values are optional)
		switch(colorCode) {
			case 0: { // Red
				result = Arrays.asList(255, 0, 0);
				break;
			}
			case 1: { // Orange
				result = Arrays.asList(255, 128, 0);
				break;
			}
			case 2: { // Magenta
				result = Arrays.asList(255, 0, 255);
				break;
			}
			case 3: { // Yellow
				result = Arrays.asList(255, 255, 0);
				break;
			}
			case 4: { // Green
				result = Arrays.asList(0, 255, 0);
				break;
			}
			case 5: { // Cyan / Turquoise
				result = Arrays.asList(0, 255, 255);
				break;
			}
			case 6: { // Blue
				result = Arrays.asList(0, 0, 255);
				break;
			}
			case 7: { // White
				result = Arrays.asList(255, 255, 255, 0, 24);
				break;
			}
			case 8: { // Pink
				result = Arrays.asList(255, 204, 230, 74, 254);
				break;
			}
			case 9: { // Purple
				result = Arrays.asList(102, 0, 204);
				break;
			}
			default: { // Black
				result = Arrays.asList(255, 255, 255, 0, 24);
				break;
			}
		}
		return result;
	}

	/**
	 * Method to change the state of the Philips Hue light
	 * @param url The URL of the light
	 * @param on The state of the light
	 * @return The response of the REST call
	 */

	public String changeState(String url, boolean on) {
		MediaType mediaType = MediaType.parse("application/json");

		RequestBody body;
		String content = "{\"on\":" + (on ? "true" : "false") + "}\n";
		body = RequestBody.create(content, mediaType);

		Request request = new Request.Builder()
			.url(url)
			.put(body)
			.addHeader("Content-Type", "application/json")
			.build();

		try {
			Response response = CLIENT.newCall(request).execute();
			return response.body().string();
		} catch (IOException e) {
			System.out.println("Hmm, da ist wohl was schief gelaufen und es ist meine Schuld... :/");
			e.printStackTrace();
			return null;
		}		
	}
	
	/**
	 * Method to 
	 * @param url
	 * @return
	 */

	public String alertHue(String url) {
		MediaType mediaType = MediaType.parse("application/json");
		
		// Here, the body of the REST call is set.
		// Have a look here for the parameters: http://www.burgestrand.se/hue-api/api/lights/
		JSONObject jsonObject = new JSONObject();
		try {
		    jsonObject.put("alert", "lselect");
		    jsonObject.put("hue", 54722);
		} catch (JSONException e) {
		    e.printStackTrace();
		}

		RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
		

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

	public String changeColorXY(String url, float x, float y, Integer... satBri) {
		MediaType mediaType = MediaType.parse("application/json");
		int sat = satBri.length > 0 ? satBri[0]: 254;
		int bri = satBri.length > 1 ? satBri[1]: 254;
		RequestBody body = RequestBody.create("{\"on\":true, \"sat\":" + sat + ", \"bri\":" + bri + ",\"xy\":["+x+", " + y + "]}\n",mediaType);

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

	public String changeColor(String url, int farbwert, Integer... satBri) {
		MediaType mediaType = MediaType.parse("application/json");
		int sat = satBri.length > 0 ? satBri[0]: 254;
		int bri = satBri.length > 1 ? satBri[1]: 254;
		RequestBody body = RequestBody.create("{\"on\":true, \"sat\":" + sat + ", \"bri\":" + bri + ",\"hue\":"+farbwert+"}\n", mediaType);

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

	/**
	 * Method to convert any RGB color to Philips Hue Color
	 * @param red The red value between 0 and 255
	 * @param green The green value between 0 and 255
	 * @param blue The blue value between 0 and 255
	 * @return 
	 */
	public static int getRGBtoPhilipsHue(float red, float green, float blue) {
		float min = red > green ? green : red;
		min = min < blue ? min : blue;
		float max = red > green ? red : green;
		max = max > blue ? max : blue;
		float delta = max - min;

		float hue = 0;
		if(delta < 0.00001) {
			return (int) hue;
		}

		if(max == 0.0) {
			return (int) hue;
		}
		if(red >= max) {
			hue = (green - blue) / delta;
		}
		else if(green >= max) {
			hue = 2.0f + (blue - red) / delta;
		}
		else {
			hue = 4.0f + (red - green) / delta;
		}
		hue *= 60.0;
	
		if( hue < 0.0 ) {
			hue += 360.0;
		}
		
		float huePhilips = (hue / 360) * 65353; // Convert the Hue value (0-360°) to a Philips Hue value (0-65353)

		return (int) huePhilips;
}

public static List<Float> getRGBtoXY(float red, float green, float blue) {
    // For the hue bulb the corners of the triangle are:
    // -Red: 0.675, 0.322
    // -Green: 0.4091, 0.518
    // -Blue: 0.167, 0.04
    double[] normalizedToOne = new double[3];
    normalizedToOne[0] = (red / 255);
    normalizedToOne[1] = (green / 255);
    normalizedToOne[2] = (blue / 255);
    float vividRed, vividGreen, vividBlue;

    // Make red more vivid
    if (normalizedToOne[0] > 0.04045) {
        vividRed = (float) Math.pow(
                (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        vividRed = (float) (normalizedToOne[0] / 12.92);
    }

    // Make green more vivid
    if (normalizedToOne[1] > 0.04045) {
        vividGreen = (float) Math.pow((normalizedToOne[1] + 0.055)
                / (1.0 + 0.055), 2.4);
    } else {
        vividGreen = (float) (normalizedToOne[1] / 12.92);
    }

    // Make blue more vivid
    if (normalizedToOne[2] > 0.04045) {
    	vividBlue = (float) Math.pow((normalizedToOne[2] + 0.055)
                / (1.0 + 0.055), 2.4);
    } else {
        vividBlue = (float) (normalizedToOne[2] / 12.92);
    }

    float X = (float) (vividRed * 0.649926 + vividGreen * 0.103455 + vividBlue * 0.197109);
    float Y = (float) (vividRed * 0.234327 + vividGreen * 0.743075 + vividBlue * 0.022598);
    float Z = (float) (vividRed * 0.0000000 + vividGreen * 0.053077 + vividBlue * 1.035763);

    float x = X / (X + Y + Z);
    float y = Y / (X + Y + Z);

    float[] xy = new float[2];
    xy[0] = x;
    xy[1] = y;
    List<Float> xyAsList = Floats.asList(xy);
    return xyAsList;
}
}
