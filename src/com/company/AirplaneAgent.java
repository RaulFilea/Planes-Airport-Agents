package com.company;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.List;
import java.util.Random;

import static com.company.Main.getRoute;

public class AirplaneAgent extends Agent{
    /**
     * @param ID - flight number
     * @param route - list of airports the plane should visit
     * @param speed - constant for a certain plane to multiply with the distance between two airports
     * @param rwy - the time needed for runway operations (landing and takeoff)
     * @param service - the time needed for ground operations
     * @param goaround - the time needed for a goaround
     *
     * @param current - the current airport of the plane
     * @param inAir - status of the plane [in air / on ground]
     */
    private static final long serialVersionIUID = 1L;
    protected String ID;
    protected List<String> route;
    protected int speed;
    protected int rwy;
    protected int service;
    protected int goaround;
    protected int fuel;

    protected String current;
    protected boolean inAir = false;

    @Override
    protected void setup() {

        Object[] args = getArguments();
        if (args == null) {
            return;
        }

        // Creating AirplaneAgents like this to avoid using another class
        this.ID = (String) args[0];
        this.route = (List<String>) args[1];
        this.speed = (int) args[2];
        this.rwy = (int) args[3];
        this.service = (int) args[4];
        this.goaround = (int) args[5];
        this.fuel = (int) args[6];

        current = route.get(0);

        this.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Interpretation of airport messages
                ACLMessage msg = this.myAgent.receive();
                if (msg != null) {
                    // Ask airport for clearance
                    int performative = msg.getPerformative();

                    System.out.println(ID + " fuel left: " + fuel);

                    // If the runway is clear
                    if (performative == ACLMessage.ACCEPT_PROPOSAL) {
                        // If plane is on ground, takeoff and fly to destination
                        if (!inAir) {
                            System.out.println(ID + " took of from " + current);
                            fuel = fuel - rwy - speed;
                            doWait(rwy);
                            inAir = true;
                            doWait(speed);
                            // If plane is in the air, land
                        } else {
                            // If missed landing, go around
                            if (new Random().nextBoolean()) {
                                fuel = fuel - rwy - goaround;
                            } else { // If landing done, go to gate, use no fuel
                                System.out.println(ID + " landed at " + current);
                                fuel -= rwy;
                                doWait(rwy);
                                inAir = false;
                                doWait(service);
                                try {
                                    current = route.get(route.indexOf(current) + 1);
                                } catch (IndexOutOfBoundsException e) {
                                    System.out.println(ID + " reached destination.");
                                    if (fuel > 250) { // If final airport en route and enough fuel, get new route
                                        route = getRoute();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        this.addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                if (inAir && fuel <= 0){ // If plane ran out of fuel in the air, crash
                    System.out.println("Plane " + ID + " crashed");
                    myAgent.doDelete();
                    return;
                }

                if (!inAir && fuel <= 250) { // If not enough fuel to takeoff, but plane didn't finish route
                    System.out.println("Plane " + ID + " stopped on ground at " + current);
                    myAgent.doDelete();
                    return;
                }

                if (inAir) { // While waiting for runway clearance, use fuel [on ground / in air]
                    fuel -= 100;
                }

                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                AID receiverAID = new AID(current, AID.ISLOCALNAME); // in the same container
                msg.addReceiver(receiverAID);
                msg.setContent(String.valueOf(rwy));
                myAgent.send(msg);
            }
        });
    }
}
