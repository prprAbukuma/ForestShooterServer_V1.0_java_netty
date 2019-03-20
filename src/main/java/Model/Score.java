package Model;

public class Score {
    private int id;
    private int userid;
    private int totalCount;
    private int winCount;
    public Score(){}
    public Score(int id,int userid,int totalCount,int winCount)
    {
        this.id=id;
        this.userid=userid;
        this.totalCount=totalCount;
        this.winCount=winCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }
}
