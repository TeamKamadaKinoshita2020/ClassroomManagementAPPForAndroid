package jp.ac.ibaraki.felicacardidlinkapp;

/**
 * Created by 大学用 on 2017/09/04.
 * 講義情報に関するクラス
 */

public class ClassroomInfo{
    private String classroomName;
    private int roomId;

    /**
     * コンストラクタ
     * @param classroomName 教室名
     * @param roomId　教室ID
     */
    public ClassroomInfo(String classroomName, int roomId) {
        this.classroomName = classroomName;
        this.roomId = roomId;
    }

    public String getName() {
        return classroomName;
    }

    public int getId() { return roomId; }
}