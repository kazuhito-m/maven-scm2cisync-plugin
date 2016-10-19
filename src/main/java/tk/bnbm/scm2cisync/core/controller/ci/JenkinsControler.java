package tk.bnbm.scm2cisync.core.controller.ci;

import hudson.cli.CLI;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import tk.bnbm.commons.io.LineStringListOutputStream;
import tk.bnbm.commons.io.StringBuilderOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Jenkins(CIサーバの一種)のコントロールを行うクラス。
 *
 * @author Kazuhito Miura
 */
public class JenkinsControler {

    /**
     * デバッグ/情報表示用ログオブジェクト。
     */
    protected Log log = null;

    /**
     * Jenkinsへと接続するURL文字列
     */
    private String urlPath = null;

    /**
     * Jenkinsサーバに現在登録されているすべてのJob名を取得する。
     *
     * @return job名の文字列Set。
     * @throws InterruptedException Jenkinsに割り込みが入った場合の例外。
     * @throws IOException          何らかの入出力エラー
     */
    public List<String> getJobNames() throws IOException, InterruptedException {

        // 答えはList<String>で受ける
        List<String> lines = new ArrayList<String>();
        OutputStream os = new LineStringListOutputStream(lines);

        // Jenkins-cliの「groovy実行コマンド」を利用し、job全量の列挙させる。
        String groovyScript =
                "for (item in hudson.model.Hudson.instance.items) println(item.name)";
        execCli(null, os, "groovysh", groovyScript);

        // debug
        if (log.isDebugEnabled()) {
            log.debug("groovysh result : ");
            for (String line : lines) {
                log.debug(line);
            }
        }

        // 末尾一行はノイズなので削除
        // FIXME 出力形式に依存しているが、日本語じゃなくなっても大丈夫…だと思う。要検証。
        lines.remove(lines.size() - 1);

        return lines;

    }

    /**
     * Jenkinsサーバから指定したjob名の設定ファイルをダウンロードする。
     *
     * @param jobName        ジョブ名。
     * @param downloadTarget ダウンロード先のローカルファイル。
     * @throws MalformedURLException URL指定が不全。
     * @throws IOException           何らかの入出力エラー
     * @throws InterruptedException  Jenkinsに割り込みが入った場合の例外。
     */
    public void downloadJobConfigFile(String jobName, File downloadTarget)
            throws MalformedURLException, IOException, InterruptedException {

        FileOutputStream fos = null;

        try {
            // 答え(コンフィグ情報)はFileOutputStriamで受ける
            fos = new FileOutputStream(downloadTarget);

            // Jenkins-cliの「ジョブ定義XMLを標準出力に出力します。」を利用し、job設定をローカルファイルへと落とす。
            execCli(null, fos, "get-job", jobName);

        } finally {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        }

    }

    /**
     * Jenkins中のコントロール用jobの「管理対象job群(自身が終わった後、実行されるjob群)」を更新する。
     *
     * @param contJobName          管理用jobの名前。
     * @param manageTargetJobNames 管理対象job名のList。
     * @throws MalformedURLException        URL指定が不全。
     * @throws IOException                  何らかの入出力エラー
     * @throws InterruptedException         Jenkinsに割り込みが入った場合の例外。
     * @throws ParserConfigurationException Jenkinsから取得したデータが不正の場合。
     * @throws SAXException                 XML系の例外。
     */
    public void updateManagingJobNamesForControlJob(String contJobName,
                                                    final List<String> manageTargetJobNames) throws MalformedURLException,
            IOException, InterruptedException, DocumentException {

        // 内部処理移譲メソッド呼び出し(XMLの編集のみ差分記述)
        this.updateJobConfig(contJobName, new JobConfigEditor() {

            public void editJob(Document configXml) {
                // job名リストのCSV文字列化
                String jobNames =
                        StringUtils.join(manageTargetJobNames.toArray(), ",");

                // XML箇所の特定・取得。
                Element top =
                        (Element) configXml.selectSingleNode("/*/publishers");
                if (top != null) {
                    // 絶対あると思われる、"/*/publishers"が無いならお手上げ。
                    String xpath = "hudson.tasks.BuildTrigger";
                    Element next = (Element) top.selectSingleNode(xpath);
                    if (next == null) {
                        next = (Element) top.addElement(xpath);
                    }
                    xpath = "childProjects";
                    Element end = (Element) next.selectSingleNode(xpath);
                    if (end == null) {
                        end = (Element) next.addElement(xpath);
                    }

                    // デバッグ
                    if (log.isDebugEnabled()) {
                        String message =
                                "config.xml change value : " + end.getText()
                                        + " -> " + jobNames;
                        log.debug(message);
                    }
                    // 実際に書き換え
                    end.setText(jobNames);
                }
            }
        });

    }

