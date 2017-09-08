package main;

import network.APIManager;
import okhttp3.OkHttpClient;
import tui.TUI;

public class NorthQStandalone {
    public static void main(String[] args) {
        APIManager apiManager = new APIManager(new OkHttpClient());
        if (apiManager.authenticate("kaare1994@gmail.com", "Bachelor123")) {
            TUI tui = new TUI(apiManager);
            tui.start();
//            System.out.println(apiManager.getGatewayStatus("0000003703"));
        }
    }
}
