package com.company;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.LinkedList;
import java.util.Queue;

public class AirportAgent extends Agent {
    /**
     * @param name - name of the airport
     * @param isClear - status of the runway of the airport [clear / not clear]
     * @param queue - queue of planes that wait for landing
     */
    private static final long serialVersionIUID = 1L;
    protected String name;
    protected boolean isClear = true;
    protected Queue<String> queue = new LinkedList<>();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null) {
            return;
        }
        // Sets the name of the airport
        this.name = (String) args[0];

        // Airport listener
        this.addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = this.myAgent.receive();
                if (msg != null) {
                    String airplaneName = msg.getSender().getLocalName();

                    int performative = msg.getPerformative();

                    // plane announced runway usage intention [takeoff / landing]
                    if (performative == ACLMessage.PROPOSE) {
                        if (isClear && (queue.isEmpty() || queue.peek().equals(airplaneName))) {
                            isClear = false;
                            queue.poll();
                            int rwy = Integer.parseInt(msg.getContent());
                            doWait(rwy);
                            isClear = true;
                            replyAccepted(msg); // Accept runway usage by airplane
                        } else {
                            System.out.println("Airplane with ID " + airplaneName + " waits to land at " + name);
                            if (!queue.contains(airplaneName)) {
                                queue.add(airplaneName); // Adding plane to landing queue
                            }
                            replyRejected(msg); // Reject runway usage by airplane
                        }
                    }
                }
            }
        });
    }

    private void replyAccepted(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        reply.setContent("[AcceptIntention] - " + this.name);
        this.send(reply);
    }

    private void replyRejected(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
        reply.setContent("[RejectIntention - " + this.name);
        this.send(reply);
    }
}
