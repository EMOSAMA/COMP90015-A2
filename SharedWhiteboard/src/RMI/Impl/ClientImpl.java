package RMI.Impl;

import GUI.Whiteboard.WhiteboardGUI;
import RMI.Client;
import STRUCTURE.UserInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ClientImpl extends UnicastRemoteObject implements Client {
    private static final long serialVersionUID = 1L;
    UserInfo userInfo;
    UserInfo hostInfo;
    protected WhiteboardGUI GUI;

    public ClientImpl(UserInfo hostInfo, UserInfo userInfo) throws RemoteException {
        this.hostInfo = hostInfo;
        this.userInfo = userInfo;
        this.GUI = new WhiteboardGUI(hostInfo, userInfo);
    }
    @Override
    public void loadImage (byte[] bytes) throws RemoteException {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            BufferedImage image = ImageIO.read(in);
            GUI.getCanvas().loadImage(image);
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
        GUI.getHostList().setListData(new String[0]);
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
    public void loadNotify (String notify) throws RemoteException{
        if(notify.equals("kick out")) {
            JOptionPane.showMessageDialog(null,"You have been kicked out by host!");
            System.exit(1);
        }
        else if(notify.equals("host exit")) {
            JOptionPane.showMessageDialog(null,"The host is closed!");
            System.exit(1);
        }
    }
}
