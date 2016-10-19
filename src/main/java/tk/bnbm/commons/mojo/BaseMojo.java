package tk.bnbm.commons.mojo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import tk.bnbm.commons.log.CustomSystemStreamLog;

/**
 * 本プロジェクト中Mojoクラス群の基底クラス。
 *
 * @author Kazuhito Miura
 * @goal list
 * @requiresDependencyResolution test
 */
public abstract class BaseMojo extends AbstractMojo {

    /**
     * Mavenプロジェクト情報(pom.xmlの内容)
     *
     * @parameter default-value="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * debug enabled
     *
     * @parameter expression="${debug}"
     */
    protected boolean debug;

    /**
     * full path when this plugin execute.
     *
     * @parameter expression="${basedir}"
     * @required
     */
    protected String basedir;

    /**
     * デバッグ用ログオブジェクト。
     */
    protected Log log = this.getLog();

    /**
     * インフォメーションログ出力。
     *
     * @param log     Mavenのログオブジェクト。
     * @param message 出力文字列。
     */
    protected void infoLog(String message) {
        if (log != null) {
            if (log.isInfoEnabled()) {
                log.info((CharSequence) message);
            }
        }
    }

    /**
     * デバッグログ出力。
     *
     * @param log     Mavenのログオブジェクト。
     * @param message 出力文字列。
     */
    protected void debugLog(String message) {
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug((CharSequence) message);
            }
        }
    }

    /**
     * ログオブジェクトを生成し、返す。
     *
     * @return Returns the log.
     */
    @Override
    public Log getLog() {
        CustomSystemStreamLog customLog = new CustomSystemStreamLog();
        customLog.setDebugEnabled(this.debug);
        log = customLog;
        return this.log;
    }

    /**
     * 自身オブジェクトの文字列表現を返す。 プロパティを列挙するようにオーバーライドしている。
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * Mavenプロジェクト情報(pom.xmlの内容)オブジェクト を返す。
     *
     * @return Mavenプロジェクト情報(pom.xmlの内容)オブジェクト。
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * @return the basedir
     */
    public String getBasedir() {
        return basedir;
    }

}
