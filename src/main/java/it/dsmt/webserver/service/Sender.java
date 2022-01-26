package it.dsmt.webserver.service;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import java.util.concurrent.CountDownLatch;

public class Sender implements Runnable {
    private final Client1 c1;
    private final CountDownLatch latch;
    private final String msg;

    public Sender(Client1 c1, CountDownLatch latch, String msg) {
        this.c1 = c1;
        this.latch = latch;
        this.msg = msg;
    }
    @Override
    public void run() {
        if (c1 != null) {
            try {
                c1.sendMessage(c1.getRoom(), this.msg);
            } catch (OtpErlangDecodeException e) {
                e.printStackTrace();
            } catch (OtpErlangExit e) {
                e.printStackTrace();
            }
        }
        latch.countDown();
    }


}
