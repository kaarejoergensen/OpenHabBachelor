package main;

import models.BinarySensor;
import models.BinarySwitch;
import models.Gateway;
import models.House;
import network.QStickBridge;
import exceptions.APIException;
import tui.TUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NorthQStandalone {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: NorthQStandalone 'user' 'password'");
            return;
        }
        try {
            QStickBridge qStickBridge = new QStickBridge(args[0], args[1]);
            TUI tui = new TUI(qStickBridge);
            tui.start();
        } catch (APIException | IOException e) {
            System.out.println(e.getMessage());
        }
    }
}