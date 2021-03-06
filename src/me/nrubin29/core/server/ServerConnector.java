package me.nrubin29.core.server;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import javax.swing.JOptionPane;

import me.nrubin29.core.server.packet.handler.PacketHandlerManager;
import me.nrubin29.core.server.packet.packet.Packet;
import me.nrubin29.core.server.packet.packet.PacketJoin;

public class ServerConnector {
	
	private ServerConnector() { }
	
	private static ServerConnector instance = new ServerConnector();
	
	public static ServerConnector getInstance() {
		return instance;
	}

	private Socket socket;
    private Thread listener;
    private BufferedReader reader;
    private PrintWriter writer;

    private boolean usingServer = false;

    public boolean initConnection(String ip, int port) {
        try {
            socket = new Socket(ip, port);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            
            writer.println(Session.getInstance().getLocalPlayer().getName());

            listener = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            String packet = reader.readLine();

                            System.out.println("Got packet: " + packet);
                            
                            if (packet == null) System.exit(0);
                            
                            PacketHandlerManager.getInstance().handle(packet);
                        }
                        catch (EOFException e) { System.exit(0); }
                        catch (Exception e) { e.printStackTrace(); }
                    }
                }
            });

            listener.start();
            
            sendPacket(new PacketJoin(Session.getInstance().getLocalPlayer().getName()));

            usingServer = true;
            return true;
        }
        catch (ConnectException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to server at given port.");
            return false;
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An unexpected error has occurred (" + e + ").");
            e.printStackTrace();
            return false;
        }
    }
    
    public void sendPacket(Packet packet) {
        if (!usingServer) return;

    	try {
            System.out.println("Writing " + packet.asString());
    		writer.println(packet.asString());
    	}
    	catch (Exception e) { e.printStackTrace(); }
    }
}