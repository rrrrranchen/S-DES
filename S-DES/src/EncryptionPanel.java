import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EncryptionPanel extends JPanel {
    private JTextField plaintextField;
    private JPasswordField keyField;
    private JTextField encryptedField;
    private ButtonGroup styleGroup;
    private JRadioButton binaryRadioButton;
    private JRadioButton asciiRadioButton;
    private JButton encryptButton;

    public EncryptionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 明文输入框
        plaintextField = new JTextField(20);
        add(new JLabel("明文:"));
        add(plaintextField);

        // 密钥输入框
        keyField = new JPasswordField(20);
        JButton togglePasswordField = new JButton("显示/隐藏密钥");
        togglePasswordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char c = keyField.getEchoChar();
                if (c == '*' || c == '\u2022') {
                    keyField.setEchoChar((char) 0);
                    togglePasswordField.setText("隐藏密钥");
                } else {
                    keyField.setEchoChar('•');
                    togglePasswordField.setText("显示密钥");
                }
            }
        });
        add(new JLabel("密钥:"));
        add(new JPanel(new FlowLayout(), false) {{
            add(keyField);
            add(togglePasswordField);
        }});

        // 加密输出框
        encryptedField = new JTextField(20);
        encryptedField.setEditable(false);
        add(new JLabel("密文:"));
        add(encryptedField);

        // 单选框
        styleGroup = new ButtonGroup();
        binaryRadioButton = new JRadioButton("二进制", true);
        asciiRadioButton = new JRadioButton("ASCII码");
        styleGroup.add(binaryRadioButton);
        styleGroup.add(asciiRadioButton);
        add(binaryRadioButton);
        add(asciiRadioButton);

        // 加密按钮
        encryptButton = new JButton("加密");
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String plaintext = plaintextField.getText();
                String key = new String(keyField.getPassword());
                String style = binaryRadioButton.isSelected() ? "1" : "2";
                if(style=="1"){
                    String encrypted = SDES.encrypt(plaintext, key);
                    encryptedField.setText(encrypted);
                }else{
                    String []encrypted;
                    String []decrypted = new String[plaintext.length()];
                    encrypted=SDES.charToBinaryStringArray(plaintext);
                    for(int i=0;i<plaintext.length();i++){
                        encrypted[i]=SDES.encrypt(encrypted[i],key);
                        decrypted[i]=SDES.decrypt(encrypted[i],key);
                    }
                    String encrypted1=SDES.binaryStringArrayToString(encrypted);
                    encryptedField.setText(encrypted1);
                }
            }
        });
        add(encryptButton);
    }

}