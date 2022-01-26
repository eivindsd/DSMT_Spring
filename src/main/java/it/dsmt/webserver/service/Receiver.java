package it.dsmt.webserver.service;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class Receiver implements Runnable {

    private final Client1 c1;
    private boolean running = true;

    public Receiver(Client1 c1) {
        this.c1 = c1;
    }

    @Override
    public void run() {
        OtpErlangAtom exit = new OtpErlangAtom("exit");
        while(running) {
            try {
                OtpErlangTuple reply = c1.receive();
                if (reply.elementAt(0).equals(exit)) {
                    this.running = false;
                }
            } catch (OtpErlangDecodeException e) {
                e.printStackTrace();
            } catch (OtpErlangExit e) {
                e.printStackTrace();
            }
        }
    }
}
