package it.dsmt.webserver.service;

import com.ericsson.otp.erlang.*;
import it.dsmt.webserver.model.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Client2 {

    private String nodeId = "client1";
    private String cookie = "abcde";
    private OtpNode otpNode;
    private String room;
    private String username = "eirik";
    private String servername = "chatserver@LAPTOPAUR7RSLG";

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    {
        try {
            otpNode = new OtpNode(nodeId, cookie);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private OtpMbox otpMbox = otpNode.createMbox("hello");

    public List<String> getRooms() throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("newuser");
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[]{
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[]{
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername, msg_gen);
        OtpErlangTuple reply = (OtpErlangTuple) this.otpMbox.receive();
        OtpErlangTuple roomTuple = (OtpErlangTuple) reply.elementAt(1);
        OtpErlangList chatRooms = (OtpErlangList) roomTuple.elementAt(1);
        List<String> rooms = new ArrayList<>();

        if (chatRooms.elementAt(0) == null) {
            System.out.println("No available rooms");
        } else {
            chatRooms.forEach(System.out::println);
            chatRooms.forEach(x -> rooms.add(String.valueOf(x).substring(1, String.valueOf(x).length() - 1)));
        }
        return rooms;
    }

    public boolean addRoom(String room) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("newroom");
        OtpErlangString otpRoom = new OtpErlangString(room);
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType, otpRoom});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[]{
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[]{
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername, msg_gen);
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
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[]{
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[]{
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername, msg_gen);
        OtpErlangTuple reply = (OtpErlangTuple) this.otpMbox.receive();
        OtpErlangTuple userTuple = (OtpErlangTuple) reply.elementAt(1);
        OtpErlangList usernames = (OtpErlangList) userTuple.elementAt(1);
        List<String> users = new ArrayList<>();

        if (!(usernames.elementAt(0) == null)) {
            System.out.println("Users already in room: ");
            usernames.forEach(System.out::println);
            usernames.forEach(x -> users.add(String.valueOf(x).substring(1, String.valueOf(x).length() - 1)));
            return users;
        } else {
            System.out.println("You are the first person in the room");
            usernames.forEach(x -> users.add(String.valueOf(x).substring(1, String.valueOf(x).length() - 1)));
            return users;
        }
    }

    public void sendMessage(String room, String msg) throws OtpErlangDecodeException, OtpErlangExit {
        OtpErlangAtom msgType = new OtpErlangAtom("send");
        OtpErlangString otpRoom = new OtpErlangString(room);
        OtpErlangString otpMsg = new OtpErlangString(msg);
        OtpErlangString otpUsername = new OtpErlangString(this.username);
        OtpErlangTuple outMsg = new OtpErlangTuple(new OtpErlangObject[]{msgType, otpMsg, otpRoom, otpUsername});
        OtpErlangTuple from = new OtpErlangTuple(new OtpErlangObject[]{
                this.otpMbox.self(), this.otpNode.createRef()
        });
        OtpErlangObject msg_gen = new OtpErlangTuple(new OtpErlangObject[]{
                new OtpErlangAtom("$gen_call"), from, outMsg});
        this.otpMbox.send("chat_server", this.servername, msg_gen);
    }

    public List<String> receive() throws OtpErlangDecodeException, OtpErlangExit {
        List<String> replyList = new ArrayList<>();
        OtpErlangTuple reply = (OtpErlangTuple) this.otpMbox.receive();
        OtpErlangList replies = (OtpErlangList) reply.elementAt(1);
        replies.forEach(x -> replyList.add(String.valueOf(x).substring(1, String.valueOf(x).length() - 1)));
        try {
            OtpErlangTuple replyMsg = (OtpErlangTuple) reply.elementAt(1);
            OtpErlangAtom ok = new OtpErlangAtom("ok");
            if (!replyMsg.elementAt(0).equals(ok)) {
                System.out.println("Something went wrong");
            }
            return null;
        } catch (Exception e) {
            System.out.println("Message from, " + reply.elementAt(1) + ": " + reply.elementAt(0));
            return replyList;
        }

    }
}
