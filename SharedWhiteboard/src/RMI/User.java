package RMI;

import COMM.UserInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface User extends Remote {
    public void loadImage (byte[] bytes) throws RemoteException;
    public void loadMessage (List<String> messages) throws RemoteException;
    public void loadUserList (List<UserInfo> users) throws RemoteException;
    public void loadNotify (String notify) throws RemoteException;
}
