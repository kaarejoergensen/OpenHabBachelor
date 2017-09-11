package main;

import network.APIManager;
import network.NetworkErrorException;
import okhttp3.OkHttpClient;
import org.json.JSONException;
import tui.TUI;

public class NorthQStandalone {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: NorthQStandalone 'user' 'password'");
            return;
        }
        APIManager apiManager = new APIManager(new OkHttpClient());
        try {
            apiManager.authenticate(args[0], args[1]);
            TUI tui = new TUI(apiManager);
            tui.start();
        } catch (NetworkErrorException | JSONException e) {
            System.out.println(e.getMessage());
        }
    }
}