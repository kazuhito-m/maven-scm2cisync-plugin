package com.github.kazuhito_m.commons.log;

import java.util.Date;

/**
 * 「ログの内容」を表現するクラス。
 * CustomSystemStreamLog内部、historyプロパティの要素となるオブジェクトである。
 *
 * @author Kazuhito Miura
 */
public class LogDetail {

    // 内部フィールド値

    /**
     * 出力された日付時刻
     */
    private Date outputTime;

    /**
     * ログレベル(org.codehaus.plexus.logging.Loggerのログレベル値に準拠)
     */
    private int level;

    /**
     * 出力メッセージ
     */
    private String message;

    /**
     * 出力時のThrowableオブジェクト
     */
    private Throwable throwItem;

    // Getter/Setter群

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the outputTime
     */
    public Date getOutputTime() {
        return outputTime;
    }

    /**
     * @param outputTime the outputTime to set
     */
    public void setOutputTime(Date outputTime) {
        this.outputTime = outputTime;
    }

    /**
     * @return the throwItem
     */
    public Throwable getThrowItem() {
        return throwItem;
    }

    /**
     * @param throwItem the throwItem to set
     */
    public void setThrowItem(Throwable throwItem) {
        this.throwItem = throwItem;
    }

}
