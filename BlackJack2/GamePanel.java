
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener {
    private final int SCREEN_HEIGHT = 600;
    private final int SCREEN_WIDTH = 600;

    private int playAgain = 0; //1=play again, 2=stop playing, 0=user has not chosen yet
    private int hitAgain = 0;  //1=hit again, 2=next phase, 0=user has not chosen yet
    private boolean introPhase = true;
    private boolean skipIntro = false;
    //private boolean dealPhase = false; //In case I need a separate phase for a dealing hand animation or smthn
    private boolean hitPhase = false;
    private boolean resultsPhase = false;

    private Deck d = new Deck();
    private Player player;
    private Player dealer;
    private boolean cardsDealt=false;
    private int gamesWon = 0;
    private int gamesLost = 0;
    private int gamesTied = 0;

    private final int DELAY = 100;
    Timer timer = new Timer(DELAY,this);
    
    GamePanel(){
        this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        d.loadDeck();
        d.shuffleDeck();
        player = new Player();
        dealer = new Player();
        startGame();
    }

    public void startGame(){
        timer.start();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g){
        //draw cards
        drawCard(g);

        //controls
        g.setColor(Color.red);
        g.setFont(new Font("Monospaced",Font.BOLD,12));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Controls: -> hit again; <- don't hit; ^ play again; v end game", (SCREEN_WIDTH-metrics1.stringWidth("Controls: -> hit again; <- don't hit; ^ continue playing; v end game"))/2, metrics1.getHeight());

        //game dialogue
        if(playAgain==0 || playAgain==1){
            if (introPhase){
                g.setColor(Color.red);
                g.setFont(new Font("Monospaced",Font.BOLD,30));
                FontMetrics metrics = getFontMetrics(g.getFont());
                g.drawString("Welcome to the table!", (SCREEN_WIDTH-metrics.stringWidth("Welcome to the table!"))/2, SCREEN_HEIGHT/3);
                g.drawString("My name is GAMBLEBOT 3000,", (SCREEN_WIDTH-metrics.stringWidth("My name is GAMBLEBOT 3000,"))/2, SCREEN_HEIGHT/2);
                g.drawString("and I will be your dealer.", (SCREEN_WIDTH-metrics.stringWidth("and I will be your dealer."))/2, SCREEN_HEIGHT*2/3);
                g.drawString("Press ENTER to begin.", (SCREEN_WIDTH-metrics.stringWidth("Press ENTER to begin."))/2, SCREEN_HEIGHT-metrics.getHeight());
                
                if(skipIntro){
                    introPhase=false;
                    hitPhase=true;
                }
            }
            if(hitPhase){
                if(!cardsDealt){dealPhase();}
                cardsDealt=true;
                while (dealer.totalHand()<=16){
                    dealer.addCard(d.dealCard());
                }

                if (player.totalHand()>=21){ //blackjack or lose, automatically move to results
                    hitAgain=2;
                }
            
                if(hitAgain==1){
                    player.addCard(d.dealCard());
                    hitAgain=0;
                }else if(hitAgain==2){
                    //move on to next phase
                    hitPhase=false;
                    resultsPhase=true;
                    hitAgain=0;
                }else if(hitAgain==0){
                    g.setColor(Color.red);
                    g.setFont(new Font("Monospaced",Font.BOLD,30));
                    FontMetrics metrics = getFontMetrics(g.getFont());
                    g.drawString("Your current total is: ", (SCREEN_WIDTH-metrics.stringWidth("Your current total is: "))/2, SCREEN_HEIGHT/2);
                    g.drawString(">>>"+player.totalHand()+"<<<", (SCREEN_WIDTH-metrics.stringWidth("<<<"+player.totalHand()+">>>"))/2, SCREEN_HEIGHT*5/8);
                    g.drawString("Would you like to hit again?", (SCREEN_WIDTH-metrics.stringWidth("Would you like to hit again?"))/2, SCREEN_HEIGHT*3/4);
                }
                playAgain=0; //so accidental button presses don't cause skipping of the stages

            }
            if(resultsPhase){

                if(playAgain==0){ //display results before player chooses to play again or not
                    g.setColor(Color.red);
                    g.setFont(new Font("Monospaced",Font.BOLD,30));
                    FontMetrics metrics = getFontMetrics(g.getFont());
                    g.drawString("Your total: "+player.totalHand(), (SCREEN_WIDTH-metrics.stringWidth("Your total: "+player.totalHand()))/2, SCREEN_HEIGHT/2);
                    g.drawString("Dealer total: "+dealer.totalHand(), (SCREEN_WIDTH-metrics.stringWidth("Dealer total: "+dealer.totalHand()))/2, SCREEN_HEIGHT*5/8);
                    
                    if(playerWins() && player.totalHand()==21){ //blackjack, although technically not just any hand with a sum of 21 counts as a blackjack, so I might clarify this later
                        g.drawString("BLACKJACK! You win! :)", (SCREEN_WIDTH-metrics.stringWidth("BLACKJACK! You win! :)"))/2, SCREEN_HEIGHT*3/4);
                    }else if(playerWins()){ //normal win
                        g.drawString("You win! Proud of you.", (SCREEN_WIDTH-metrics.stringWidth("You win! Proud of you."))/2, SCREEN_HEIGHT*3/4);
                    }else if(player.totalHand()==dealer.totalHand()||(player.totalHand()>21 && dealer.totalHand()>21)){ //both over or tied
                        g.drawString("At least we lost together.", (SCREEN_WIDTH-metrics.stringWidth("At least we lost together."))/2, SCREEN_HEIGHT*3/4);
                    }else{ //lose
                        g.drawString("you=!winner;", (SCREEN_WIDTH-metrics.stringWidth("you=!winner;"))/2, SCREEN_HEIGHT*3/4);
                    }
                }

                if(playAgain==1){  //play again
                    if(playerWins()){
                        gamesWon++;
                    }else if(player.totalHand()==dealer.totalHand()||(player.totalHand()>21 && dealer.totalHand()>21)){
                        gamesTied++;
                    }else{
                        gamesLost++;
                    }

                    resultsPhase=false;
                    hitPhase=true;

                    playAgain=0;
                    hitAgain=0; //so accidental button presses don't cause skipping of the stages

                    player.clearHand();
                    dealer.clearHand();
                    d.loadDeck();
                    d.shuffleDeck();
                    cardsDealt=false;
                }

            }
        }
        //game ended & no play again conditions met
        if(playAgain==2){
            if(playerWins()){
                gamesWon++;
            }else if(player.totalHand()==dealer.totalHand()||(player.totalHand()>21 && dealer.totalHand()>21)){
                gamesTied++;
            }else{
                gamesLost++;
            }

            if(player.getHand().size()>=2){
                player.clearHand(); 
                repaint();
            }

            g.setColor(Color.red);
            g.setFont(new Font("Monospaced",Font.BOLD,30));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Thanks for your money.", (SCREEN_WIDTH-metrics.stringWidth("Thanks for your money."))/2, SCREEN_HEIGHT/3);
            g.drawString("Have a good night!", (SCREEN_WIDTH-metrics.stringWidth("Have a good night!"))/2, SCREEN_HEIGHT/2);
            g.drawString("Come back again soon! :)", (SCREEN_WIDTH-metrics.stringWidth("Come back again soon! :)"))/2, SCREEN_HEIGHT*2/3);

            timer.stop();
        }
        //score
        g.setColor(Color.red);
        g.setFont(new Font("Monospaced",Font.BOLD,12));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("GAMES WON: "+gamesWon+"  GAMES LOST: "+gamesLost+"  INCONCLUSIVE: "+gamesTied, (SCREEN_WIDTH-metrics2.stringWidth("GAMES WON: "+gamesWon+"  GAMES LOST: "+gamesLost+"  INCONCLUSIVE: "+gamesTied))/2, SCREEN_HEIGHT-metrics1.getHeight());

    }

    public void dealPhase(){
        player.addCard(d.dealCard());
        dealer.addCard(d.dealCard());
        player.addCard(d.dealCard());
        dealer.addCard(d.dealCard());
    }

    public boolean playerWins(){
        boolean win;
        if (player.totalHand()<=21 && player.totalHand()>dealer.totalHand()){
          win=true;
        }else if (player.totalHand()<=21 && dealer.totalHand()>21){
          win=true;
        }else if (player.totalHand()>21){
          win=false;
        }else if (player.totalHand()<dealer.totalHand()){
          win=false;
        }else{
          //System.out.println("did you just... tie?");
          win=false;
        }
        return win;
    }


    public void drawCard(Graphics g){
        //inserting an image is just not working for me, so I'll try to draw the cards :/
        int cardWidth=SCREEN_WIDTH/6; //100
        int cardHeight=SCREEN_HEIGHT/4; //150
        int widthSpace=10;
        ArrayList<Card> hand = player.getHand();
        int cardTranslationX=SCREEN_WIDTH/2-hand.size()*cardWidth/2-hand.size()*widthSpace/2; //centers the cards no matter how many
        int cardTranslationY=SCREEN_HEIGHT/8;
        for(int i=0; i<hand.size();i++){

        int suitTranslationX=i*cardWidth+i*widthSpace+cardTranslationX;
        int suitTranslationY=cardTranslationY;
        int suitTranslationY2=cardTranslationY+cardHeight*3/4;
        int suitTranslationX2=i*cardWidth+i*widthSpace+cardTranslationX+cardWidth*5/8;
        g.setColor(Color.white);
        g.fillRect(0+suitTranslationX, suitTranslationY, cardWidth, cardHeight);
        
        if(hand.get(i).getSuit().equalsIgnoreCase("hearts")){

            //heart
            g.setColor(Color.red);
            int[] xh={11+suitTranslationX,20+suitTranslationX,29+suitTranslationX,20+suitTranslationX};
            int[] yh={19+suitTranslationY,15+suitTranslationY,19+suitTranslationY,30+suitTranslationY};
            g.fillPolygon(xh,yh,4);
            g.fillOval(10+suitTranslationX, 10+suitTranslationY, 10, 10);
            g.fillOval(20+suitTranslationX,10+suitTranslationY,10,10);

            int[] xh2={11+suitTranslationX2,20+suitTranslationX2,29+suitTranslationX2,20+suitTranslationX2};
            int[] yh2={19+suitTranslationY2,15+suitTranslationY2,19+suitTranslationY2,30+suitTranslationY2};
            g.fillPolygon(xh2,yh2,4);
            g.fillOval(10+suitTranslationX2, 10+suitTranslationY2, 10, 10);
            g.fillOval(20+suitTranslationX2,10+suitTranslationY2,10,10);

            //highlight
            g.setColor(Color.white);
            g.fillOval(10+suitTranslationX+cardWidth/30, 10+suitTranslationY+cardHeight/40, 2, 2);
            g.fillOval(20+suitTranslationX+cardWidth/30,10+suitTranslationY+cardHeight/40,2,2);
            g.fillOval(10+suitTranslationX2+cardWidth/30, 10+suitTranslationY2+cardHeight/40, 2, 2);
            g.fillOval(20+suitTranslationX2+cardWidth/30,10+suitTranslationY2+cardHeight/40,2,2);
        }else if(hand.get(i).getSuit().equalsIgnoreCase("clubs")){

            //club
            g.setColor(Color.black);
            int[] xc={20+suitTranslationX,20+suitTranslationX,22+suitTranslationX,18+suitTranslationX};
            int[] yc={20+suitTranslationY,20+suitTranslationY,30+suitTranslationY,30+suitTranslationY};
            g.fillPolygon(xc,yc,4);
            g.fillOval(10+suitTranslationX, 15+suitTranslationY, 10, 10);
            g.fillOval(20+suitTranslationX,15+suitTranslationY,10,10);
            g.fillOval(15+suitTranslationX,10+suitTranslationY,10,10);

            int[] xc2={20+suitTranslationX2,20+suitTranslationX2,22+suitTranslationX2,18+suitTranslationX2};
            int[] yc2={20+suitTranslationY2,20+suitTranslationY2,30+suitTranslationY2,30+suitTranslationY2};
            g.fillPolygon(xc2,yc2,4);
            g.fillOval(10+suitTranslationX2, 15+suitTranslationY2, 10, 10);
            g.fillOval(20+suitTranslationX2,15+suitTranslationY2,10,10);
            g.fillOval(15+suitTranslationX2,10+suitTranslationY2,10,10);

            //highlight
            g.setColor(Color.white);
            g.fillOval(10+suitTranslationX+cardWidth/30, 15+suitTranslationY+cardHeight/40, 2, 2);
            g.fillOval(20+suitTranslationX+cardWidth/30,15+suitTranslationY+cardHeight/40,2,2);
            g.fillOval(15+suitTranslationX+cardWidth/30,10+suitTranslationY+cardHeight/40,2,2);
            g.fillOval(10+suitTranslationX2+cardWidth/30, 15+suitTranslationY2+cardHeight/40, 2, 2);
            g.fillOval(20+suitTranslationX2+cardWidth/30,15+suitTranslationY2+cardHeight/40,2,2);
            g.fillOval(15+suitTranslationX2+cardWidth/30,10+suitTranslationY2+cardHeight/40,2,2);
        }else if(hand.get(i).getSuit().equalsIgnoreCase("spades")){

            //spade
            g.setColor(Color.black);
            int[] xsStick={20+suitTranslationX,20+suitTranslationX,22+suitTranslationX,18+suitTranslationX};
            int[] ysStick={20+suitTranslationY,20+suitTranslationY,30+suitTranslationY,30+suitTranslationY};
            g.fillPolygon(xsStick,ysStick,4);
            g.fillOval(10+suitTranslationX, 15+suitTranslationY, 10, 10);
            g.fillOval(20+suitTranslationX,15+suitTranslationY,10,10);
            int[] xsPoint={10+suitTranslationX,20+suitTranslationX,30+suitTranslationX,20+suitTranslationX};
            int[] ysPoint={18+suitTranslationY,8+suitTranslationY,18+suitTranslationY,25+suitTranslationY};
            g.fillPolygon(xsPoint,ysPoint,4);

            int[] xsStick2={20+suitTranslationX2,20+suitTranslationX2,22+suitTranslationX2,18+suitTranslationX2};
            int[] ysStick2={20+suitTranslationY2,20+suitTranslationY2,30+suitTranslationY2,30+suitTranslationY2};
            g.fillPolygon(xsStick2,ysStick2,4);
            g.fillOval(10+suitTranslationX2, 15+suitTranslationY2, 10, 10);
            g.fillOval(20+suitTranslationX2,15+suitTranslationY2,10,10);
            int[] xsPoint2={10+suitTranslationX2,20+suitTranslationX2,30+suitTranslationX2,20+suitTranslationX2};
            int[] ysPoint2={18+suitTranslationY2,8+suitTranslationY2,18+suitTranslationY2,25+suitTranslationY2};
            g.fillPolygon(xsPoint2,ysPoint2,4);

            //highlight
            g.setColor(Color.white);
            int[] xsHighlight={12+suitTranslationX,20+suitTranslationX,20+suitTranslationX,14+suitTranslationX};
            int[] ysHighlight={20+suitTranslationY,10+suitTranslationY,10+suitTranslationY,20+suitTranslationY};
            g.fillPolygon(xsHighlight,ysHighlight,4);
            int[] xsHighlight2={12+suitTranslationX2,20+suitTranslationX2,20+suitTranslationX2,14+suitTranslationX2};
            int[] ysHighlight2={20+suitTranslationY2,10+suitTranslationY2,10+suitTranslationY2,20+suitTranslationY2};
            g.fillPolygon(xsHighlight2,ysHighlight2,4);

        }else if(hand.get(i).getSuit().equalsIgnoreCase("diamonds")){

            //diamond
            g.setColor(Color.red);
            int[] xd = {10+suitTranslationX,20+suitTranslationX,30+suitTranslationX,20+suitTranslationX};
            int[] yd = {20+suitTranslationY,10+suitTranslationY,20+suitTranslationY,30+suitTranslationY};
            g.fillPolygon(xd,yd,4);

            int[] xd2 = {10+suitTranslationX2,20+suitTranslationX2,30+suitTranslationX2,20+suitTranslationX2};
            int[] yd2 = {20+suitTranslationY2,10+suitTranslationY2,20+suitTranslationY2,30+suitTranslationY2};
            g.fillPolygon(xd2,yd2,4);

            //highlight
            g.setColor(Color.white);
            int[] xdHighlight={12+suitTranslationX,21+suitTranslationX,21+suitTranslationX,14+suitTranslationX};
            int[] ydHighlight={20+suitTranslationY,10+suitTranslationY,10+suitTranslationY,20+suitTranslationY};
            g.fillPolygon(xdHighlight,ydHighlight,4);
            int[] xdHighlight2={12+suitTranslationX2,21+suitTranslationX2,21+suitTranslationX2,14+suitTranslationX2};
            int[] ydHighlight2={20+suitTranslationY2,10+suitTranslationY2,10+suitTranslationY2,20+suitTranslationY2};
            g.fillPolygon(xdHighlight2,ydHighlight2,4);
        }

        g.setColor(Color.black);
        g.setFont(new Font("Monospaced",Font.BOLD,40));
        int rankX = cardWidth/2+suitTranslationX-g.getFont().getSize()/4;
        int rankY = cardHeight/2+suitTranslationY+g.getFont().getSize()/4;

        if(hand.get(i).getRank().equalsIgnoreCase("two")){
            g.drawString("2", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("three")){
            g.drawString("3", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("four")){
            g.drawString("4", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("five")){
            g.drawString("5", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("six")){
            g.drawString("6", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("seven")){
            g.drawString("7", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("eight")){
            g.drawString("8", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("nine")){
            g.drawString("9", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("ten")){
            g.drawString("10", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("jack")){
            g.drawString("J", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("queen")){
            g.drawString("Q", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("king")){
            g.drawString("K", rankX, rankY);
        }else if(hand.get(i).getRank().equalsIgnoreCase("ace")){
            g.drawString("A", rankX, rankY);
        }

        }

    }

    public void actionPerformed(ActionEvent e){
        
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter{
        
        @Override
        public void keyPressed(KeyEvent e){
            switch(e.getKeyCode()){
                case (KeyEvent.VK_RIGHT):
                    hitAgain = 1; //hit again
                    break;
                case(KeyEvent.VK_LEFT):
                    hitAgain = 2; //next phase
                    break;
                case(KeyEvent.VK_UP):
                    playAgain = 1; //play again
                    break;
                case(KeyEvent.VK_DOWN):
                    playAgain = 2; //end game
                    break;
                case(KeyEvent.VK_ENTER):
                    skipIntro = true;
            }
        }

        
    }

}
