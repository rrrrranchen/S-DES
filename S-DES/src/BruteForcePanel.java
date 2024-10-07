import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BruteForcePanel extends JPanel {
    private JTextArea ciphertextsArea;
    private JTextArea plaintextsArea;
    private JButton bruteForceButton;
    private JTextArea keysArea;
    private JTextField timeField;

    public BruteForcePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 密文输入区域
        ciphertextsArea = new JTextArea(5, 20);
        add(new JLabel("密文:"));
        add(new JScrollPane(ciphertextsArea));

        // 明文输入区域
        plaintextsArea = new JTextArea(5, 20);
        add(new JLabel("明文:"));
        add(new JScrollPane(plaintextsArea));

        // 暴力破解按钮
        bruteForceButton = new JButton("开始暴力破解");
        bruteForceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] ciphertexts = ciphertextsArea.getText().split("\n");
                String[] plaintexts = plaintextsArea.getText().split("\n");
                String[] keys = SDES.bruteForceDecrypt(ciphertexts, plaintexts);
                keysArea.setText("");
                for (String key : keys) {
                    if (key != null) {
                        keysArea.append(key + "\n");
                    }
                }
                timeField.setText("破解时间: " + System.currentTimeMillis() + " 毫秒");
            }
        });
        add(bruteForceButton);

        // 密钥显示区域
        keysArea = new JTextArea(10, 20);
        keysArea.setEditable(false);
        add(new JLabel("找到的密钥:"));
        add(new JScrollPane(keysArea));

        // 破解时间显示
        timeField = new JTextField(20);
        timeField.setEditable(false);
        add(new JLabel("破解时间:"));
        add(timeField);
    }

}
