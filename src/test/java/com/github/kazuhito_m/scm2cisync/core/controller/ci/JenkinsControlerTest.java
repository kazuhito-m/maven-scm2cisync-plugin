package com.github.kazuhito_m.scm2cisync.core.controller.ci;

import com.github.kazuhito_m.commons.log.CustomSystemStreamLog;
import com.github.kazuhito_m.scm2cisync.core.TestConstants;
import org.dom4j.DocumentException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * JenkinsControlerのテストクラス。
 *
 * @author Kazuhito Miura
 */
public class JenkinsControlerTest {

    /**
     * テスト対象クラスオブジェクト
     */
    private JenkinsControler sut;

    @Before
    public void setUp() throws Exception {

        // 初期値セット済みのテスト対象オブジェクトを広域変数にセット。

        JenkinsControler t = new JenkinsControler();

        CustomSystemStreamLog log = new CustomSystemStreamLog();
        log.setDebugEnabled(true);
        t.setLog(log);

        // Jenkinsのパス(どうせこの現場でしかビルドしないのだから、決め打ちで)
        t.setUrlPath(TestConstants.TEST_JENKINS_URL);

        this.sut = t;
    }

    @Test
    public void dummy() {

    }

    @Ignore
    public void testGetJobNames001() throws Exception {

        List<String> names = sut.getJobNames();

        // 複数件返ってくることを基準とする。
        assertThat(names.size() > 0, is(true));

        // デバッグ
        int i = 0;
        for (String name : names) {
            System.out.println(++i + " : " + name);
        }

    }

    @Ignore
    public void testDownloadJobConfigFile() throws IOException,
            InterruptedException {

        // ファイルダウンロード先を確保(すでにあったら削除)
        File tmp = File.createTempFile("config", ".xml");
        if (tmp.exists()) {
            assertThat(tmp.delete(), is(true));
        }
        assertThat(tmp.exists(), is(false));
        System.out.println("Tempolary file : " + tmp.getCanonicalPath());

        // ダウンロード実行。
        sut.downloadJobConfigFile(TestConstants.TEST_JEKNINS_JOB, tmp);

        // 存在するか。
        assertThat(tmp.exists(), is(true));
        // デバッグ。読みだしてみる。
        FileReader fr = new FileReader(tmp);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        fr.close();
        br.close();
        // 後後片付け、削除。
        tmp.delete();

    }

    @Ignore
    public void testUpdateManagingJobNamesForControlJob() throws IOException,
            InterruptedException, DocumentException, SAXException {

        List<String> jobs = new ArrayList<String>();
        jobs.add("");

        // 1度目、空にする。
        sut.updateManagingJobNamesForControlJob(TestConstants.TEST_JEKNINS_JOB, jobs);

        jobs.clear();
        jobs.add("test");

        // 2度目、test(絶対あるjob名)を下記戻す。
        sut.updateManagingJobNamesForControlJob(TestConstants.TEST_JEKNINS_JOB, jobs);

    }

    @Ignore
    public void testDisableJob() throws Exception {
        // ジョブの無効化。
        sut.desibleJob(TestConstants.TEST_JEKNINS_JOB);
        // ジョブの有効化。
        sut.enableJob(TestConstants.TEST_JEKNINS_JOB);
    }

}
