import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class MainGUI {
	static ChatApp app;
	static String queue = "omnianachat";
    MainGUI mainGUI;
    JFrame newFrame = new JFrame("Omniana Chat v0.1");
    JButton sendMessage;
    static JTextField messageBox = new JTextField(30);
    static JTextArea chatBox = new JTextArea();
    JTextField usernameChooser;
    JFrame preFrame;

    public static void main(String[] args) throws InterruptedException, URISyntaxException, StorageException {
        try {
        	app = new ChatApp();
        	app.createQueue(queue);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainGUI mainGUI = new MainGUI();
        mainGUI.preDisplay();
        while(true) {
        	
        	if(app.isChangePresent(queue)) {
        		chatBox.setText("");
                app.limitLength(queue);
            	for(CloudQueueMessage x: app.readMessages(queue)) {
            		try {
    					chatBox.append(x.getMessageContentAsString());
    				} catch (StorageException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
            	}
            	
            }
        	Thread.sleep(1000);
        }
    }


    public void preDisplay() {
        newFrame.setVisible(false);
        preFrame = new JFrame("Choose your username");
        usernameChooser = new JTextField();
        JLabel chooseUsernameLabel = new JLabel("Pick a username:");
        JButton enterServer = new JButton("Enter Chat Server");
        JPanel prePanel = new JPanel(new GridBagLayout());

        GridBagConstraints preRight = new GridBagConstraints();
        preRight.anchor = GridBagConstraints.EAST;
        GridBagConstraints preLeft = new GridBagConstraints();
        preLeft.anchor = GridBagConstraints.WEST;
        preRight.weightx = 2.0;
        preRight.fill = GridBagConstraints.HORIZONTAL;
        preRight.gridwidth = GridBagConstraints.REMAINDER;

        prePanel.add(chooseUsernameLabel, preLeft);
        prePanel.add(usernameChooser, preRight);
        preFrame.add(BorderLayout.CENTER, prePanel);
        preFrame.add(BorderLayout.SOUTH, enterServer);
        preFrame.setVisible(true);
        preFrame.setSize(800, 800);

        enterServer.addActionListener(new enterServerButtonListener());
    }

    public void display() {
        newFrame.setVisible(true);
        JPanel southPanel = new JPanel();
        newFrame.add(BorderLayout.SOUTH, southPanel);
        southPanel.setBackground(Color.BLUE);
        southPanel.setLayout(new GridBagLayout());

        sendMessage = new JButton("Send Message");
        chatBox.setEditable(false);
        newFrame.add(new JScrollPane(chatBox), BorderLayout.CENTER);

        chatBox.setLineWrap(true);

        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.WEST;
        GridBagConstraints right = new GridBagConstraints();
        right.anchor = GridBagConstraints.EAST;
        right.weightx = 2.0;

        southPanel.add(messageBox, left);
        southPanel.add(sendMessage, right);

        chatBox.setFont(new Font("Serif", Font.PLAIN, 24));
        messageBox.addActionListener(new sendMessageButtonListener());
        sendMessage.addActionListener(new sendMessageButtonListener());
        newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newFrame.setSize(800, 800);
    }

    class sendMessageButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
        	
            if (messageBox.getText().length() < 1) {
            	
            } else if (messageBox.getText().equals(".clear")) {
                chatBox.setText("Cleared all messages\n");
                messageBox.setText("");
            } else {
            
            	String message = "<" + username + ">:  " + messageBox.getText() + "\n";
            	messageBox.setText("");
                app.sendMessage(queue, message);
                messageBox.setText("");
                chatBox.append(message);
            }
        }
    }

    String username;

    class enterServerButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            username = usernameChooser.getText();
            if (username.length() < 1) {System.out.println("No!"); }
            else {
            preFrame.setVisible(false);
            display();
            }
        }

    }
}
