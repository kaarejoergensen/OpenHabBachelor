package main;

import network.APIManager;
import network.NetworkErrorException;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import tui.TUI;

public class NorthQStandalone {
    public static void main(String[] args) {
        APIManager apiManager = new APIManager(new OkHttpClient());
        try {
            apiManager.authenticate("kaare1994@gmail.com", "Bachelor123");
            TUI tui = new TUI(apiManager);
            tui.start();
        } catch (NetworkErrorException | JSONException e) {
            System.out.println(e.getMessage());
        }
    }
}
