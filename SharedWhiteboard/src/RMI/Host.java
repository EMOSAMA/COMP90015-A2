package RMI;

import COMM.UserInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Host extends Remote,User {
    public void syncImage (byte[] bytes) throws RemoteException;
    public void undo () throws RemoteException;
    public void redo () throws RemoteException;

    public void syncMessage () throws RemoteException;
    public void sendMessage (String message) throws RemoteException;

    public void syncUserList () throws RemoteException;
    public void syncNotification (byte[] bytes) throws RemoteException;

    public UserInfo userJoin (UserInfo userInfo) throws RemoteException;
    public void userQuit (UserInfo userInfo) throws RemoteException;
    public void kickUser (String username) throws RemoteException;
    public void hostExit () throws RemoteException;
}
