package com.github.kazuhito_m.commons.log;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * maven-plugin専用のログクラス。<br>
 * MavenのLog、plexusのLoggerの性質を併せ持つクラス。
 * また、「ログ履歴を参照できる」機能が追加されている。
 *
 * @author Kazuhito Miura
 */
public class CustomSystemStreamLog implements LevelChangeableLogger, Log {

    /**
     * 内部ロギング実処理クラス(org.codehaus.plexus.logging.console.ConsoleLogger)
     */
    protected LevelChangeableLogger innerLogger;
    /**
     * ログ履歴リスト取得用、内部プロキシクラス
     */
    LogInterceptHandler logProxy;

    /**
     * コンストラクタ。<br>
     * 内部実処理クラスConsoleLoggerのコンストラクタを呼ぶ。
     * 初期レベルは"警告以上で出力",名前は自身クラス名。
     */
    public CustomSystemStreamLog() {
        this(LEVEL_WARN, CustomSystemStreamLog.class.getName());// 初期レベルは"警告以上"
    }

    /**
     * コンストラクタ。<br>
     * 内部実処理クラスConsoleLoggerのコンストラクタを呼ぶ。
     * さらにそれを核として、JavaのDynamicProxyを利用し、
     * LogInterceptHandlerによる「ログの履歴取り機能」を加える。
     *
     * @param threshold 初期ログレベル。
     * @param name      ログの名前。
     */
    public CustomSystemStreamLog(int threshold, String name) {

        // 通常のnewで得られるインスタンスを用意しておきます。
        ConsoleLogger logger = new ConsoleLogger(threshold, name);

        // 呼び出しハンドラクラスを生成します。
        // その際に実際のインスタンスを渡しておきます。
        this.logProxy = new LogInterceptHandler(logger);

        // Proxy.newProxyInstanceメソッドを使ってインスタンスを生成します。
        innerLogger =
                (LevelChangeableLogger) Proxy.newProxyInstance(
                        CustomSystemStreamLog.class.getClassLoader(),
                        new Class[]{LevelChangeableLogger.class}, logProxy);
    }

    /**
     * 履歴機能が有効か否かを真偽値で返す。
     *
     * @return 履歴機能が有効か否か。
     */
    public boolean isHistoryOn() {
        return logProxy.getHistory() != null;
    }

    /**
     * 履歴機能が有効・無効を指定する。<br>
     * <p>
     * 無効から有効にする場合、新たに内部履歴リストが作成され、
     * 有効から無効とする場合、内部履歴リストが削除される。
     *
     * @param historyOn true:有効、false:無効
     */
    public void setHistoryOn(boolean historyOn) {
        if (historyOn) {
            if (logProxy.getHistory() == null) {
                logProxy.setHistory(new ArrayList<LogDetail>());
            }
        } else {
            if (logProxy.getHistory() != null) {
                logProxy.setHistory(null);
            }
        }
    }

    /**
     * 蓄えられたログ履歴を検索し、引数に指定されたレベルに対応するものがあれば、
     * それのみを返す。
     *
     * @param logLevels 取得したいログレベル(可変引数)。
     * @return 該当するログのリスト。
     */
    public List<LogDetail> getHistory(int... logLevels) {

        // まず、無い場合を省く。
        if (logProxy.getHistory() == null) {
            return null;
        }

        List<LogDetail> results = new ArrayList<LogDetail>();

        // 「指定されていた」フラグを用意。
        List<Integer> lavels = null;
        if (logLevels != null && logLevels.length > 0) {
            lavels = new ArrayList<Integer>();
            for (int i = 0; i < logLevels.length; i++) {
                lavels.add(logLevels[i]);
            }
        }

        // 今まで溜まっている履歴を検査しながらループ
        for (LogDetail log : this.logProxy.getHistory()) {
            if (lavels == null || lavels.contains(log.getLevel())) {
                results.add(log);
            }

        }
        return results;
    }

    /**
     * 蓄えられたログ履歴をすべて返す。
     *
     * @return 全件ログのリスト。
     */
    public List<LogDetail> getHistory() {
        return this.getHistory(null);
    }

    /**
     * 自身の子ロガーを取得する。
     */
    public Logger getChildLogger(String name) {
        return innerLogger.getChildLogger(name);
    }

    /**
     * ログレベルの設定を変更する。
     *
     * @param level  指定するログレベル。
     * @param enable 指定されたログレベルを有効とするか無効とするか。
     */
    private void levelControl(int level, boolean enable) {
        if (enable) {
            if (this.innerLogger.getThreshold() > level) {
                this.innerLogger.setThreshold(level);
            }
        } else {
            if (this.innerLogger.getThreshold() <= level) {
                this.innerLogger.setThreshold(level + 1);
            }
        }
    }

    /**
     * ロガーの名前を返す。
     */
    public String getName() {
        return innerLogger.getName();
    }

