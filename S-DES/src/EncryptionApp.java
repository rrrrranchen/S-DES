import javax.swing.*;

public class EncryptionApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("加密解密应用");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JTabbedPane tabbedPane = new JTabbedPane();

        EncryptionPanel encryptionPanel = new EncryptionPanel();
        DecryptionPanel decryptionPanel = new DecryptionPanel();
        BruteForcePanel bruteForcePanel = new BruteForcePanel();

        tabbedPane.addTab("加密", encryptionPanel);
        tabbedPane.addTab("解密", decryptionPanel);
        tabbedPane.addTab("暴力破解", bruteForcePanel);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }
}
