package com.github.kazuhito_m.scm2cisync.core;

import com.github.kazuhito_m.commons.log.CustomSystemStreamLog;
import com.github.kazuhito_m.scm2cisync.core.controller.ci.JenkinsControler;
import com.github.kazuhito_m.scm2cisync.core.controller.scm.SubversionControler;
import com.github.kazuhito_m.scm2cisync.mojo.SynchronizeTarget;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNAuthenticationException;

import java.io.File;
import java.util.*;

import static org.junit.Assert.fail;

/**
 * SVN内Javaプロジェクト、最適化プラグイン - テスト前設定変更処理
 *
 * @author Kazuhito Miura
 */
public class ScmToCiSynchronizerTest {

    /**
     * テスト対象クラスオブジェクト
     */
    private ScmToCiSynchronizer sut;

    private SynchronizeTarget st;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        // 初期値セット済みのテスト対象オブジェクトを広域変数にセット。

        st = new SynchronizeTarget();
        st.setScmType("Subversion");
        st.setScmUrl(TestConstants.TEST_SVN_URL);
        st.setCiType("Jenkins");
        st.setCiUrl(TestConstants.TEST_JENKINS_URL);
        st.setCiControlJobName(TestConstants.TEST_JEKNINS_JOB);
        st.setJenkinsJobTemplatePath("src/test/resources/tk/bnbm/scm2cisync/core/config.xml");
        st.setJobDisableByScmDelete(true);
        st.setLastProfilePath("target/test-classes/tk/bnbm/scm2cisync/core/scmSyncLastProfile.xml");
        st.getTargetIds().add(".*");

        ScmToCiSynchronizer t = new ScmToCiSynchronizer();
        t.setLog(new CustomSystemStreamLog());

        this.sut = t;
    }

    @Test
    public void dummy() {

    }

    @Ignore
    public void testAddCiJob001() throws Exception {
        // Arrange-準備
        JenkinsControler jc = new JenkinsControler();
        jc.setUrlPath(st.getCiUrl());
        SubversionControler sc = new SubversionControler();
        sc.setUrlPath(st.getScmUrl());

        // Act-実行。
        sut.addCiJob(jc, "addCiJob", st, sc); // jobを追加。

        // Assert-検証
        // …は、通れば良し！

        // 後片付け。ジョブを削除。
        jc.deleteJob("addCiJob");
    }

    @Ignore
    public void testSynchronize001() throws Exception {

        // Arrange-準備

        // 前処理。履歴ファイルがあるなら削除しておく。
        File lastProfile = new File(st.getLastProfilePath());
        if (lastProfile.exists()) {
            lastProfile.delete();
        }

        // Act-実行
        sut.synchronize(st);
        // 二回目(スキップされるはず)
        sut.synchronize(st);

        // 後始末。上記処理で追加されたjobをテスト環境から削除。
        CustomSystemStreamLog log = new CustomSystemStreamLog();
        log.setDebugEnabled(true);
        JenkinsControler jc = new JenkinsControler();
        jc.setUrlPath(st.getCiUrl());
        jc.setLog(log);
        List<String> jobs = jc.getJobNames();
        Set<String> exclude = new HashSet<String>(Arrays.asList(TestConstants.TEST_JEKNINS_JOBS));
        for (String job : jobs) {
            if (!exclude.contains(job)) {
                jc.deleteJob(job);
            }
        }
        // 実行ジョブリストを初期化
        List<String> joblist = new ArrayList<String>();
        joblist.add("test");
        jc.updateManagingJobNamesForControlJob(TestConstants.TEST_JEKNINS_JOB, joblist);

    }

    /**
     * synchronize()メソッドのテスト。<br>
     * 正常系。BASIC認証が必要だった場合のテスト。
     *
     * @throws Exception
     */
    @Ignore
    public void testSynchronize002() throws Exception {

        // Arrange-準備
        st.setScmUrl(TestConstants.TEST_SVN_AUTH_URL);
        st.setScmAuthType("basic");
        st.setScmUserName("svntest");
        st.setScmPassword("svntest");

        // 前処理。履歴ファイルがあるなら削除しておく。
        File lastProfile = new File(st.getLastProfilePath());
        if (lastProfile.exists()) {
            lastProfile.delete();
        }

        // Act-実行
        sut.synchronize(st);
        // 二回目(スキップされるはず)
        sut.synchronize(st);

        // 後始末。上記処理で追加されたjobをテスト環境から削除。
        CustomSystemStreamLog log = new CustomSystemStreamLog();
        log.setDebugEnabled(true);
        JenkinsControler jc = new JenkinsControler();
        jc.setUrlPath(st.getCiUrl());
        jc.setLog(log);
        List<String> jobs = jc.getJobNames();
        Set<String> exclude = new HashSet<String>(Arrays.asList(TestConstants.TEST_JEKNINS_JOBS));
        for (String job : jobs) {
            if (!exclude.contains(job)) {
                jc.deleteJob(job);
            }
        }
        // 実行ジョブリストを初期化
        List<String> joblist = new ArrayList<String>();
        joblist.add("test");
        jc.updateManagingJobNamesForControlJob(TestConstants.TEST_JEKNINS_JOB, joblist);

    }

    @Ignore
    public void synchronizeメソッドにてBASIC認証が必要かつこける場合() throws Exception {
        // Arrange-準備
        st.setScmUrl(TestConstants.TEST_SVN_AUTH_URL);
        st.setScmAuthType("basic");
        st.setScmUserName("svntest");
        st.setScmPassword("svntest???");

        // Act-実行
        try {
            sut.synchronize(st);
            fail("例外が起こるべき箇所でおこらない。");
        } catch (SVNAuthenticationException e) {
            e.printStackTrace();
        }
    }
}
