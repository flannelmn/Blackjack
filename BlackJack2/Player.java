
import java.util.ArrayList;

public class Player {
  private ArrayList<Card> hand=new ArrayList<>();

    public Player(){}

    public void addCard(Card c) {
        hand.add(c);
    }

    public void clearHand(){
      hand.clear();
    }

  public int totalHand() {
      int sum = 0;
      int aces = 0;
    for (Card c: hand){
      sum += c.getRankValue();
        if (c.getRank() == "Ace") {
            aces++;
        }
    }
      while (sum > 21 && aces > 0) {
          sum -= 10;
          aces--;
      }
    return sum;
      
  }

  public ArrayList<Card> getHand(){
    return hand;
  }

    /*public int total2() {
        int total = 0;
        int aces = 0;

        for (Card c: hand) {
            if (c.getRank().equalsIgnoreCase("ace")) {
                total += 1;
                aces++;
            } else {
                total += c.getRankValue();
            }
        }
      return total;
    }*/
  
}