    /**
     * 設定されているログレベルを返す。
     */
    public int getThreshold() {
        return innerLogger.getThreshold();
    }

    // Getter/Setter群

    /**
     * ログレベルを変更する。<br>
     * 実行の途中で出来るのは、LevelChangeableLoggerの特性によるもの。
     */
    public void setThreshold(int threshold) {
        innerLogger.setThreshold(threshold);
    }

    /**
     * 自身のログレベルでdeubgが有効かどうかを真偽値で返す。
     */
    public boolean isDebugEnabled() {
        return innerLogger.isDebugEnabled();
    }

    /**
     * debug設定のOn/Offを真偽値で設定する。
     *
     * @param enable debug指定。true:Onにする , false:offにする。
     */
    public void setDebugEnabled(boolean enable) {
        levelControl(LEVEL_DEBUG, enable);
    }

    /**
     * 自身ログレベルでerrorが有効か否かを真偽値で返す。
     */
    public boolean isErrorEnabled() {
        return innerLogger.isErrorEnabled();
    }

    /**
     * 自身ログレベルでfatalErrorが有効か否かを真偽値で返す。
     */
    public boolean isFatalErrorEnabled() {
        return innerLogger.isFatalErrorEnabled();
    }

    /**
     * 自身ログレベルでinfoが有効か否かを真偽値で返す。
     */
    public boolean isInfoEnabled() {
        return innerLogger.isInfoEnabled();
    }

    /**
     * info設定のOn/Offを真偽値で設定する。
     *
     * @param enable info指定。true:Onにする , false:offにする。
     */
    public void setInfoEnabled(boolean enable) {
        levelControl(LEVEL_INFO, enable);
    }

    /**
     * 自身ログレベルでwarnが有効か否かを真偽値で返す。
     */
    public boolean isWarnEnabled() {
        return innerLogger.isWarnEnabled();
    }

    /**
     * infoレベルのログを出力する。
     */
    public void info(String message) {
        this.innerLogger.info(message);
    }

    /**
     * infoレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void info(String message, Throwable throwable) {
        this.innerLogger.info(message, throwable);
    }

    /**
     * infoレベルのログをCharSequenceで出力する。
     */
    public void info(CharSequence content) {
        this.info(content.toString());
    }

    /**
     * infoレベルのログへthrowableオブジェクトを出力する。
     */
    public void info(Throwable error) {
        this.info(error.getMessage(), error);
    }

    /**
     * infoレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void info(CharSequence content, Throwable error) {
        this.info(content.toString(), error);
    }

    /**
     * warnレベルのログを出力する。
     */
    public void warn(String message) {
        this.innerLogger.warn(message);
    }

    /**
     * warnレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void warn(String message, Throwable throwable) {
        this.innerLogger.warn(message, throwable);
    }

    /**
     * warnレベルのログを出力する。
     */
    public void warn(CharSequence content) {
        this.warn(content.toString());
    }

    /**
     * warnレベルのログへthrowableオブジェクトを出力する。
     */
    public void warn(Throwable error) {
        this.warn(error.getMessage(), error);
    }

    /**
     * warnレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void warn(CharSequence content, Throwable error) {
        this.warn(content.toString(), error);
    }

    /**
     * debugレベルのログを出力する。
     */
    public void debug(String message) {
        this.innerLogger.debug(message);
    }

    /**
     * debugレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void debug(String message, Throwable throwable) {
        this.innerLogger.debug(message, throwable);
    }

    /**
     * debugレベルのログを出力する。
     */
    public void debug(CharSequence content) {
        this.debug(content.toString());
    }

    /**
     * debugレベルのログへthrowableオブジェクトを出力する。
     */
    public void debug(Throwable error) {
        this.debug(error.getMessage(), error);
    }

    /**
     * debugレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void debug(CharSequence content, Throwable error) {
        this.debug(content.toString(), error);
    }

    /**
     * errorレベルのログを出力する。
     */
    public void error(String message) {
        this.innerLogger.error(message);
    }

    /**
     * errorレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void error(String message, Throwable throwable) {
        this.innerLogger.error(message, throwable);
    }

    /**
     * errorレベルのログを出力する。
     */
    public void error(CharSequence content) {
        this.error(content.toString());
    }

    /**
     * errorレベルのログへthrowableオブジェクトを出力する。
     */
    public void error(Throwable error) {
        this.error(error.getMessage(), error);
    }

    /**
     * errorレベルのログをthrowableオブジェクト付きで出力する。
     */
    public void error(CharSequence content, Throwable error) {
        this.error(content.toString(), error);
    }

    /**
     * fatalErrorレベルのログを出力する。
     */
    public void fatalError(String message) {
        this.innerLogger.fatalError(message);
    }

    /**
     * fatalErrorレベルのログを出力する。
     */
    public void fatalError(String message, Throwable throwable) {
        this.innerLogger.fatalError(message, throwable);
    }

}
