package tk.bnbm.scm2cisync.core;

/**
 * 環境依存のテスト設定定数を閉じ込めたインターフェイス。
 *
 * @author Kazuhito Miura
 */
public interface TestConstants {

    /**
     * テスト用のSVNパス
     */
    String TEST_SVN_URL = "http://localhost/repos/scenario/trunk/";
    String TEST_SVN_AUTH_URL = "http://localhost/repos_toolstest/scenario/trunk/";

    /**
     * テスト用のJenkinsパス
     */
    String TEST_JENKINS_URL = "http://localhost:8080/";

    /**
     * テスト用のJenkinsジョブ
     */
    String TEST_JEKNINS_JOB = "base-jobs-kikker";

    /**
     * テスト用のJenkinsジョブ
     */
    String[] TEST_JEKNINS_JOBS = new String[]{TEST_JEKNINS_JOB, "test"};

}
