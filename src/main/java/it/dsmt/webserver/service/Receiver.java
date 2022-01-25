package it.dsmt.webserver.service;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;

public class Receiver implements Runnable {

    private final Client1 c1;
    private final Client2 c2;

    public Receiver(Client1 c1, Client2 c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    @Override
    public void run() {
        while(true) {
            if (c1 != null) {
                try {
                    c1.receive();
                } catch (OtpErlangDecodeException e) {
                    e.printStackTrace();
                } catch (OtpErlangExit e) {
                    e.printStackTrace();
                }
            }
            else if (c2 != null) {
                try {
                    c2.receive();
                } catch (OtpErlangDecodeException e) {
                    e.printStackTrace();
                } catch (OtpErlangExit e) {
                    e.printStackTrace();
                }
            }
            /*
            else if (c3 != null) {
                try {
                    c3.receive();
                } catch (OtpErlangDecodeException e) {
                    e.printStackTrace();
                } catch (OtpErlangExit e) {
                    e.printStackTrace();
                }
            }

             */
        }

    }


}
