package jp.ac.ibaraki.felicacardidlinkapp;

/**
 * Created by 大学用 on 2017/11/15.
 */

public class SeatInfo {
    private String seatNum;
    private String cardId;
    private String identityId;

    /**
     * コンストラクタ
     * @param seatNum 座席番号
     * @param cardId　カードID
     */
    public SeatInfo(String seatNum,String cardId,String identityId) {
        this.seatNum = seatNum;
        this.cardId = cardId;
        this.identityId = identityId;
    }

    public String getNum() {
        return seatNum;
    }

    public String getCardId() { return cardId; }

    public String getIdentityId() { return identityId; }
}
