package it.dsmt.webserver.controller;

import com.ericsson.otp.erlang.OtpErlangDecodeException;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangList;
import it.dsmt.webserver.model.Message;
import it.dsmt.webserver.model.Room;
import it.dsmt.webserver.model.User;
import it.dsmt.webserver.service.Client1;
import it.dsmt.webserver.service.Receiver;
import it.dsmt.webserver.service.Sender;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ClientController {

    @Autowired
    Client1 client1;

    @GetMapping("/rooms")
    public ResponseEntity<List<String>> getRooms() {
        try {
            client1.setExecutorService(Executors.newFixedThreadPool(2));
            client1.incrementX();
            client1.setNode();
            client1.setmBox();
            List<String> rooms = new ArrayList<>(client1.getRooms());
            if(rooms.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(rooms, HttpStatus.OK);
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        try {
            client1.setUsername(user.getUser());
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> addRoom(@RequestBody Room room) {
        try {
            if(!client1.joinRoom(room.getRoom()).isEmpty()) {
                return new ResponseEntity<>(room, HttpStatus.OK);
            }
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/joinroom/{room}")
    public ResponseEntity<List<String>> joinRoom(@PathVariable("room") String room) {
        try {
            List<String> users = client1.joinRoom(room);
            client1.setRoom(room);
            Receiver receiver = new Receiver(client1);
            client1.getExecutorService().execute(receiver);
            System.out.println(users);
            if (users.size() == 0) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sendmessage")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        Sender sender = new Sender(client1,null, client1.getLatch(), message.getMessage());
        client1.getExecutorService().execute(sender);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getmessages")
    public ResponseEntity<List<Message>> getMessages() {
        try {
            return new ResponseEntity<>(client1.getMessages(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/exitroom")
    public ResponseEntity<String> exitRoom() {
        try {
            client1.exit();
            return new ResponseEntity<>("Exited room", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
