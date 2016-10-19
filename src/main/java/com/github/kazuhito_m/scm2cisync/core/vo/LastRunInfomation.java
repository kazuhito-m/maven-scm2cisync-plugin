package com.github.kazuhito_m.scm2cisync.core.vo;

import java.io.Serializable;

/**
 * 前回実行の記録を持つVO。<br>
 * ファイルにて保存する。
 *
 * @author Kazuhito Miura
 */
@SuppressWarnings("serial")
public class LastRunInfomation implements Serializable {

    /**
     * SCMから取得した際の対象ディレクトリのリビジョン番号
     */
    private long scmHeadRevisionNumber = 0;

    public long getScmHeadRevisionNumber() {
        return scmHeadRevisionNumber;
    }

    public void setScmHeadRevisionNumber(long scmHeadRevisionNumber) {
        this.scmHeadRevisionNumber = scmHeadRevisionNumber;
    }

}