    /**
     * 指定されたJobを無効化する。
     *
     * @param jobName 無効化するjob名。
     * @throws MalformedURLException URL指定が不全。
     * @throws IOException           何らかの入出力エラー
     * @throws InterruptedException  Jenkinsに割り込みが入った場合の例外。
     */
    public void desibleJob(String jobName) throws MalformedURLException,
            IOException, InterruptedException {
        // Jenkins-cliの「ジョブを無効化します。」を利用。
        execCli(null, null, "disable-job", jobName);
    }

    /**
     * 指定されたJobを有効化する。
     *
     * @param jobName 有効化するjob名。
     * @throws MalformedURLException URL指定が不全。
     * @throws IOException           何らかの入出力エラー
     * @throws InterruptedException  Jenkinsに割り込みが入った場合の例外。
     */
    public void enableJob(String jobName) throws MalformedURLException,
            IOException, InterruptedException {
        // Jenkins-cliの「ジョブを有効化します。」を利用。
        execCli(null, null, "enable-job", jobName);
    }

    /**
     * 指定されたInputStreamからconfig.xmlファイルの内容を取得、Jobを追加する。
     *
     * @param jobName 追加するjob名。
     * @param is      config.xmlの内容を取得できるInputStreamオブジェクト。
     * @throws MalformedURLException URL指定が不全。
     * @throws IOException           何らかの入出力エラー
     * @throws InterruptedException  Jenkinsに割り込みが入った場合の例外。
     */
    public void addJob(String jobName, InputStream is)
            throws MalformedURLException, IOException, InterruptedException {
        // Jenkins-cliの「標準入力をConfig XMLとして読み込み、ジョブを新規に作成します。」を利用。
        execCli(is, null, "create-job", jobName);
    }

    /**
     * 指定されたjobを削除する。
     *
     * @param jobName 追加するjob名。
     * @throws MalformedURLException URL指定が不全。
     * @throws IOException           何らかの入出力エラー
     * @throws InterruptedException  Jenkinsに割り込みが入った場合の例外。
     */
    public void deleteJob(String jobName) throws MalformedURLException,
            IOException, InterruptedException {
        // Jenkins-cliの「ジョブを削除します。」を利用。
        execCli(null, null, "delete-job", jobName);
    }

    /**
     * 接続や前準備の整ったJenkinsCLIオブジェクトを取得する。
     *
     * @return 接続済みのJenkinsCLIオブジェクト。
     * @throws MalformedURLException URL指定が不全。
     * @throws IOException           何らかの入出力エラー
     * @throws InterruptedException  Jenkinsに割り込みが入った場合の例外。
     */
    protected CLI getConnectedJenkinsCli() throws MalformedURLException,
            IOException, InterruptedException {

        // JenkinsのCLIオブジェクト作成
        CLI cli = new CLI(new URL(urlPath));

        // ログインなど「使う前の処理」あらばここに記述

        // 完成品を返す。
        return cli;

    }

