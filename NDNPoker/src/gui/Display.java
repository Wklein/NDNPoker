package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import net.named_data.jndn.encoding.EncodingException;
import poker.GameState;
import poker.LocalState;
import poker.Player;

public class Display {
	JFrame mainFrame;
	
	JPanel gameListPanel;
	JPanel buttonPanel;
	CardTable gamePanel;
	JPanel playerPanel;
	
	JButton findButton;
	JButton createButton;
	JButton startButton;
	
	public Display(){
		
	}
	
	public void init(){
		mainFrame = new JFrame("Poker");
		mainFrame.setLayout(new BorderLayout());
		
		gameListPanel = new JPanel();
		gameListPanel.setLayout(new BoxLayout(gameListPanel, BoxLayout.Y_AXIS));
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		gamePanel = new CardTable();
		playerPanel = new JPanel();
		playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
		
		
		mainFrame.add(gameListPanel, BorderLayout.EAST);
		mainFrame.add(buttonPanel, BorderLayout.SOUTH);
		mainFrame.add(playerPanel, BorderLayout.WEST);
		mainFrame.add(gamePanel, BorderLayout.CENTER);
	
		buttonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		findButton = new JButton("Find Games");
		findButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					LocalState.findGames(null);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			}
		});
		
		createButton = new JButton("Create Game");
		createButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalState.createGame("Test Game");
				buttonPanel.remove(createButton);
				buttonPanel.add(startButton);
				mainFrame.revalidate();
				
			}
		});
		
		startButton = new JButton("Start Game");
		startButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalState.startGame();
				
			}
			
		});
		
		buttonPanel.add(findButton);
		buttonPanel.add(createButton);
	
		mainFrame.setSize(600, 350);
		mainFrame.setVisible(true);
		mainFrame.revalidate();
	}
	
	public void repaint(){
		mainFrame.repaint();
	}
	
	public void listGame(GameState state){
		JPanel tmp = new JPanel();
		tmp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
		tmp.add(new JLabel(state.name));
		tmp.add(new JLabel(String.valueOf(state.id)));
		tmp.add(new JLabel(String.valueOf(state.currentPlayers.size())));
		JButton joinButton = new JButton("Join Game");
		joinButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
//					System.out.println("Sending join interest");
					LocalState.joinGame(state);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		tmp.add(joinButton);
		gameListPanel.add(tmp);
		mainFrame.revalidate();
	}
	
	public void displayGameState(GameState state){
		playerPanel.removeAll();
		for(Player p: state.currentPlayers){
//			System.out.println(p.name + " " + p.id);
			JPanel pPanel = new JPanel();
			pPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));
			pPanel.add(new JLabel("NAME : " + p.name));
			pPanel.add(new JLabel("ID : " + p.id));
			pPanel.add(new JLabel("CASH : " + p.currentCash));
			pPanel.add(new JLabel("BET : " + p.currentBet));
			playerPanel.add(pPanel);
		}
		mainFrame.revalidate();
	}
	
	public CardTable getCardTable(){
		return gamePanel;
	}
	
}
