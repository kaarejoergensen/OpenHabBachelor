package tui;

import models.*;
import network.APIManager;

import java.util.List;
import java.util.Scanner;

public class TUI {
    private APIManager apiManager;
    private Gateway chosenGateway;
    private Scanner scanner;

    public TUI(APIManager apiManager) {
        this.apiManager = apiManager;
        scanner = new Scanner(System.in);
    }

    public void start() {
        boolean houseChosen = chooseHouse();
        if (houseChosen) {
            mainMenu();
        }
    }

    private boolean chooseHouse() {
        List<House> houseList = apiManager.getHouses();
        if (houseList.isEmpty()) {
            System.out.println("No active houses! Exiting.");
            return false;
        }
        boolean houseSelectionDone = false;
        while (!houseSelectionDone) {
            System.out.println("Select house:");
            for (int i = 0; i < houseList.size(); i++) {
                System.out.println(i + 1 + ": " + houseList.get(i).getName());
            }
            String answer = scanner.nextLine();
            try {
                int selection = Integer.parseInt(answer);
                if ((selection - 1) > houseList.size()) {
                    System.out.println("Please valid input number!");
                } else {
                    House chosenHouse = houseList.get(selection - 1);
                    List<Gateway> gateways = apiManager.getGateways(chosenHouse.getId());
                    if (!gateways.isEmpty()) {
                        chosenGateway = gateways.get(0);
                        houseSelectionDone = true;
                    } else {
                        System.out.println("No gateways installed, please select different house!");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Please valid input number!");
            }
        }
        return true;
    }

    private void mainMenu() {
        String answer = "";

        while (!answer.equals("0")) {
            System.out.println("1: See list of switches\n2: See list of rooms\n3: See list of binary sensors\n0: Exit");
            answer = scanner.nextLine();
            switch (answer) {
                case "1":
                    switchMenu();
                    break;
                case "2":
                    roomTemperatureMenu();
                    break;
                case "3":
                    binarySensorsMenu();
                    break;
            }
        }
    }

    private void switchMenu() {
        List<BinarySwitch> binarySwitches = apiManager.getSwitches(chosenGateway.getSerial());
        System.out.println("Choose switch to switch power state");
        for (int i = 0; i < binarySwitches.size(); i++) {
            System.out.println(i + 1 + ": " + binarySwitches.get(i).getName() + ", turned " + (binarySwitches.get(i).isTurnedOn() ? "on" : "off"));
        }
        System.out.println("0: Back");
        Boolean done = false;
        while (!done) {
            String answer = scanner.nextLine();
            try {
                int selection = Integer.parseInt(answer);
                if (selection == 0) {
                    return;
                }
                if (selection - 1 > binarySwitches.size()) {
                    System.out.println("Please enter valid number!");
                } else {
                    if (apiManager.changeSwitchState(chosenGateway.getSerial(), binarySwitches.get(selection - 1))) {
                        System.out.println("State changed!");
                    }
                    switchMenu();
                    done = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid number!");
            }
        }
    }

    private void roomTemperatureMenu() {
        List<Room> rooms = apiManager.getRooms(chosenGateway.getSerial());
        System.out.println("Choose room to change temperature");
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println(i + 1 + ": " + rooms.get(i).getName() +
                    ", temperature " + rooms.get(i).getTemperature());
        }
        System.out.println("0: Back");
        Boolean done = false;
        while (!done) {
            String answer = scanner.nextLine();
            try {
                int selection = Integer.parseInt(answer);
                if (selection == 0) {
                    return;
                }
                if (selection - 1 > rooms.size()) {
                    System.out.println("Please enter valid number!");
                } else {
                    Room chosenRoom = rooms.get(selection - 1);
                    System.out.println("Please enter value between 5 and 28 (current temperature: " + chosenRoom.getTemperature() + ")");
                    while (!done) {
                        answer = scanner.nextLine();
                        try {
                            double newTemperature = Double.parseDouble(answer);
                            if (newTemperature >= 5 && newTemperature <= 28) {
                                if (apiManager.setRoomTemperature(chosenGateway.getSerial(), chosenRoom, newTemperature)) {
                                    System.out.println("Temperature changed!");
                                }
                                roomTemperatureMenu();
                                done = true;
                            } else {
                                System.out.println("Please enter number between 5 and 28!");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter valid number!");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid number!");
            }
        }
    }

    private void binarySensorsMenu() {
        List<BinarySensor> binarySensors = apiManager.getBinarySensors(chosenGateway.getSerial());
        System.out.println("Choose sensor to see more information");
        for (int i = 0; i < binarySensors.size(); i++) {
            System.out.println(i + 1 + ": " + binarySensors.get(i).getName() +
                    ", " + (binarySensors.get(i).isArmed() ? "armed" : "not armed"));
        }
        System.out.println("0: Back");
        Boolean done = false;
        while (!done) {
            String answer = scanner.nextLine();
            try {
                int selection = Integer.parseInt(answer);
                if (selection == 0) {
                    return;
                }
                if (selection - 1 > binarySensors.size()) {
                    System.out.println("Please enter valid number!");
                } else {
                    System.out.println(binarySensors.get(selection - 1).toString());
                    binarySensorsMenu();
                    done = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid number!");
            }
        }
    }
}
