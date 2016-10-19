package tk.bnbm.scm2cisync.mojo;

import tk.bnbm.commons.idfilter.IdFilter;

/**
 * 最適化パターンクラス。 Id(正規表現)の規則ごとの設定値を保持する。
 *
 * @author Kazuhito Miura
 */
@SuppressWarnings("serial")
public class SynchronizeTarget extends IdFilter {

    /**
     * 前回実行情報の保存パス
     */
    private String lastProfilePath;

    /**
     * ソース管理システムのタイプ(今はSVNのみ)
     */
    private String scmType;

    /**
     * ソース管理システムのURL(http以外のプロトコルor認証は非対応)
     */
    private String scmUrl;

    /**
     * 認証タイプ。null or 期待の文字列ではければ「認証無し」扱い。(今のところ"basic"のみ)
     */
    private String scmAuthType;

    /**
     * 認証時ユーザID(authTypeの設定があった時のみ有効)
     */
    private String scmUserName;

    /**
     * 認証時パスワード(authTypeの設定があった時のみ有効)
     */
    private String scmPassword;

    /**
     * ソース管理システムのタイプ(今はSVNのみ)
     */
    private String ciType;

    /**
     * ソース管理システムのURL(http以外のプロトコルor認証は非対応)
     */
    private String ciUrl;

    /**
     * ソース管理システム中のコントロールジョブ(下流ジョブを実行するジョブ)名
     */
    private String ciControlJobName;

    /**
     * jenkinsの1jobのテンプレートファイル
     */
    private String jenkinsJobTemplatePath;

    /**
     * SCM中で削除されていたものはjobを無効化するか否か
     */
    private boolean jobDisableByScmDelete = true;

    // プロパティ:Setter/Getter

    public String getLastProfilePath() {
        return lastProfilePath;
    }

    public void setLastProfilePath(String lastProfilePath) {
        this.lastProfilePath = lastProfilePath;
    }

    public String getScmType() {
        return scmType;
    }

    public void setScmType(String scmType) {
        this.scmType = scmType;
    }

    public String getScmUrl() {
        return scmUrl;
    }

    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    public String getCiType() {
        return ciType;
    }

    public void setCiType(String ciType) {
        this.ciType = ciType;
    }

    public String getCiUrl() {
        return ciUrl;
    }

    public void setCiUrl(String ciUrl) {
        this.ciUrl = ciUrl;
    }

    public String getJenkinsJobTemplatePath() {
        return jenkinsJobTemplatePath;
    }

    public void setJenkinsJobTemplatePath(String jenkinsJobTemplatePath) {
        this.jenkinsJobTemplatePath = jenkinsJobTemplatePath;
    }

    public boolean isJobDisableByScmDelete() {
        return jobDisableByScmDelete;
    }

    public void setJobDisableByScmDelete(boolean jobDisableByScmDelete) {
        this.jobDisableByScmDelete = jobDisableByScmDelete;
    }

    public String getCiControlJobName() {
        return ciControlJobName;
    }

    public void setCiControlJobName(String ciControlJobName) {
        this.ciControlJobName = ciControlJobName;
    }

    /**
     * @return scmAuthType
     */
    public String getScmAuthType() {
        return scmAuthType;
    }

    /**
     * @param scmAuthType セットする scmAuthType
     */
    public void setScmAuthType(String scmAuthType) {
        this.scmAuthType = scmAuthType;
    }

    /**
     * @return scmUserName
     */
    public String getScmUserName() {
        return scmUserName;
    }

    /**
     * @param scmUserName セットする scmUserName
     */
    public void setScmUserName(String scmUserName) {
        this.scmUserName = scmUserName;
    }

    /**
     * @return scmPassword
     */
    public String getScmPassword() {
        return scmPassword;
    }

    /**
     * @param scmPassword セットする scmPassword
     */
    public void setScmPassword(String scmPassword) {
        this.scmPassword = scmPassword;
    }

}
