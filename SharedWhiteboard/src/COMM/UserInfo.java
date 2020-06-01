package COMM;
import java.io.Serializable;
import java.util.UUID;

public class UserInfo implements Serializable {
    UUID userId;
    String userIpAddress;
    int port;
    String userName;
    Boolean isHost;

    public UserInfo(String userIpAddress, int port, String userName, Boolean isHost) {
        this.userId = UUID.randomUUID();
        this.userIpAddress = userIpAddress;
        this.port = port;
        this.userName = userName;
        this.isHost = isHost;
    }

    public Boolean getIsHost() {
        return isHost;
    }

    public void setIsHost(Boolean isHost) {
        this.isHost = isHost;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserIpAddress() {
        return userIpAddress;
    }

    public void setUserIpAddress(String userIpAddress) {
        this.userIpAddress = userIpAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
