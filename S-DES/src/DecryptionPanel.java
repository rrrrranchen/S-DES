import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DecryptionPanel extends JPanel {
    private JTextField ciphertextField;
    private JPasswordField keyField;
    private JTextField decryptedField;
    private ButtonGroup styleGroup;
    private JRadioButton binaryRadioButton;
    private JRadioButton asciiRadioButton;
    private JButton decryptButton;

    public DecryptionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 密文输入框
        ciphertextField = new JTextField(20);
        add(new JLabel("密文:"));
        add(ciphertextField);

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

        // 解密输出框
        decryptedField = new JTextField(20);
        decryptedField.setEditable(false);
        add(new JLabel("明文:"));
        add(decryptedField);

        // 单选框
        styleGroup = new ButtonGroup();
        binaryRadioButton = new JRadioButton("二进制", true);
        asciiRadioButton = new JRadioButton("ASCII码");
        styleGroup.add(binaryRadioButton);
        styleGroup.add(asciiRadioButton);
        add(binaryRadioButton);
        add(asciiRadioButton);

        // 解密按钮
        decryptButton = new JButton("解密");
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ciphertext = ciphertextField.getText();
                String key = new String(keyField.getPassword());
                String style = binaryRadioButton.isSelected() ? "1" : "2";
                if(style=="1") {
                    String decrypted = SDES.decrypt(ciphertext, key);
                    decryptedField.setText(decrypted);
                }else{
                    String []encrypted;
                    String []decrypted = new String[ciphertext.length()];
                    encrypted=SDES.charToBinaryStringArray(ciphertext);
                    for(int i=0;i<ciphertext.length();i++){
                        encrypted[i]=SDES.encrypt(encrypted[i],key);
                        decrypted[i]=SDES.decrypt(encrypted[i],key);
                    }
                    String decrypted1=SDES.binaryStringArrayToString(decrypted);
                    decryptedField.setText(decrypted1);
                }
            }
        });
        add(decryptButton);
    }

}