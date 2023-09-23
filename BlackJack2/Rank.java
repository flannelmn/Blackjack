
public class Rank {
    private final String[] ranks={"Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King", "Ace"};

    public String[] getRanks() {
        return ranks;
    }

    public int[] getRankValues() {
        return rankValues;
    }

    private final int[] rankValues={2,3,4,5,6,7,8,9,10,10,10,10,11};


}