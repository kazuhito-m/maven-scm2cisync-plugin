package tk.bnbm.scm2cisync.mojo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import tk.bnbm.commons.mojo.BaseMojo;
import tk.bnbm.scm2cisync.core.ScmToCiSynchronizer;

import java.util.ArrayList;

import static org.apache.commons.lang.builder.ToStringStyle.MULTI_LINE_STYLE;

/**
 * CI(継続的インテグレーション)コントロールプラグイン - SCM(ソース管理)と同期プラグイン
 *
 * @author Kazuhito Miura
 * @goal sync
 * @phase initialize
 */
public class ScmToCiSynchronizerMojo extends BaseMojo {

    // MavenPlugin組み込みパラメータ群

    /**
     * このPlungInが実行されたディレクトリまでのフルパス。<br>
     * (pom.xmlが置かれている場所とは限らない)
     *
     * @parameter expression="${basedir}"
     * @required
     */
    @SuppressWarnings("unused")
    private String basedir;

    // 自身PlugIn用パラメータ群
    /**
     * 最適化パターン(複数指定)。 ArtifactId(正規表現)の規則ごと設定を変更する、そのパターン群。
     *
     * @parameter expression="${optimizePatterns}"
     */
    private ArrayList<SynchronizeTarget> synchronizeTargets = new ArrayList<SynchronizeTarget>();

    /**
     * 実行 {@inheritDoc}
     *
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException {

        // パラメータがセットされるのが、どうもNew後→execute()の直前らしい。再度ログオブジェクトを取得直す。
        log = super.getLog();

        // デバッグ域
        if (log.isDebugEnabled()) {
            debugDump();
        }

        // 主処理
        try {

            infoLog("CI to SCM Synchronize start !");

            // mvn-test前の準備処理を行う(前から優先)
            ScmToCiSynchronizer synchronizer = new ScmToCiSynchronizer();
            synchronizer.setLog(log);

            // パターンをすべて回す
            for (SynchronizeTarget st : synchronizeTargets) {
                // シンクロナイザーによる処理実行。
                synchronizer.synchronize(st);
            }

            infoLog("CI to SCM Synchronize succeed !");

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * デバッグ用メソッド
     */
    private void debugDump() {

        // システムプロパティを取得
        debugLog("[SystemProperties]");
        debugLog(ToStringBuilder.reflectionToString(System.getProperties(),
                MULTI_LINE_STYLE));

        // プロジェクトの情報をデバッグで出力。
        debugLog("[ProjectInfo]");
        debugLog(ToStringBuilder.reflectionToString(project, MULTI_LINE_STYLE));

        // 自身の情報をデバッグで出力。
        debugLog("[SelfProperties]");
        debugLog(ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE));

    }
}
