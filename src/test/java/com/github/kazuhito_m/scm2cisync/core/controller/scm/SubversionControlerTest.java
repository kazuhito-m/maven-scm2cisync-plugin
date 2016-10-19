package com.github.kazuhito_m.scm2cisync.core.controller.scm;

import com.github.kazuhito_m.scm2cisync.core.TestConstants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import com.github.kazuhito_m.commons.log.CustomSystemStreamLog;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * SubversionControlerのテストクラス。
 *
 * @author Kazuhito Miura
 */
public class SubversionControlerTest {

    /**
     * テスト対象クラスオブジェクト
     */
    private SubversionControler sut;

    @Before
    public void setUp() throws Exception {

        // 初期値セット済みのテスト対象オブジェクトを広域変数にセット。

        SubversionControler t = new SubversionControler();
        t.setLog(new CustomSystemStreamLog());

        // Jenkinsのパス(どうせこの現場でしかビルドしないのだから、決め打ちで)
        t.setUrlPath(TestConstants.TEST_SVN_URL);

        this.sut = t;
    }

    @Ignore
    // FIXME テスト用Subversionサーバorスタブが無いので、とりあえず除外。
    public void testGetDirNameList001() throws Exception {

        List<String> names = sut.getDirNameList();
        // デバッグ
        int i = 0;
        for (String name : names) {
            System.out.println(++i + " : " + name);
        }

        // 複数件返ってくることを基準とする。
        assertThat(names.size() > 3, is(true));

    }

    @Ignore
    // FIXME テスト用Subversionサーバorスタブが無いので、とりあえず除外。
    public void getDirNameListにてバージョン指定時の値確認() throws Exception {

        List<String> names = sut.getDirNameList(80);

        // デバッグ
        int i = 0;
        for (String name : names) {
            System.out.println(++i + " : " + name);
        }

        // 複数件返ってくることを基準とする。
        assertThat(names.size() >= 2, is(true));

    }

    /**
     * getDirNameList()メソッドのテスト。<br>
     * 正常系。認証(一例としてBASIC)を行うテスト。
     *
     * @throws Exception
     */
    @Ignore
    // FIXME テスト用Subversionサーバorスタブが無いので、とりあえず除外。
    public void testGetDirNameList003() throws Exception {

        // Arrange-準備
        sut.setAuthType("basic");
        sut.setUserName("svntest");
        sut.setPassword("svntest");
        sut.setUrlPath(TestConstants.TEST_SVN_AUTH_URL);

        // Act-実行
        List<String> names = sut.getDirNameList(92);

        // Assert-検証

        // 複数件返ってくることを基準とする。
        assertThat(names.size() >= 2, is(true));

        // デバッグ
        int i = 0;
        for (String name : names) {
            System.out.println(++i + " : " + name);
        }

        // 検証でコケル場合

        // Arrange-準備
        sut.setPassword("svntest?");
        boolean isRiseException = false;

        // Act-実行
        try {
            names = sut.getDirNameList(92);
        } catch (SVNAuthenticationException e) {
            isRiseException = true;
        }

        // Assert-検証
        assertThat("BASIC認証時「例外が起こって欲しい箇所」で起こらない。", isRiseException, is(true));


    }

    @Ignore
    // FIXME テスト用Subversionサーバorスタブが無いので、とりあえず除外。
    public void testGetAddedDirNameList001() throws Exception {

        List<String> names = sut.getAddedDirNameList(10);

        // 複数件返ってくることを基準とする。
        assertThat(names.size() >= 1, is(true));

        // デバッグ
        int i = 0;
        System.out.println("getAddedDirNameList() execute");
        for (String name : names) {
            System.out.println(++i + " : " + name);
        }

    }

    @Ignore
    // FIXME テスト用Subversionサーバorスタブが無いので、とりあえず除外。
    public void testGetHeadRevisionNo() throws SVNException {

        long no = sut.getHeadRevisionNo();
        // 1とかではない…程度のチェックしかしてない。
        assertThat(no >= 19, is(true));

        // デバッグ
        System.out.println("getHeadRevisionNo() execute");
        System.out.println("headRegNo:" + no);

    }

    /**
     * createOptimizedClientManager()メソッドのテスト。 正常系。「デフォルト設定でnullを返さない」チェック。
     */
    @Test
    public void testCreateOptimizedClientManager001() {
        // Arrenge-準備

        // Act-実行
        Object res = sut.createOptimizedClientManager();

        // Assert-検証
        assertThat(res, is(notNullValue()));
    }

    // FIXME テスト用Subversionサーバorスタブが無いので、とりあえず除外。
    @Ignore
    public void createOptimizedClientManagerメソッドにてベーシック認証の設定を含むインスタンスが帰ってくるか() throws SVNException {
        // Arrenge-準備
        sut.setAuthType("basic");
        sut.setUserName("svntest");
        sut.setPassword("svntest");

        // Act-実行
        SVNClientManager res = sut.createOptimizedClientManager();

        // Assert-検証
        assertThat(res, is(notNullValue()));

        // 内部から「認証オブジェクト」取り出して、検算
        SVNRepository rep = res.createRepository(
                SVNURL.parseURIEncoded(TestConstants.TEST_SVN_AUTH_URL), true);
        ISVNAuthenticationManager authManager = rep.getAuthenticationManager();
        assertThat(authManager.getClass().equals(BasicAuthenticationManager.class), is(true));

    }
}