    /**
     * Jenkins中のコントロール用jobを更新する。<br>
     * Jenkins-cliの「config.xmlを標準入力で設定する」「標準出力に出す」コマンドを利用し、 取ったものをXMLとして変更する。
     *
     * @param jobName jobの名前。
     * @param editor  jobのXMLを編集するエディタオブジェクト。
     * @throws MalformedURLException        URL指定が不全。
     * @throws IOException                  何らかの入出力エラー
     * @throws InterruptedException         Jenkinsに割り込みが入った場合の例外。
     * @throws ParserConfigurationException Jenkinsから取得したデータが不正の場合。
     */
    public void updateJobConfig(String jobName, JobConfigEditor editor)
            throws DocumentException, MalformedURLException, IOException,
            InterruptedException {

        OutputStream os = null;
        InputStream is = null;
        File work = null;

        try {

            // データのやり取りはワークファイルとFileInput/OutputStreamで行う。
            work = File.createTempFile("config", ".xml");
            log.debug("temporary config file :" + work.getCanonicalPath());

            // 答え(コンフィグ情報)はFileに受ける
            os = new FileOutputStream(work);

            // Jenkins-cliの「ジョブ定義XMLを標準出力に出力します。」を利用し、job設定をローカルファイルへと落とす。
            execCli(null, os, "get-job", jobName);

            os.close();
            os = null;

            // 受けた標準出力の内容をXMLパーサに掛ける。
            is = new FileInputStream(work);

            SAXReader reader = new SAXReader();
            Document doc = reader.read(is);
            is.close();
            is = null;

            // XML値の変更
            editor.editJob(doc);

            // 再度、StringBufferへ出力
            os = new FileOutputStream(work);
            XMLWriter writer = new XMLWriter(os);
            writer.write(doc);
            writer.flush();
            writer.close();
            os.close();
            os = null;

            // Jenkins-cliの「標準入力からの情報でジョブ定義XMLを更新します。」を利用し、job設定をローカルファイルへと落とす。
            is = new FileInputStream(work);
            execCli(is, null, "update-job", jobName);

            // 後始末。
            is.close();
            is = null;

        } finally {
            if (os != null) {
                os.close();
                os = null;
            }
            if (is != null) {
                is.close();
                is = null;
            }
            if (work != null && work.exists()) {
                work.delete();
            }
        }

    }

    /**
     * Jenkins-cliにてコマンドを実行する。
     *
     * @param is   標準入力からの値が必要な際に指定する。nullを指定すれば標準入力となる。
     * @param os   標準出力へ値が必要な際に指定する。nullを指定すれば標準出力となる。
     * @param args 実際にjenkins-cliwを使用するう際のコマンドライン引数。(可変引数)
     * @throws MalformedURLException URL指定が不全。
     * @throws IOException           何らかの入出力エラー
     * @throws InterruptedException  Jenkinsに割り込みが入った場合の例外。
     */
    protected void execCli(InputStream is, OutputStream os, String... args)
            throws MalformedURLException, IOException, InterruptedException {

        CLI cli = null;

        try {

            // Stream系を省略されている場合、標準入出力をセットしておく。
            if (is == null) {
                is = System.in;
            }
            if (os == null) {
                os = System.out;
            }

            // エラー出力はStringBuilderに受ける。
            StringBuilder sb = new StringBuilder();
            StringBuilderOutputStream sbos = new StringBuilderOutputStream(sb);

            // ※なにやら、早すぎると内部で操作出来なくなるように見えるため、スリープを入れる。
            Thread.sleep(1000);

            // CLI新規作成。
            cli = getConnectedJenkinsCli();

            // コマンド実行
            long result = cli.execute(Arrays.asList(args), is, os, sbos);

            // 結果が正常を返さなかった場合、エラー出力の内容を例外としてスロー。
            if (result != 0) {
                throw new RuntimeException(
                        "Jenkins-ci returnd 'not succeseed' : \n" + sb.toString());
            }

        } finally {
            if (cli != null) {
                cli.close();
                cli = null;
            }
        }

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

    interface JobConfigEditor {

        public void editJob(Document configXml);
    }

}
