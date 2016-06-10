package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class CardTable extends JPanel {
	private List<Integer> cardList;
	
	private List<Integer> myCards;
	
	private void drawOpenCards(Graphics2D g){
		if(cardList == null) return;
		for(int i = 0; i < 3 && i < cardList.size(); i++){
			drawCard(g, new Point(70 + 50 * i, 30), cardList.get(i));
		}
		for(int i = 3; i < 5 && i < cardList.size(); i++){
			drawCard(g, new Point(-55 + 50 * i, 100), cardList.get(i));
		}
	}
	
	private void drawMyCards(Graphics2D g){
		if(myCards == null) return;
		for(int i = 0; i < 2 && i < myCards.size(); i++){
			drawCard(g, new Point(95 + 50 * i, 200), myCards.get(i));
		}
	}
	
	private void drawCard(Graphics2D g, Point upperLeft, int card){
		if(card % 2 == 0)
			g.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{3, 5}, 0.0f));
		else
			g.setStroke(new BasicStroke(3));
		
		if(card % 4 > 1)
			g.setColor(Color.RED);
		else
			g.setColor(Color.BLACK);
		
		RoundRectangle2D.Double rect = new RoundRectangle2D.Double(upperLeft.getX(), upperLeft.getY(), 40, 60, 4, 4);
		
		String cardValue = "";
		card = card / 4;
		if(card < 10 && card > 0)
			cardValue = cardValue += (card + 1);
		else if(card == 0)
			cardValue = "A";
		else if(card == 10)
			cardValue = "J";
		else if(card == 11)
			cardValue = "Q";
		else if(card == 12)
			cardValue = "K";
		
		g.draw(rect);
		g.drawString(cardValue, upperLeft.x + 5, upperLeft.y + 15);
	}
	
	@Override
	public void paintComponent(Graphics g){
		drawOpenCards((Graphics2D)g);
		drawMyCards((Graphics2D)g);
	}

	public List<Integer> getCardList() {
		return cardList;
	}

	public void setCardList(List<Integer> cardList) {
		this.cardList = cardList;
	}
	
	public void addOpenCard(int card){
		if(cardList == null) cardList = new ArrayList<Integer>();
		cardList.add(card);
	}
	public void addMyCard(int card){
		if(myCards == null) myCards = new ArrayList<Integer>();
		myCards.add(card);
	}
	
	public static void main(String args[]){
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		CardTable table = new CardTable();
		table.setBorder(new LineBorder(Color.black, 5));
		ArrayList<Integer> cards = new ArrayList<Integer>();
		cards.add(0);
		cards.add(1);
		cards.add(2);
		cards.add(3);
		cards.add(4);
		
		frame.add(table, BorderLayout.CENTER);
		
		frame.setSize(600,  400);
		frame.setVisible(true);
		
		table.cardList = cards;
		frame.revalidate();
		frame.repaint();
	}

}
