package com.github.kazuhito_m.commons.log;

import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 内部ログオブジェクトに対し「すべてのログの記録を取る」ためのインターセプタ。
 *
 * @author Kazuhito Miura
 */
public class LogInterceptHandler implements InvocationHandler {

    private static List<String> LOG_LEVELS =
            Arrays.asList(new String[]{"debug", "info", "warn", "error",
                    "fatalError"});
    protected List<LogDetail> history = null;
    private ConsoleLogger logger = null;

    /**
     * コンストラクタ。<br>
     * 内部で実際に実行するLoggerオブジェクトを引数として録る。
     *
     * @param logger
     */
    public LogInterceptHandler(ConsoleLogger logger) {
        this.logger = logger;
    }

    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

//        if (args == null) {
//            System.out.println("メソッド名 : " + method.getName() + " , 引数 : なし");
//        } else {
//            System.out.println("メソッド名 : " + method.getName() + " , 引数 : "
//                + args.length);
//        }
//
        // 内部に履歴オブジェクトがセットされていたら、ログの履歴を取る処理を行う。
        if (this.history != null) {
            int level = LOG_LEVELS.indexOf(method.getName());
            if (level >= 0) {
                // 対象のメソッドなら、ログの履歴取りを開始
                LogDetail log = new LogDetail();
                log.setLevel(level);
                log.setOutputTime(new Date());

                // 引数チェック。あるなら。
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof String) {
                        log.setMessage((String) args[i]);
                    } else if (args[i] instanceof Throwable) {
                        log.setThrowItem((Throwable) args[i]);
                    }
                }
                // 履歴リストに追加。
                this.history.add(log);
            }
        }

        // 型違いで別のインターフェイスについているメソッドは返せないため、ちょっと小細工。
        if (method.getName().equals("getThreshold")) {
            return this.logger.getThreshold();
        } else if (method.getName().equals("setThreshold")) {
            this.logger.setThreshold(((Integer) args[0]).intValue());
            return null;
        }

        // 本来呼ばれるべきメソッドを本来のインスタンスで実行します。

        Object ret = method.invoke(this.logger, args);
        return ret;
    }

    // Setter/Getter群。

    /**
     * @return the history
     */
    public List<LogDetail> getHistory() {
        return history;
    }

    /**
     * @param history the history to set
     */
    public void setHistory(List<LogDetail> history) {
        this.history = history;
    }

}
