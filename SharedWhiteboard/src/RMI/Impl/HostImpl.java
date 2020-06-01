package RMI.Impl;

import GUI.Whiteboard.WhiteboardGUI;
import RMI.Client;
import RMI.Host;
import COMM.UserInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class HostImpl extends UnicastRemoteObject implements Host {
    UserInfo hostInfo;
    List<UserInfo> userList = new ArrayList<UserInfo>();
    List<String> messages = new ArrayList<String>();
    protected String serviceName;
    protected String clientServiceName;
    protected WhiteboardGUI GUI;
    LinkedList<BufferedImage> images = new LinkedList<BufferedImage>();
    int imageIndex;
    SimpleDateFormat sdf;

    public List<UserInfo> getUserList() {
        return userList;
    }

    public void setUserList(List<UserInfo> userList) {
        this.userList = userList;
    }

    public HostImpl(UserInfo hostInfo) throws RemoteException {
        sdf = new SimpleDateFormat();
        sdf.applyPattern("HH:mm:ss a");
        this.hostInfo = hostInfo;
        this.userList.add(hostInfo);
        this.GUI = new WhiteboardGUI(hostInfo,hostInfo);
        images.addLast(this.GUI.getCanvas().save());
        imageIndex = images.size() - 1;
    }

    public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
        // The images must be the same size.
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
            return false;
        }

        int width  = imgA.getWidth();
        int height = imgA.getHeight();

        // Loop over every pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Compare the pixels for equality.
                if (imgA.getRGB(x, y) != imgB.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void syncImage (byte[] bytes) throws RemoteException{
        if (bytes == null){
            try {
                BufferedImage image = this.GUI.getCanvas().getImage();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image,"png", out);
                bytes = out.toByteArray();
                this.syncImage(bytes);
            } catch (Exception e) {

            }
        } else {
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage image = ImageIO.read(in);
                if (!compareImages(image,images.get(this.imageIndex))) {
                    while (!(images.size() == imageIndex+1)) {
                        images.removeLast();
                    }
                    if(images.size()<20){
                        images.addLast(image);
                    }else {
                        images.removeFirst();
                        images.addLast(image);
                    }
                    imageIndex = images.size()-1;
                }
            }catch (Exception e) {

            }
            for (UserInfo userInfo : userList) {
                try {
                    if(userInfo.getIsHost()) {
                        Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                        Host host = (Host) registry.lookup("hostServer");
                        host.loadImage(bytes);
                    } else {
                        Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                        Client client = (Client) registry.lookup( userInfo.getUserId().toString());
                        client.loadImage(bytes);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void undo () throws RemoteException {
        if(imageIndex > 0) {
            BufferedImage image = images.get(imageIndex-1);
            imageIndex -= 1;
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image,"png", out);
                byte[] bytes = out.toByteArray();
                syncImage(bytes);
            }catch (Exception e) {

            }
        }
    }

    @Override
    public void redo () throws  RemoteException {
        if(imageIndex < images.size()) {
            BufferedImage image = images.get(imageIndex+1);
            imageIndex += 1;
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image,"png", out);
                byte[] bytes = out.toByteArray();
                syncImage(bytes);
            }catch (Exception e) {

            }
        }
    }

    @Override
    public void syncMessage () throws RemoteException{
        for (UserInfo userInfo : userList) {
            try {
                if(userInfo.getIsHost()){
                    Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                    Host host = (Host) registry.lookup("hostServer");
                    host.loadMessage(this.messages);
                }else {
                    Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                    Client client = (Client) registry.lookup(userInfo.getUserId().toString());
                    client.loadMessage(this.messages);
                }
            }catch (Exception e){

            }
        }
    }

    @Override
    public void sendMessage (String message) throws RemoteException{
        messages.add(message);
        this.syncMessage();
    }

    @Override
    public void syncUserList () throws RemoteException{
        for (UserInfo userInfo : this.userList) {
            try {
                if(userInfo.getIsHost()) {
                    Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                    Host host = (Host) registry.lookup("hostServer");
                    host.loadUserList(this.userList);
                } else {
                    Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                    Client client = (Client) registry.lookup( userInfo.getUserId().toString());
                    client.loadUserList(this.userList);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    @Override
    public void syncNotification (byte[] bytes) throws RemoteException{
        return;
    }

    @Override
    public void loadImage (byte[] bytes) throws RemoteException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(in);
            GUI.getCanvas().loadImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadMessage (List<String> messages) throws RemoteException {
        GUI.getMessageBox().setText(null);
        for (String message : messages){
            GUI.getMessageBox().append(message);
            GUI.getMessageBox().setCaretPosition(GUI.getMessageBox().getText().length());
        }
    }

    @Override
    public void loadUserList (List<UserInfo> users) throws RemoteException {
        List<String> hostsList = new ArrayList<String>();
        List<String> clientsList = new ArrayList<String>();
        for (UserInfo user : users) {
            if(user.getIsHost()) {
                hostsList.add(user.getUserName());
            }else {
                clientsList.add(user.getUserName());
            }
        }
        String[] hostsArr=new String[hostsList.size()];
        String[] clientsArr=new String[clientsList.size()];

        int index = 0;
        for (String hostName : hostsList) {
            hostsArr[index] = hostName;
            index += 1;
        }
        index = 0;
        for (String clientName : clientsList) {
            clientsArr[index] = clientName;
            index += 1;
        }
        this.GUI.getHostList().setListData(hostsArr);
        this.GUI.getUserList().setListData(clientsArr);
    }

    @Override
    public UserInfo userJoin (UserInfo userInfo) throws RemoteException{
        if (hostInfo.getUserName().equals(userInfo.getUserName())){
            return userInfo;
        }

        for (UserInfo userInfoItem : userList) {
            if (userInfoItem.getUserName().equals(userInfo.getUserName())){
                return userInfo;
            }
        }

        int flag = JOptionPane.showConfirmDialog(null,userInfo.getUserName() + " want to join");
        if(flag == 0){
            userList.add(userInfo);
            messages.add("----- "+" NOTIFICATION: "+"WELCOME "+userInfo.getUserName()+" JOIN OUR WHITEBOARD ROOM! "+sdf.format(new Date())+"\n");
            return this.hostInfo;
        } else {
            return null;
        }
    }

    @Override
    public void userQuit (UserInfo userInfo) throws RemoteException{
        for (UserInfo item : userList){
            if(item.getUserId().equals(userInfo.getUserId())) {
                userList.remove(item);
                break;
            }
        }
        try {
            Registry registry = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(), hostInfo.getPort());
            Host host = (Host) registry.lookup( "hostServer");
            host.syncUserList();

            String time = sdf.format(new Date());
            messages.add("----- "+" NOTIFICATION: "+userInfo.getUserName()+" QUIT FROM THIS WHITEBOARD ROOM! "+sdf.format(new Date())+"\n");
            host.syncMessage();
        }catch (Exception e) {
        }
    }

    @Override
    public void kickUser (String username) throws RemoteException{
        for(UserInfo userInfo : userList) {
            if (userInfo.getUserName().equals(username)) {
                try {
                    userList.remove(userInfo);
                    messages.add("----- "+" NOTIFICATION: "+userInfo.getUserName()+" HAS BEEN KICKED OUT FROM THIS WHITEBOARD ROOM! "+sdf.format(new Date())+"\n");
                    Registry registry = LocateRegistry.getRegistry(hostInfo.getUserIpAddress(),hostInfo.getPort());
                    Host host = (Host) registry.lookup( "hostServer");
                    host.syncUserList();
                    host.syncMessage();

                    registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                    Client client = (Client) registry.lookup( userInfo.getUserId().toString());
                    new Thread(){
                        public void run() {
                            try{
                                client.loadNotify("kick out");
                            } catch(Exception e){System.out.println(e);}
                        }
                    }.start();
                }catch (Exception e){

                }
                return;
            }
        }
        JOptionPane.showMessageDialog(null,"This user is not found!");
    }

    @Override
    public void loadNotify (String notify) throws RemoteException{
    }

    public void hostExit () throws RemoteException {
        for (UserInfo userInfo : userList) {
            try {
                Registry registry = LocateRegistry.getRegistry(userInfo.getUserIpAddress(),userInfo.getPort());
                Client client = (Client) registry.lookup( userInfo.getUserId().toString());
                client.loadNotify("host exit");
            }catch (Exception e){

            }
        }
    }
}
