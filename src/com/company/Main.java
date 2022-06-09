package com.company;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Application that manages planes and airports. Planes randomly get a list of destinations (route) and start from the
 * first airport in the list. Airports each have only one runway. Each action costs time (and fuel), so if a plane can't
 * land at an airport, it will eventually crash. Everything is random, so there should not be 2 consecutive identical scenarios.
 */

public class Main {
    // List of all possible airports. Created like this for AirportAgent creation and for route creation.
    static List<String> airports = Arrays.asList("LRTR", "LRAR", "LRCB", "LROD", "LROP", "LRIA", "LRCK", "LRBS", "LRSB");

    // Get random element from a provided list
    public static String getRandomElement(List<String> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    // Route creation. Minimum of airports per route.
    public static List<String> getRoute() {
        List<String> route = new ArrayList<>();
        String airport;
        route.add(getRandomElement(airports));
        if (new Random().nextBoolean()) {
            while(true) {
                airport = getRandomElement(airports);
                if (!airport.equals(route.get(route.size()-1))) {
                    break;
                }
            }
        }
        return route;
    }

    public static void main(String[] args) {
        // Helper strings
        String helper1 = "com.company.AirportAgent";
        String helper2 = "com.company.AirplaneAgent";

        jade.core.Runtime rt = jade.core.Runtime.instance();
        Profile myProfile = new ProfileImpl();
        AgentContainer myContainer = rt.createMainContainer(myProfile);
        try {
            myContainer.createNewAgent("rma", "jade.tools.rma.rma", null).start();

            // Create all airport agents
            for (String airport : airports) {
                myContainer.createNewAgent(airport, helper1, new Object[]{airport}).start();
            }

            // Create new airplane
            myContainer.createNewAgent("YR-5011", helper2, new Object[]{"YR-5011", getRoute(), 100, 50, 200, 500, 10000}).start();
            myContainer.createNewAgent("YR-5012", helper2, new Object[]{"YR-5012", getRoute(), 100, 60, 230, 500, 10000}).start();
            myContainer.createNewAgent("YR-7777", helper2, new Object[]{"YR-7777", getRoute(),150, 55, 200, 500, 10000}).start();
            myContainer.createNewAgent("YR-7000", helper2, new Object[]{"YR-7000", getRoute(), 160, 64, 230, 500, 10000}).start();
            myContainer.createNewAgent("YR-ZCL", helper2, new Object[]{"YR-ZCL", getRoute(), 130, 50, 200, 500, 10000}).start();
            myContainer.createNewAgent("D-AZUF", helper2, new Object[]{"D-AZUF", getRoute(), 500, 160, 1230, 2500, 100000}).start();
            myContainer.createNewAgent("LFT-SH", helper2, new Object[]{"LFT-SH", getRoute(), 1500, 150, 1200, 2500, 100000}).start();
            myContainer.createNewAgent("FE-H", helper2, new Object[]{"FE-H", getRoute(), 1000, 160, 1530, 2500, 100000}).start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}