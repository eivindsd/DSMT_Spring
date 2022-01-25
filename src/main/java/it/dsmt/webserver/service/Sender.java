package it.dsmt.webserver.service;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Sender implements Runnable {
    private final Client1 c1;
    private final Client2 c2;
    private final CountDownLatch latch;
    private final String msg;

    public Sender(Client1 c1, Client2 c2, CountDownLatch latch, String msg) {
        this.c1 = c1;
        this.c2 = c2;
        this.latch = latch;
        this.msg = msg;
    }
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        if (c1 != null) {
            try {
                c1.sendMessage(c1.getRoom(), this.msg);
            } catch (OtpErlangDecodeException e) {
                e.printStackTrace();
            } catch (OtpErlangExit e) {
                e.printStackTrace();
            }
        }
        else if (c2 != null) {
            System.out.println("Write a message to " + c2.getRoom());
            String msg = sc.nextLine();
            try {
                c2.sendMessage(c2.getRoom(), msg);
            } catch (OtpErlangDecodeException e) {
                e.printStackTrace();
            } catch (OtpErlangExit e) {
                e.printStackTrace();
            }
        }
        /*
        else if (c3 != null) {
            System.out.println("Write a message to " + c3.getRoom());
            String msg = sc.nextLine();
            try {
                c3.sendMessage(c3.getRoom(), msg);
            } catch (OtpErlangDecodeException e) {
                e.printStackTrace();
            } catch (OtpErlangExit e) {
                e.printStackTrace();
            }
        }
        latch.countDown();
        */
    }


}
