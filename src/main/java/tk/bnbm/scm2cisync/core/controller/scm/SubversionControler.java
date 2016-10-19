package tk.bnbm.scm2cisync.core.controller.scm;

import org.apache.maven.plugin.logging.Log;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.tmatesoft.svn.core.wc.SVNRevision.HEAD;

/**
 * Subversion(SCMサーバの一種)のコントロールを行うクラス。
 *
 * @author Kazuhito Miura
 */
public class SubversionControler {

    /**
     * デバッグ/情報表示用ログオブジェクト。
     */
    protected Log log = null;

    /**
     * Subversionリポジトリへと接続するURL文字列
     */
    private String urlPath = null;

    /**
     * 認証タイプ。null or 期待の文字列ではければ「認証無し」扱い。(今のところ"basic"のみ)
     */
    private String authType = null;

    /**
     * 認証時ユーザID(authTypeの設定があった時のみ有効)
     */
    private String userName = null;

    /**
     * 認証時パスワード(authTypeの設定があった時のみ有効)
     */
    private String password = null;

    /**
     * Subversionディレクトリの直下のディレクトリの一覧を返す。
     *
     * @param revisionNo 取得するディレクトリのリビジョン。0を指定した場合は「最新取得」と解釈する。
     * @return ディレクトリ名が文字列要素となったListオブジェクト。
     * @throws SVNException
     */
    public List<String> getDirNameList(long revisionNo) throws SVNException {

        final List<String> dirNameList = new ArrayList<String>();

        // SVNに接続準備
        DAVRepositoryFactory.setup();
        SVNClientManager manager = this.createOptimizedClientManager();

        // "svn list"コマンドのエミュレーションはLogCliantで。
        SVNLogClient client = manager.getLogClient();

        // 指定リビジョンが 0以上なら
        SVNRevision rev = HEAD;
        if (revisionNo > 0) {
            rev = SVNRevision.create(revisionNo);
        }

        // コマンド実行(リビジョンは最大)
        SVNURL url = SVNURL.parseURIEncoded(urlPath);
        client.doList(url, HEAD, rev, false, new ISVNDirEntryHandler() {

            public void handleDirEntry(SVNDirEntry svnDir) throws SVNException {
                dirNameList.add(svnDir.getName()); // ハンドラでは結果リストに名前だけ足す。
            }
        });

        // 「ディレクトリの名前だけリスト」を返す。
        return dirNameList;

    }

    /**
     * Subversionディレクトリの直下のディレクトリの一覧を返す。<br>
     * 引数無し版。「リビジョン最新」固定で取得する。
     *
     * @return ディレクトリ名が文字列要素となったListオブジェクト。
     * @throws SVNException
     */
    public List<String> getDirNameList() throws SVNException {
        return getDirNameList(0L); // 0<=指定 : 最新
    }

    /**
     * Subversionディレクトリの直下のディレクトリで、 引数に指定されたリビジョン番号より後に追加されたディレクトリの一覧を返す。<br>
     *
     * @param fromRevisionNo 対象とするリビジョン。この番号より後に追加されたものだけを対象とする。
     * @return ディレクトリ名が文字列要素となったListオブジェクト。
     * @throws SVNException
     */
    public List<String> getAddedDirNameList(long fromRevisionNo)
            throws SVNException {

        // 指定リビジョンのリストと最新のリストをSet型で取得しておく
        List<String> lastNames = this.getDirNameList(fromRevisionNo);
        Set<String> headNames = new HashSet<String>(this.getDirNameList());

        for (String name : lastNames) {
            // 存在した場合、その最新セットから削除
            if (headNames.contains(name)) {
                headNames.remove(name);
            }
        }

        // 「ディレクトリの名前だけリスト」を返す。
        return new ArrayList<String>(headNames);

    }

    /**
     * Subversionディレクトリの直下のディレクトリで、 引数に指定されたリビジョン番号より後削除されたディレクトリの一覧を返す。<br>
     *
     * @param fromRevisionNo 対象とするリビジョン。この番号より後に削除されたものだけを対象とする。
     * @return ディレクトリ名が文字列要素となったListオブジェクト。
     * @throws SVNException
     */
    public List<String> getDeletedDirNameList(long fromRevisionNo)
            throws SVNException {

        // 指定リビジョンのリストと最新のリストをSet型で取得しておく
        Set<String> lastNames = new HashSet<String>(
                this.getDirNameList(fromRevisionNo));
        List<String> headNames = this.getDirNameList();

        for (String name : headNames) {
            // 存在した場合、その最新セットから削除
            if (lastNames.contains(name)) {
                lastNames.remove(name);
            }
        }

        // 「ディレクトリの名前だけリスト」を返す。
        return new ArrayList<String>(lastNames);

    }

    /**
     * Subversionディレクトリの直下のディレクトリの「現在のリビジョン番号」を返す。
     *
     * @return 最新リビジョン番号。
     * @throws SVNException
     */
    public long getHeadRevisionNo() throws SVNException {

        // SVNに接続準備
        DAVRepositoryFactory.setup();
        SVNClientManager manager = this.createOptimizedClientManager();

        SVNURL url = SVNURL.parseURIEncoded(urlPath);

        // "svn list"コマンドのエミュレーションはLogCliantで。
        SVNWCClient client = manager.getWCClient();

        // コマンド実行(リビジョンは最大)
        SVNInfo info = client.doInfo(url, HEAD, HEAD);

        // リビジョン番号を返す。
        return info.getRevision().getNumber();

    }

    /**
     * 自身オブジェクトに設定された各種情報を元に、最適なSVNクライアントマネージャーを作成する。
     *
     * @return
     */
    protected SVNClientManager createOptimizedClientManager() {
        SVNClientManager manager;
        // authTypeプロパティを判定し、認証アリであれば設定を施したClientManagerを返す。
        String fixedType = this.authType == null ? "" : this.authType
                .toLowerCase();
        if (fixedType.equals("basic")) {
            // BASIC認証のクライアントを返す。
            BasicAuthenticationManager bam = new BasicAuthenticationManager(
                    this.userName, this.password);
            manager = SVNClientManager.newInstance(
                    SVNWCUtil.createDefaultOptions(true), bam);
        } else {
            // 未設定or認識不能。「認証無し」と見なし、デフォルトのClientManagerを返す。
            manager = SVNClientManager.newInstance(SVNWCUtil
                    .createDefaultOptions(true));
        }
        return manager;
    }

    // Getter/Setter群

    /**
     * Logオブジェクトのセット。
     *
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @return the urlPath
     */
    public String getUrlPath() {
        return urlPath;
    }

    /**
     * @param urlPath the urlPath to set
     */
    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    /**
     * @return authType
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * @param authType セットする authType
     */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName セットする userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password セットする password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
