
import java.util.ArrayList;

public class Deck {
  private final int NUMBER_OF_CARDS = 52;
  private ArrayList<Card> deck = new ArrayList<>(NUMBER_OF_CARDS);
    private Suit suit = new Suit();
    private Rank rank = new Rank();

    public Deck() {}

  public ArrayList<Card> getDeck(){
    return deck;
  }
  public void setDeck(int index, Card c){
    deck.add(index, c);
  }
    public void setDeck(int index, String s, String r, int v) {
        deck.add(index, new Card(s, r, v));
    }
  public Card dealCard(){
    return deck.remove(0);
  }
  public void loadDeck(){
    for (String s : suit.getSuits()) {
        for (int j = 0; j < rank.getRanks().length; j++) {
            deck.add(new Card(s, rank.getRanks()[j], rank.getRankValues()[j]));
        }
    }
  }
  public void shuffleDeck(){
    for (int i = deck.size() - 1; i > 0; i--) {
        Card current = deck.get(i);
        int swapindex = (int) (Math.random() * i);
        Card swap = deck.get(swapindex);
        deck.set(i, swap);
        deck.set(swapindex, current);
    }
  }

    public String toString() {
        String spacing = "   \t";
        String output = "[\n" + spacing + deck.get(0);
        for (int i = 1; i < deck.size(); i++) {
            output += spacing + deck.get(i).toString();
            if (i%4 == 3) {
                output += "\n";
            }
        }
        output += "]";
        return output;
    }
}