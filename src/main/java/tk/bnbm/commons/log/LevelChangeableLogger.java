package tk.bnbm.commons.log;

import org.codehaus.plexus.logging.Logger;

/**
 * 実行中レベル変更可能ロガーのインターフェイス。
 *
 * @author Kazuhito Miura
 */
public interface LevelChangeableLogger extends Logger {

    /**
     * 現在のログレベルを取得する。
     */
    int getThreshold();

    /**
     * 現在のログレベルを設定し直す。
     */
    void setThreshold(int threshold);
}
