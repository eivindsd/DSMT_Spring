package it.dsmt.webserver.service;

import com.ericsson.otp.erlang.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@Component
public class Client1 {

    private String nodeId = "springclient";
    private String cookie = "abcde";
    private String mBox = "mBox";
    private String room;
    private OtpNode otpNode;
    private String username;
    private String servername = "server@172.18.0.8";
    private int x = 0;
    private OtpMbox otpMbox;
    public ExecutorService executorService;
    public CountDownLatch latch = new CountDownLatch(1);
    public List<String> messages = new ArrayList<>();

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void incrementX() {
        this.x++;
    }

    public void setNode() throws IOException {
        this.nodeId = this.nodeId + this.x;
        this.otpNode = new OtpNode(this.nodeId, this.cookie);

    }

    public void setmBox() {
        this.otpMbox = this.otpNode.createMbox();
    }

    public List<String> getRooms() throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("newuser");
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[] {
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[] {
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername , msg_gen );
        OtpErlangTuple reply = (OtpErlangTuple) this.otpMbox.receive();
        OtpErlangTuple roomTuple = (OtpErlangTuple) reply.elementAt(1);
        OtpErlangList chatRooms = (OtpErlangList) roomTuple.elementAt(1);
        List<String> rooms = new ArrayList<>();

        if (chatRooms.elementAt(0) == null) {
            System.out.println("No available rooms");
        }
        else {
            chatRooms.forEach(System.out::println);
            chatRooms.forEach(x -> rooms.add(String.valueOf(x).substring(1, String.valueOf(x).length() -1)));
        }
        return rooms;
    }

    public boolean addRoom(String room) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("newroom");
        OtpErlangString otpRoom = new OtpErlangString(room);
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType, otpRoom});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[] {
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[] {
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername , msg_gen );
        OtpErlangObject reply = this.otpMbox.receive();
        if (reply == null) {
            System.out.println("Failed to make room");
            return false;
        }
        return true;
    }

    public List<String> joinRoom(String room) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("connect");
        OtpErlangString otpRoom = new OtpErlangString(room);
        OtpErlangString otpUsername = new OtpErlangString(this.username);
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{this.otpMbox.self(), msgType, otpRoom, otpUsername});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[] {
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[] {
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername , msg_gen );
        OtpErlangTuple reply = (OtpErlangTuple) this.otpMbox.receive();
        OtpErlangTuple userTuple = (OtpErlangTuple) reply.elementAt(1);
        OtpErlangList usernames = (OtpErlangList) userTuple.elementAt(1);
        List<String> users = new ArrayList<>();

        if (!(usernames.elementAt(0) == null)) {
            System.out.println("Users already in room: ");
            usernames.forEach(System.out::println);
            usernames.forEach(x -> users.add(String.valueOf(x).substring(1, String.valueOf(x).length() -1)));
            return users;
        }
        else {
            System.out.println("You are the first person in the room");
            usernames.forEach(x -> users.add(String.valueOf(x).substring(1, String.valueOf(x).length() -1)));
            return users;
        }
    }

    public void sendMessage(String room, String msg) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("send");
        OtpErlangString otpRoom = new OtpErlangString(room);
        OtpErlangString otpMsg= new OtpErlangString(msg);
        OtpErlangString otpUsername = new OtpErlangString(this.username);
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType, otpMsg, otpRoom, otpUsername});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[] {
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[] {
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername , msg_gen );
    }

    public void receive() throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangTuple reply = (OtpErlangTuple) this.otpMbox.receive();
        try {
            OtpErlangTuple replyMsg = (OtpErlangTuple) reply.elementAt(1);
            OtpErlangAtom ok = new OtpErlangAtom("ok");
            if (!replyMsg.elementAt(0).equals(ok)) {
                System.out.println("Something went wrong");
            }

        } catch (Exception e) {
            this.getMessages().add(reply.elementAt(0).toString());
        }
    }

    public void exit() throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("exit");
        OtpErlangString otpUsername = new OtpErlangString(this.username);
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType, otpUsername});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[] {
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[] {
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername , msg_gen);
        this.otpMbox.receive();
    }


}
